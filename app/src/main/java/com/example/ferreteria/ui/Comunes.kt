package com.example.ferreteria.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ferreteria.ui.theme.EstiloClave
import com.example.ferreteria.ui.theme.EstiloImporte
import com.example.ferreteria.ui.theme.EstiloImporteGrande
import com.example.ferreteria.ui.theme.EstiloRotulo
import com.example.ferreteria.ui.theme.TemaFerreteria

/**
 * Lenguaje visual de la app.
 *
 * Dos reglas lo sostienen, y conviene respetarlas al agregar pantallas:
 *
 * 1. **El círculo con iniciales es solo para personas.** Clientes y empleados lo
 *    llevan porque un nombre propio sí se abrevia así. Un producto no: ponerle
 *    "MA" a un martillo hace que el inventario se lea como una agenda de
 *    contactos. Las cosas se identifican por su franja de color y su clave.
 *
 * 2. **Cada tarjeta lleva una franja de acento a la izquierda.** Es lo que da
 *    ritmo a las listas y permite agrupar de un vistazo sin leer.
 */

/** Grosor de la franja de acento que llevan las tarjetas. */
private val anchoFranja = 4.dp

/** Tono semántico de una etiqueta. Define el par contenedor/texto que usa. */
enum class Tono { NEUTRO, PRIMARIO, ACENTO, EXITO, ALERTA, ERROR }

@Composable
private fun coloresDeTono(tono: Tono): Pair<Color, Color> {
    val estado = TemaFerreteria.estado
    return when (tono) {
        Tono.NEUTRO -> MaterialTheme.colorScheme.surfaceContainerHighest to
            MaterialTheme.colorScheme.onSurfaceVariant

        Tono.PRIMARIO -> MaterialTheme.colorScheme.primaryContainer to
            MaterialTheme.colorScheme.onPrimaryContainer

        Tono.ACENTO -> MaterialTheme.colorScheme.tertiaryContainer to
            MaterialTheme.colorScheme.onTertiaryContainer

        Tono.EXITO -> estado.exitoContenedor to estado.sobreExitoContenedor
        Tono.ALERTA -> estado.alertaContenedor to estado.sobreAlertaContenedor
        Tono.ERROR -> MaterialTheme.colorScheme.errorContainer to
            MaterialTheme.colorScheme.onErrorContainer
    }
}

/** Color plano de un tono, para franjas y barras. */
@Composable
fun colorDeTono(tono: Tono): Color {
    val estado = TemaFerreteria.estado
    return when (tono) {
        Tono.NEUTRO -> MaterialTheme.colorScheme.outline
        Tono.PRIMARIO -> MaterialTheme.colorScheme.primary
        Tono.ACENTO -> MaterialTheme.colorScheme.tertiary
        Tono.EXITO -> estado.exito
        Tono.ALERTA -> estado.alerta
        Tono.ERROR -> MaterialTheme.colorScheme.error
    }
}

// ---------------------------------------------------------------------------
// Piezas atómicas
// ---------------------------------------------------------------------------

/** Rótulo de sección: mayúsculas con tracking amplio. */
@Composable
fun Rotulo(
    texto: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Text(
        texto.uppercase(),
        modifier = modifier,
        style = EstiloRotulo,
        color = color,
    )
}

/** Alias histórico; se conserva para no tocar las pantallas de formulario. */
@Composable
fun TituloSeccion(texto: String, modifier: Modifier = Modifier) {
    Rotulo(texto, modifier.padding(top = 8.dp))
}

/**
 * Clave o folio en monoespaciado. Es el detalle que más hace por que la app se
 * lea como un sistema de inventario y no como una libreta.
 */
@Composable
fun Clave(texto: String, modifier: Modifier = Modifier) {
    Text(
        texto,
        modifier = modifier,
        style = EstiloClave,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
    )
}

/** Importe alineado a la derecha, con el peso más alto de la tipografía. */
@Composable
fun Importe(
    texto: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(texto, modifier = modifier, style = EstiloImporte, color = color, maxLines = 1)
}

data class DatosEtiqueta(
    val texto: String,
    val tono: Tono = Tono.NEUTRO,
    val icono: ImageVector? = null,
)

/** Etiqueta rectangular de esquinas suaves — no píldora: se ve menos genérica. */
@Composable
fun Etiqueta(datos: DatosEtiqueta, modifier: Modifier = Modifier) {
    val (fondo, texto) = coloresDeTono(datos.tono)

    Surface(
        modifier = modifier,
        color = fondo,
        contentColor = texto,
        shape = RoundedCornerShape(6.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (datos.icono != null) {
                Icon(datos.icono, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(4.dp))
            }
            Text(
                datos.texto,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

data class DatosAvatar(
    val texto: String? = null,
    val icono: ImageVector? = null,
    val tono: Tono = Tono.PRIMARIO,
    /** Si viene, gana sobre [tono]: sirve para igualar el avatar a la franja. */
    val color: Color? = null,
)

/**
 * Círculo con iniciales o icono. **Solo para personas** (clientes, empleados).
 * Para productos y catálogos se usa [CuadroIcono], que no evoca una agenda.
 */
@Composable
fun Avatar(datos: DatosAvatar, modifier: Modifier = Modifier) {
    val porTono = coloresDeTono(datos.tono)
    val fondo = datos.color?.copy(alpha = 0.18f) ?: porTono.first
    val contenido = datos.color ?: porTono.second

    Surface(
        modifier = modifier.size(42.dp),
        color = fondo,
        contentColor = contenido,
        shape = CircleShape,
    ) {
        Box(contentAlignment = Alignment.Center) {
            when {
                datos.icono != null -> Icon(
                    datos.icono,
                    contentDescription = null,
                    modifier = Modifier.size(21.dp),
                )

                else -> Text(
                    datos.texto.orEmpty().take(2).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

/** Cuadro con icono, para cosas: categorías, proveedores, documentos. */
@Composable
fun CuadroIcono(
    icono: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    tamano: Int = 40,
) {
    Surface(
        modifier = modifier.size(tamano.dp),
        color = color.copy(alpha = 0.16f),
        contentColor = color,
        shape = RoundedCornerShape(10.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icono, contentDescription = null, modifier = Modifier.size((tamano * 0.5).dp))
        }
    }
}

/** Iniciales de un nombre: "Ana López" -> "AL". */
fun iniciales(vararg partes: String?): String =
    partes.filter { !it.isNullOrBlank() }
        .take(2)
        .joinToString("") { it!!.trim().take(1) }
        .uppercase()

/**
 * Barra de existencias. Muestra el nivel de un producto contra el mayor de la
 * lista, no contra 100: así se compara entre renglones de un vistazo.
 */
@Composable
fun BarraNivel(
    valor: Int,
    maximo: Int,
    color: Color,
    modifier: Modifier = Modifier,
    alto: Int = 5,
) {
    val fraccion = if (maximo <= 0) 0f else (valor.toFloat() / maximo).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(alto.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
    ) {
        if (fraccion > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraccion)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(color),
            )
        }
    }
}

/** Botones de editar y borrar de un renglón. */
@Composable
fun AccionesFila(
    alEditar: (() -> Unit)? = null,
    alBorrar: (() -> Unit)? = null,
) {
    Row {
        if (alEditar != null) {
            IconButton(onClick = alEditar, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.EditNote,
                    contentDescription = "Editar",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (alBorrar != null) {
            IconButton(onClick = alBorrar, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Eliminar",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Contenedores
// ---------------------------------------------------------------------------

/**
 * Tarjeta con franja de acento a la izquierda. Es el contenedor base de todas
 * las listas; lo que cambia entre secciones es lo que va dentro.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarjetaConFranja(
    acento: Color,
    modifier: Modifier = Modifier,
    alTocar: (() -> Unit)? = null,
    contenido: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = alTocar ?: {},
        enabled = alTocar != null,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        // La franja se pinta con drawBehind y no con un Box de altura intrínseca:
        // drawBehind usa el tamaño ya medido, así que funciona con cualquier
        // contenido. Con IntrinsicSize.Min bastaría un hijo que no soporte
        // medición intrínseca para tumbar la lista en ejecución.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(color = acento, size = Size(anchoFranja.toPx(), size.height))
                }
                .padding(start = anchoFranja),
        ) {
            contenido()
        }
    }
}

/**
 * Renglón genérico para catálogos y listados simples.
 *
 * El avatar es opcional y debe usarse **solo con personas**. Para lo demás, se
 * pasa [icono] y se dibuja un cuadro, o nada y manda la franja sola.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TarjetaRegistro(
    titulo: String,
    modifier: Modifier = Modifier,
    acento: Color? = null,
    clave: String? = null,
    subtitulo: String? = null,
    lineas: List<String> = emptyList(),
    etiquetas: List<DatosEtiqueta> = emptyList(),
    avatar: DatosAvatar? = null,
    icono: ImageVector? = null,
    valor: String? = null,
    valorSecundario: String? = null,
    alEditar: (() -> Unit)? = null,
    alBorrar: (() -> Unit)? = null,
    alTocar: (() -> Unit)? = null,
) {
    val colorAcento = acento ?: MaterialTheme.colorScheme.primary

    TarjetaConFranja(acento = colorAcento, modifier = modifier, alTocar = alTocar) {
        Row(modifier = Modifier.padding(start = 12.dp, top = 12.dp, end = 6.dp, bottom = 10.dp)) {
            when {
                avatar != null -> {
                    Avatar(avatar)
                    Spacer(Modifier.width(12.dp))
                }

                icono != null -> {
                    CuadroIcono(icono, colorAcento)
                    Spacer(Modifier.width(12.dp))
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                if (clave != null) {
                    Clave(clave)
                    Spacer(Modifier.height(2.dp))
                }

                Text(
                    titulo,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (subtitulo != null) {
                    Text(
                        subtitulo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                lineas.forEach { linea ->
                    Text(
                        linea,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (etiquetas.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        etiquetas.forEach { Etiqueta(it) }
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .widthIn(max = 130.dp),
            ) {
                if (valor != null) {
                    Importe(valor, color = MaterialTheme.colorScheme.onSurface)
                }
                if (valorSecundario != null) {
                    Text(
                        valorSecundario,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (alEditar != null || alBorrar != null) {
                    Spacer(Modifier.height(4.dp))
                    AccionesFila(alEditar, alBorrar)
                }
            }
        }
    }
}

/**
 * Panel de cifra que encabeza una lista. El número manda: va en el tamaño más
 * grande de la tipografía y el rótulo va arriba, chico y espaciado.
 */
@Composable
fun TarjetaResumen(
    icono: ImageVector,
    etiqueta: String,
    valor: String,
    detalle: String? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icono, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(etiqueta.uppercase(), style = EstiloRotulo)
            }

            Spacer(Modifier.height(10.dp))

            Text(valor, style = EstiloImporteGrande, maxLines = 1)

            if (detalle != null) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                Spacer(Modifier.height(8.dp))
                Text(detalle, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Listas y estados
// ---------------------------------------------------------------------------

@Composable
fun CampoBusqueda(
    valor: String,
    alCambiar: (String) -> Unit,
    marcador: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        placeholder = {
            Text(marcador, style = MaterialTheme.typography.bodyMedium)
        },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp))
        },
        trailingIcon = {
            if (valor.isNotEmpty()) {
                IconButton(onClick = { alCambiar("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    )
}

@Composable
fun <T> ContenidoLista(
    cargando: Boolean,
    error: String?,
    items: List<T>,
    textoVacio: String,
    alReintentar: () -> Unit,
    modifier: Modifier = Modifier,
    tituloVacio: String = "Sin registros",
    iconoVacio: ImageVector = Icons.Default.Inbox,
    encabezado: (@Composable () -> Unit)? = null,
    fila: @Composable (T) -> Unit,
) {
    when {
        cargando && items.isEmpty() -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(strokeWidth = 3.dp)
                Spacer(Modifier.height(16.dp))
                Rotulo("Consultando el servidor", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        error != null && items.isEmpty() -> EstadoVacio(
            icono = Icons.Default.WifiOff,
            titulo = "No se pudo cargar",
            detalle = error,
            alReintentar = alReintentar,
            tonoError = true,
            modifier = modifier,
        )

        items.isEmpty() -> EstadoVacio(
            icono = iconoVacio,
            titulo = tituloVacio,
            detalle = textoVacio,
            alReintentar = alReintentar,
            modifier = modifier,
        )

        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (encabezado != null) {
                item { encabezado() }
            }
            items(items) { fila(it) }
        }
    }
}

@Composable
fun EstadoVacio(
    icono: ImageVector,
    titulo: String,
    detalle: String,
    modifier: Modifier = Modifier,
    tonoError: Boolean = false,
    alReintentar: (() -> Unit)? = null,
) {
    val acento = if (tonoError) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = RoundedCornerShape(18.dp),
            color = acento.copy(alpha = 0.14f),
            contentColor = acento,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icono, contentDescription = null, modifier = Modifier.size(30.dp))
            }
        }

        Spacer(Modifier.height(18.dp))
        Text(titulo, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(6.dp))
        Text(
            detalle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (alReintentar != null) {
            Spacer(Modifier.height(20.dp))
            FilledTonalButton(onClick = alReintentar, shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Reintentar")
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Formularios
// ---------------------------------------------------------------------------

@Composable
fun CampoFormulario(
    etiqueta: String,
    valor: String,
    alCambiar: (String) -> Unit,
    modifier: Modifier = Modifier,
    obligatorio: Boolean = true,
    tipo: KeyboardType = KeyboardType.Text,
    ultimo: Boolean = false,
    apoyo: String? = null,
    icono: ImageVector? = null,
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(if (obligatorio) "$etiqueta *" else etiqueta) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        leadingIcon = icono?.let {
            { Icon(it, contentDescription = null, modifier = Modifier.size(20.dp)) }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = tipo,
            imeAction = if (ultimo) ImeAction.Done else ImeAction.Next,
        ),
        supportingText = apoyo?.let { { Text(it) } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectorOpcion(
    etiqueta: String,
    opciones: List<T>,
    seleccionado: T?,
    textoDe: (T) -> String,
    alSeleccionar: (T) -> Unit,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
) {
    var abierto by remember { mutableStateOf(false) }
    val hayOpciones = opciones.isNotEmpty()

    val borde by animateColorAsState(
        targetValue = if (seleccionado != null) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant
        },
        label = "borde-selector",
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Rotulo(etiqueta, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))

        Box {
            Surface(
                onClick = { abierto = true },
                enabled = hayOpciones,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, borde),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (icono != null) {
                        Icon(
                            icono,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.width(12.dp))
                    }
                    Text(
                        text = seleccionado?.let(textoDe)
                            ?: if (hayOpciones) "Selecciona…" else "Sin opciones disponibles",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (seleccionado != null) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(textoDe(opcion)) },
                        onClick = {
                            alSeleccionar(opcion)
                            abierto = false
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoFormulario(
    titulo: String,
    guardando: Boolean,
    puedeGuardar: Boolean,
    alCerrar: () -> Unit,
    alGuardar: () -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    textoGuardar: String = "Guardar",
    contenido: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = alCerrar,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    TopAppBar(
                        title = { Text(titulo, style = MaterialTheme.typography.titleLarge) },
                        navigationIcon = {
                            IconButton(onClick = alCerrar) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        ),
                    )
                },
            ) { relleno ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(relleno),
                ) {
                    if (guardando) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (error != null) {
                            BannerError(error)
                        }
                        contenido()
                        Spacer(Modifier.height(4.dp))
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextButton(onClick = alCerrar, enabled = !guardando) {
                                Text("Cancelar")
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = alGuardar,
                                enabled = puedeGuardar && !guardando,
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text(textoGuardar)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BannerError(mensaje: String, modifier: Modifier = Modifier) {
    val franja = MaterialTheme.colorScheme.error

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .drawBehind {
                drawRect(color = franja, size = Size(anchoFranja.toPx(), size.height))
            }
            .padding(start = anchoFranja),
    ) {
        Text(
            mensaje,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

@Composable
fun DialogoConfirmar(
    titulo: String,
    mensaje: String,
    alConfirmar: () -> Unit,
    alCerrar: () -> Unit,
    textoConfirmar: String = "Eliminar",
) {
    AlertDialog(
        onDismissRequest = alCerrar,
        icon = {
            Icon(
                Icons.Default.DeleteOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        },
        title = { Text(titulo, style = MaterialTheme.typography.titleLarge) },
        text = { Text(mensaje) },
        shape = RoundedCornerShape(20.dp),
        confirmButton = {
            TextButton(onClick = alConfirmar) {
                Text(textoConfirmar, color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = alCerrar) { Text("Cancelar") }
        },
    )
}
