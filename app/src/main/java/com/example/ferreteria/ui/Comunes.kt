package com.example.ferreteria.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ferreteria.ui.theme.TemaFerreteria

/**
 * Piezas de UI compartidas. Todas las pantallas se arman con estas, así el
 * catálogo, el inventario y las ventas se ven como la misma app.
 */

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

data class DatosEtiqueta(
    val texto: String,
    val tono: Tono = Tono.NEUTRO,
    val icono: ImageVector? = null,
)

/** Píldora de color para datos cortos: categoría, puesto, stock, estado. */
@Composable
fun Etiqueta(datos: DatosEtiqueta, modifier: Modifier = Modifier) {
    val (fondo, texto) = coloresDeTono(datos.tono)

    Surface(
        modifier = modifier,
        color = fondo,
        contentColor = texto,
        shape = RoundedCornerShape(50),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (datos.icono != null) {
                Icon(
                    datos.icono,
                    contentDescription = null,
                    // El icono no puede encogerse: si lo hace, el texto de al
                    // lado se queda sin ancho y se parte letra por letra.
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                datos.texto,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
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
)

/** Círculo con iniciales o icono, para anclar visualmente cada renglón. */
@Composable
fun Avatar(datos: DatosAvatar, modifier: Modifier = Modifier) {
    val (fondo, contenido) = coloresDeTono(datos.tono)

    Surface(
        modifier = modifier.size(44.dp),
        color = fondo,
        contentColor = contenido,
        shape = CircleShape,
    ) {
        Box(contentAlignment = Alignment.Center) {
            when {
                datos.icono != null -> Icon(
                    datos.icono,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
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

/** Iniciales de un nombre: "Ana López" -> "AL". */
fun iniciales(vararg partes: String?): String =
    partes.filter { !it.isNullOrBlank() }
        .take(2)
        .joinToString("") { it!!.trim().take(1) }
        .uppercase()

/** Buscador de la lista. Filtra en memoria: los catálogos son chicos. */
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
            .padding(horizontal = 12.dp, vertical = 4.dp),
        placeholder = { Text(marcador) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (valor.isNotEmpty()) {
                IconButton(onClick = { alCambiar("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    )
}

/**
 * Cuerpo de una lista: resuelve cargando, error, vacío y contenido.
 * [encabezado] se dibuja como primer elemento y se desplaza con la lista.
 */
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
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text(
                    "Consultando el servidor…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (encabezado != null) {
                item { encabezado() }
            }
            items(items) { fila(it) }
        }
    }
}

/** Pantalla de estado: nada que mostrar, o algo falló. */
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
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            color = if (tonoError) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            },
            contentColor = acento,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icono, contentDescription = null, modifier = Modifier.size(34.dp))
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(titulo, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            detalle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (alReintentar != null) {
            Spacer(Modifier.height(20.dp))
            FilledTonalButton(onClick = alReintentar) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Reintentar")
            }
        }
    }
}

/**
 * Renglón de cualquier catálogo.
 *
 * Estructura: avatar a la izquierda, texto al centro con sus etiquetas, y a la
 * derecha el valor destacado con los botones de editar y borrar debajo.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TarjetaRegistro(
    titulo: String,
    modifier: Modifier = Modifier,
    subtitulo: String? = null,
    lineas: List<String> = emptyList(),
    etiquetas: List<DatosEtiqueta> = emptyList(),
    avatar: DatosAvatar? = null,
    valor: String? = null,
    valorSecundario: String? = null,
    alEditar: (() -> Unit)? = null,
    alBorrar: (() -> Unit)? = null,
    alTocar: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = alTocar ?: {},
        enabled = alTocar != null,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, top = 14.dp, end = 8.dp, bottom = 10.dp),
        ) {
            if (avatar != null) {
                Avatar(avatar)
                Spacer(Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
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
                    // FlowRow y no Row: con Row, dos etiquetas largas se reparten
                    // el ancho a la fuerza y el texto acaba partido letra por
                    // letra. Así la que no cabe se baja al siguiente renglón.
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
                // Sin tope, un valor secundario largo ("atendió Fulano de Tal")
                // le come el ancho a la columna del título y las etiquetas.
                modifier = Modifier
                    .padding(start = 8.dp)
                    .widthIn(max = 130.dp),
            ) {
                if (valor != null) {
                    Text(
                        valor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                    )
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
                    Row {
                        if (alEditar != null) {
                            IconButton(onClick = alEditar, modifier = Modifier.size(38.dp)) {
                                Icon(
                                    Icons.Default.EditNote,
                                    contentDescription = "Editar",
                                    modifier = Modifier.size(21.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        if (alBorrar != null) {
                            IconButton(onClick = alBorrar, modifier = Modifier.size(38.dp)) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = "Eliminar",
                                    modifier = Modifier.size(21.dp),
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Tarjeta de resumen que va arriba de una lista. */
@Composable
fun TarjetaResumen(
    icono: ImageVector,
    etiqueta: String,
    valor: String,
    detalle: String? = null,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(30.dp))
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(etiqueta, style = MaterialTheme.typography.labelLarge)
                Text(
                    valor,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                if (detalle != null) {
                    Text(detalle, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

/** Título de bloque dentro de un formulario. */
@Composable
fun TituloSeccion(texto: String, modifier: Modifier = Modifier) {
    Text(
        texto.uppercase(),
        modifier = modifier.padding(top = 8.dp),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
    )
}

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
        shape = RoundedCornerShape(14.dp),
        leadingIcon = icono?.let { { Icon(it, contentDescription = null) } },
        keyboardOptions = KeyboardOptions(
            keyboardType = tipo,
            imeAction = if (ultimo) ImeAction.Done else ImeAction.Next,
        ),
        supportingText = apoyo?.let { { Text(it) } },
    )
}

/** Desplegable de selección sobre una lista de opciones. */
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
        Text(
            etiqueta,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))

        Box {
            Surface(
                onClick = { abierto = true },
                enabled = hayOpciones,
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, borde),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
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

/**
 * Formulario a pantalla completa. Se usa como diálogo para no tener que armar
 * un grafo de navegación solo para las altas y ediciones.
 */
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
                        title = { Text(titulo) },
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
                                shape = RoundedCornerShape(14.dp),
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

/** Franja roja con el mensaje que devolvió la API. */
@Composable
fun BannerError(mensaje: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Text(
            mensaje,
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/** Confirmación antes de borrar. Nunca se borra con un solo toque. */
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
        title = { Text(titulo) },
        text = { Text(mensaje) },
        shape = RoundedCornerShape(24.dp),
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
