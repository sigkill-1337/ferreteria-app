# Ferretería — App Android

App de punto de venta e inventario en **Kotlin + Jetpack Compose**. Consume la
API REST del repo [`ferreteria-api`](https://github.com/sigkill-1337/ferreteria-api)
(PHP + PDO sobre MariaDB) y cubre CRUD completo sobre las 8 tablas del esquema,
más una sección de reportes que ejecuta los procedimientos almacenados y las
consultas agrupadas de la base de datos.

> ⚠️ **Repo privado a propósito.** `local.properties` **sí** está versionado aquí
> e incluye la API key del servidor. Si este repositorio se vuelve público en
> algún momento, esa key queda expuesta y hay que rotarla en el servidor de
> inmediato. Ya pasó una vez con `ferreteria-api-docs`, que decía ser privado y
> no lo era.

---

## Índice

1. [Cómo correrla](#cómo-correrla)
2. [Arquitectura](#arquitectura)
3. [Flujo de datos](#flujo-de-datos)
4. [Archivos clave](#archivos-clave)
5. [Pantallas](#pantallas)
6. [Lenguaje visual](#lenguaje-visual)
7. [Decisiones técnicas](#decisiones-técnicas)
8. [Dependencias](#dependencias)
9. [Estado](#estado)

---

## Cómo correrla

**Requisitos:** Android Studio (con JDK 21+), SDK de Android 37, un dispositivo
o emulador con API 24 o superior.

```bash
./gradlew :app:assembleDebug     # APK en app/build/outputs/apk/debug/
./gradlew :app:installDebug      # instala en el dispositivo conectado
```

`local.properties` trae dos valores:

```properties
sdk.dir=C:\\Users\\root\\AppData\\Local\\Android\\Sdk
FERRETERIA_API_KEY=<la key del servidor>
```

`sdk.dir` es específico de la máquina; si clonas en otra, Android Studio lo
reescribe solo al abrir el proyecto. `FERRETERIA_API_KEY` se inyecta en
`BuildConfig` desde `app/build.gradle.kts` y de ahí la toma el interceptor de
OkHttp. Si falta, la app arranca igual y muestra una pantalla explicándolo, en
lugar de fallar con 401 a ciegas.

**El backend tiene que estar al día.** Se necesita el commit `6f2eb8f` o
posterior de `ferreteria-api`, que agrega la columna `VENTA.canal`,
`reportes.php`, los procedimientos almacenados y los triggers. Con la versión
anterior, la pestaña de Reportes responde 404 y registrar una venta falla porque
la columna `canal` no existe. El usuario de MariaDB también necesita
`GRANT EXECUTE`, o los `CALL` a los procedimientos truenan.

---

## Arquitectura

MVVM en tres capas, sin inyección de dependencias ni base de datos local: la
fuente de verdad siempre es el servidor.

```
┌──────────────────────────────────────────────────────────────┐
│  UI            Composables (Pantalla*.kt, Comunes.kt)        │
│                Estado inmutable, eventos hacia arriba         │
└───────────────────────────┬──────────────────────────────────┘
                            │ StateFlow<Estado…>  /  llamadas a funciones
┌───────────────────────────┴──────────────────────────────────┐
│  ViewModel     CrudViewModel<T, R> genérico + 5 concretos     │
│                VentasViewModel · ReportesViewModel            │
│                Sobreviven a la rotación; nada de lógica de UI │
└───────────────────────────┬──────────────────────────────────┘
                            │ suspend fun → Resultado<T>
┌───────────────────────────┴──────────────────────────────────┐
│  Datos         Repositorio (objeto único)                     │
│                ApiClient → Retrofit + Moshi + OkHttp          │
│                Modelos.kt (reflejan el JSON tal cual)         │
└───────────────────────────┬──────────────────────────────────┘
                            │ HTTPS + header X-API-Key
                     ┌──────┴───────┐
                     │  ferreteria  │  PHP/PDO → MariaDB
                     │     -api     │  procedimientos y triggers
                     └──────────────┘
```

**Por qué así:**

- **Sin caché local (Room).** Los catálogos son de decenas de registros y el
  stock cambia con cada venta; una copia local solo introduciría desincronización
  sin ganar nada.
- **Sin Hilt/Koin.** `Repositorio` y `ApiClient` son `object`, y los ViewModels
  se crean con el `viewModel()` por defecto. Con seis ViewModels sin dependencias
  en el constructor, un framework de inyección sería andamio de más.
- **La lógica de negocio vive en el servidor.** La app manda `id_producto` y
  `cantidad`; el backend busca precios, calcula subtotales y total con `bcmath`,
  descuenta stock y hace commit, todo en una transacción. La app nunca calcula
  un total que se vaya a guardar.

---

## Flujo de datos

Un caso completo, registrar una venta:

```
Usuario arma el carrito
        │
        ▼
FormularioVenta (estado local: carrito, cliente, empleado, canal)
        │  alGuardar(idCliente, idEmpleado, canal, lineas)
        ▼
VentasViewModel.guardarVenta()           estado.guardando = true
        │
        ▼
Repositorio.crearVenta(VentaRequest)     envuelve en try/catch
        │
        ▼
FerreteriaApi.crearVenta()  ──POST──▶  ventas.php
                                          BEGIN
                                          bloquea productos (FOR UPDATE)
                                          valida stock
                                          calcula totales (bcmath)
                                          INSERT VENTA + DETALLE_VENTA
                                          descuenta stock
                                          COMMIT
                                          └─▶ trigger trg_venta_seguimiento
                                              (solo canal APP o WEB)
        │
        ▼
Resultado.Ok(Venta)  →  ViewModel actualiza estado + recarga lista
        │                y avisa a Productos para refrescar el stock
        ▼
UI recompone · snackbar "Venta #7 registrada"
```

Los errores nunca viajan como excepción: `Repositorio.llamar()` atrapa todo y
devuelve `Resultado.Error(codigo, mensaje)`, sacando el texto de
`{"error": "..."}` que manda la API. Así un `409` por correo duplicado llega a la
pantalla con el mensaje real del procedimiento almacenado, no con un texto
genérico.

---

## Archivos clave

### Capa de datos — `app/src/main/java/com/example/ferreteria/data/`

| Archivo | Qué hace |
|---|---|
| `Modelos.kt` | Todos los `data class` que reflejan el JSON. Nombres en snake_case para que coincidan con las llaves del backend sin anotar nada. |
| `FerreteriaApi.kt` | Interfaz Retrofit. GET/POST/PUT/DELETE por recurso, más los seis reportes. |
| `ApiClient.kt` | Arma Retrofit + Moshi + OkHttp. Interceptor de la API key y log con el header redactado. |
| `Repositorio.kt` | Punto único de acceso. Envuelve cada llamada en `Resultado` y traduce los errores HTTP a mensajes en español. |

### Capa de presentación — `app/src/main/java/com/example/ferreteria/ui/`

| Archivo | Qué hace |
|---|---|
| `AppFerreteria.kt` | Raíz: `Scaffold`, barra inferior de 5 pestañas, snackbars, y el submenú de "Más". |
| `ViewModels.kt` | `CrudViewModel<T, R>` genérico (cargar, guardar, eliminar, errores) y los cinco concretos: productos, clientes, empleados, categorías, proveedores. |
| `VentasViewModel.kt` | Ventas. No encaja en el CRUD genérico porque el POST no manda la misma forma que devuelve el GET. |
| `ReportesViewModel.kt` | Los seis reportes y sus parámetros (fecha, año). |
| `Comunes.kt` | Biblioteca de UI: `TarjetaConFranja`, `TarjetaRegistro`, `Clave`, `Importe`, `BarraNivel`, `CuadroIcono`, `Etiqueta`, `Avatar`, `SelectorOpcion`, `DialogoFormulario`, `EstadoVacio`. Todo lo que se repite. |
| `Formato.kt` | Pesos con `BigDecimal`, fechas legibles, plurales. |
| `Pantalla*.kt` | Una por sección. Cada una tiene su lista y su formulario. |
| `theme/` | Paleta violeta (claro y oscuro), acentos de categoría, colores de estado que Material 3 no define, y la tipografía afinada. |

### Configuración

| Archivo | Qué hace |
|---|---|
| `app/build.gradle.kts` | Lee `FERRETERIA_API_KEY` de `local.properties` y la inyecta en `BuildConfig`. Activa Compose y `buildConfig`. |
| `gradle/libs.versions.toml` | Catálogo de versiones. Todas las dependencias se declaran aquí. |
| `local.properties` | SDK y API key. **Versionado en este repo porque es privado.** |

---

## Pantallas

| Pestaña | Operaciones |
|---|---|
| **Inventario** | Lista con buscador, stock con semáforo de color y margen por pieza · alta · edición · baja |
| **Ventas** | Historial · ticket a pantalla completa · carrito con control de cantidad · edición · cancelación (devuelve el stock) |
| **Clientes** | Lista con buscador · alta · edición · baja |
| **Reportes** | Ventas del día · clientes vigentes del trimestre · más vendidos · ventas por canal · directorio · bitácora de seguimiento |
| **Más** | Empleados · Categorías · Proveedores, los tres con CRUD completo |

**Reportes** es la sección que enseña lo que vive en la base de datos y no en el
código: los dos procedimientos almacenados, las consultas con `GROUP BY`, el
`UNION` de clientes y empleados, y la bitácora que llena el trigger. Cada reporte
trae escrito de dónde sale.

Cada venta lleva un **canal** (app, sitio web o mostrador). Las de app y web
disparan el trigger que las registra en la bitácora; las de mostrador no, porque
a ese cliente ya se le atendió en persona.

Al **editar** una venta, el tope de cada producto es su stock actual **más** lo
que esa misma venta ya tenía apartado, porque el servidor repone el detalle
anterior antes de validar el nuevo.

---

## Lenguaje visual

La primera versión se veía genérica porque usaba el mismo renglón de Material
para todo: círculo con iniciales, título, subtítulo, chips. Ese patrón viene de
las apps de contactos, y aplicado a un martillo hacía que el inventario se leyera
como una agenda. El rediseño parte de dos reglas.

**1. El círculo con iniciales es solo para personas.** Clientes y empleados lo
llevan, porque un nombre propio sí se abrevia así. Los productos, categorías y
proveedores no: se identifican por su clave y, cuando hace falta un símbolo,
llevan un cuadro con icono (`CuadroIcono`), no un círculo.

**2. Cada tarjeta lleva una franja de acento a la izquierda.** Da ritmo a las
listas y permite agrupar de un vistazo sin leer. El color es estable por
identificador (`TemaFerreteria.acentoDe`), así que una categoría siempre se ve
igual. En ventas la franja codifica el canal; en personas, su color de avatar.

De ahí se derivan las piezas:

| Pieza | Para qué |
|---|---|
| `Clave` | Folios y claves en monoespaciado: `#001`, `V-0007`. Es el detalle que más hace por que la app se lea como un sistema de inventario. |
| `Importe` | Precios en el peso más alto de la tipografía con tracking negativo, para que dominen el renglón sin agrandarlos. |
| `BarraNivel` | Barra de existencias, medida contra el producto de mayor stock de la lista para que las barras se comparen entre sí. |
| `TarjetaConFranja` | Contenedor base de todas las listas. |
| `CuadroIcono` | El sustituto del círculo cuando lo que se representa es una cosa. |
| `Rotulo` | Encabezados en mayúsculas con tracking amplio. |

La tipografía también carga parte del trabajo: títulos con peso alto y tracking
**negativo**, etiquetas pequeñas con tracking **amplio**. Ese contraste es lo que
hace que una pantalla se vea compuesta y no solo rellenada.

Un detalle de implementación que vale la pena conocer: la franja se pinta con
`drawBehind`, no con un `Box` de altura intrínseca. `IntrinsicSize.Min` obliga a
que todos los hijos soporten medición intrínseca, y basta uno que no la soporte
para tumbar la lista en ejecución. `drawBehind` usa el tamaño ya medido y
funciona con cualquier contenido.

## Decisiones técnicas

**Los importes son `String` en los modelos.** En MariaDB son `DECIMAL(10,2)` y
PDO los devuelve como texto. Para operar se convierten a `BigDecimal` con
`aDecimal()`, nunca a `Double`: con `Double`, `0.1 + 0.2` no da `0.3` y los
totales acaban con centavos fantasma.

**Moshi necesita `KotlinJsonAdapterFactory` explícito.** Con
`MoshiConverterFactory.create()` a secas, un `data class` de Kotlin sin codegen
truena en tiempo de ejecución al parsear. Es un error que no aparece al compilar.

**El log de OkHttp redacta `X-API-Key`.** Sin `redactHeader`, la key aparecería
en Logcat en cada request.

**No se usa navigation-compose.** Las cinco secciones son pestañas y los
formularios son diálogos a pantalla completa, así que un grafo de navegación
sería complejidad sin beneficio. La pestaña "Más" maneja su submenú con estado
local y un `BackHandler`.

**Los iconos se declaran aparte** (`material-icons-extended`). En esta versión de
Compose ya no llegan de forma transitiva con `material3`.

**La paleta viene del tema de GitHub (Primer)**, y sigue el modo claro/oscuro
del sistema. El oscuro usa la escala de grises de GitHub —`#0D1117` de fondo,
`#161B22` para las tarjetas, `#30363D` de borde— con el azul `#58A6FF` como
color de acción. El claro se derivó del tema claro de GitHub, no se dejó el
violeta anterior: con dos paletas sin relación, cambiar el modo del teléfono se
sentiría como abrir otra aplicación.

Lo que hace reconocible a este tema no es solo el color. Es el escalón corto
entre fondo y superficie, y el **borde de 1px** en cada tarjeta. Con superficies
tan próximas, sin ese borde todo se ve plano. Las esquinas también son más
cerradas que el valor por omisión de Material: 8dp en tarjetas y campos, 6dp en
etiquetas.

**Sin `dynamicColor`**, a propósito: los colores cargan significado —verde
"stock sano", ámbar "quedan pocos", rojo "agotado"— y con colores dinámicos
cambiarían de teléfono en teléfono. Como Material 3 no define "éxito" ni
"advertencia", esos pares viven en `ColoresEstado` y se reparten con un
`CompositionLocal`.

**El fondo de ventana está declarado en XML, con variante `values-night`.** Es
el color que pinta Android antes de que Compose dibuje; si se deja el blanco por
omisión, abrir la app en modo oscuro produce un destello.

**Las etiquetas van en `FlowRow`, no en `Row`.** Un `Row` reparte el ancho entre
sus hijos y, cuando no caben, comprime el texto hasta partirlo letra por letra en
vertical. Con `FlowRow` la etiqueta que no cabe se baja al siguiente renglón.

---

## Dependencias

Declaradas en `gradle/libs.versions.toml`.

| Librería | Versión | Para qué |
|---|---|---|
| AGP | 9.3.2 | Plugin de Android |
| Kotlin | 2.2.10 | Lenguaje y plugin de Compose |
| Gradle | 9.5 | Build |
| Compose BOM | 2026.02.01 | Versiones alineadas de Compose |
| Material 3 | vía BOM | Componentes |
| material-icons-extended | 1.7.8 | Iconos (ya no son transitivos) |
| Lifecycle | 2.9.0 | ViewModel + `viewModel()` en Compose |
| Retrofit | 2.11.0 | Cliente HTTP |
| Moshi | 1.15.1 | JSON con adaptador de Kotlin |
| OkHttp logging-interceptor | 4.12.0 | Log de red en debug |

`compileSdk` 37 · `minSdk` 24 · `targetSdk` 37.

---

## Estado

- `./gradlew :app:assembleDebug` compila sin errores ni advertencias.
- **Sin probar en dispositivo**: no había emulador con AVD ni teléfono conectado
  en la máquina donde se desarrolló.
- El APK de debug pesa ~20 MB porque `material-icons-extended` trae el set
  completo. En release, activando `optimization { enable = true }` en
  `app/build.gradle.kts`, R8 se queda solo con los iconos que se usan.
