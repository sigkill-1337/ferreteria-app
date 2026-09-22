# Herramientas

## Icono de la aplicación

`generar_icono.py` genera el icono completo —una llave de tuercas— a partir de
una sola definición geométrica:

```bash
pip install pillow
python tools/generar_icono.py
```

Produce:

| Salida | Para |
|---|---|
| `drawable/ic_launcher_background.xml` | Fondo del icono adaptativo |
| `drawable/ic_launcher_foreground.xml` | La llave, en azul |
| `drawable/ic_launcher_monochrome.xml` | Silueta blanca para los iconos temáticos de Android 13+ |
| `mipmap-anydpi-v26/ic_launcher{,_round}.xml` | Declaración del icono adaptativo |
| `mipmap-{m,h,x,xx,xxx}dpi/ic_launcher{,_round}.webp` | 10 bitmaps para Android 7.x |

**Por qué un script y no Image Asset Studio.** El icono existe en dos formatos a
la vez: vectorial para Android 8.0+ y mapa de bits para Android 7.x, que no
entiende iconos adaptativos. Al `minSdk` de este proyecto (24) hacen falta los
dos, y si se dibujan por separado acaban sin coincidir. Aquí la geometría se
define una vez y de ella salen el `pathData` y los bitmaps.

### Verificación

```bash
python tools/verificar_icono.py
```

Rasteriza el `pathData` implementando el arco elíptico de SVG y lo compara
píxel a píxel contra la versión dibujada con primitivas de Pillow. Debe dar
menos del 0.2% de diferencia —lo que queda es el borde dentado del aplanado de
curvas—. Deja `comparacion.png`, donde rojo es lo que solo está en el vector y
verde lo que solo está en los bitmaps.

No es un lujo: en la primera versión los dos arcos del mango llevaban el flag
de barrido invertido y el vector salía con los extremos mordidos, algo que no
se habría notado hasta instalar la app.
