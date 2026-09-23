package com.example.ferreteria.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ferreteria.data.ClienteVigente
import com.example.ferreteria.data.PersonaDirectorio
import com.example.ferreteria.data.ProductoTop
import com.example.ferreteria.data.SeguimientoCliente
import com.example.ferreteria.data.VentaDelDia
import com.example.ferreteria.data.VentasPorCanal
import com.example.ferreteria.ui.theme.TemaFerreteria

/**
 * Reportes: es la parte de la app que enseña lo que vive en la base de datos y
 * no en el código — los dos procedimientos almacenados, las consultas agrupadas,
 * el UNION y la bitácora que llena el trigger.
 *
 * Cada reporte dice de dónde sale, para poder señalarlo al presentarlo.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PantallaReportes(
    vm: ReportesViewModel,
    modifier: Modifier = Modifier,
) {
    val estado by vm.estado.collectAsState()

    LaunchedEffect(Unit) { vm.cargarSiHaceFalta() }

    Column(modifier = modifier.fillMaxSize()) {
        FlowRow(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TipoReporte.entries.forEach { tipo ->
                ChipSeleccion(
                    texto = tipo.etiqueta,
                    activo = estado.tipo == tipo,
                    alTocar = { vm.seleccionar(tipo) },
                )
            }
        }

        Text(
            estado.tipo.descripcion,
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        when (estado.tipo) {
            TipoReporte.VENTAS_DIA -> SelectorFecha(
                etiqueta = "Fecha",
                fecha = estado.fecha,
                alCambiar = vm::cambiarFecha,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            TipoReporte.CLIENTES_TRIMESTRE -> Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SelectorTrimestre(
                    etiqueta = "Trimestre",
                    trimestre = estado.trimestre,
                    alCambiar = vm::cambiarTrimestre,
                )
                SelectorAnio(
                    etiqueta = "Año",
                    anio = estado.anio,
                    alCambiar = vm::cambiarAnio,
                )
            }

            else -> Spacer(Modifier.height(8.dp))
        }

        Box(modifier = Modifier.weight(1f)) {
            val vacio = estaVacio(estado)

            when {
                estado.cargando && vacio -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

                estado.error != null && vacio -> EstadoVacio(
                    icono = Icons.AutoMirrored.Filled.TrendingUp,
                    titulo = "No se pudo generar el reporte",
                    detalle = estado.error!!,
                    tonoError = true,
                    alReintentar = vm::cargar,
                )

                else -> ListaReporte(estado)
            }
        }
    }
}

/** true cuando el reporte activo todavía no tiene nada que dibujar. */
private fun estaVacio(estado: EstadoReportes): Boolean = when (estado.tipo) {
    TipoReporte.VENTAS_DIA -> estado.ventasDia == null
    TipoReporte.CLIENTES_TRIMESTRE -> estado.reporteTrimestre == null
    TipoReporte.TOP_PRODUCTOS -> estado.topProductos.isEmpty()
    TipoReporte.CANALES -> estado.canales.isEmpty()
    TipoReporte.DIRECTORIO -> estado.directorio.isEmpty()
    TipoReporte.SEGUIMIENTO -> estado.seguimiento.isEmpty()
}

@Composable
private fun ListaReporte(estado: EstadoReportes) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        when (estado.tipo) {
            TipoReporte.VENTAS_DIA -> {
                val reporte = estado.ventasDia ?: return@LazyColumn

                item {
                    TarjetaResumen(
                        icono = Icons.Default.PointOfSale,
                        etiqueta = "Vendido el ${reporte.fecha.fechaCortaLegible()}",
                        valor = reporte.monto_total.comoPesos(),
                        detalle = conteo(reporte.num_ventas, "venta", "ventas") +
                            " · ticket promedio " + reporte.ticket_promedio.comoPesos() +
                            " · mayor " + reporte.venta_mayor.comoPesos(),
                    )
                }

                if (reporte.ventas.isEmpty()) {
                    item { MensajeSinDatos("No hubo ventas ese día.") }
                } else {
                    items(reporte.ventas) { FilaVentaDelDia(it) }
                }
            }

            TipoReporte.CLIENTES_TRIMESTRE -> {
                val reporte = estado.reporteTrimestre ?: return@LazyColumn

                item {
                    TarjetaResumen(
                        icono = Icons.Default.Groups,
                        etiqueta = "Clientes con pedidos",
                        valor = reporte.clientes.size.toString(),
                        detalle = "Periodo ${reporte.periodo}",
                    )
                }

                if (reporte.clientes.isEmpty()) {
                    item { MensajeSinDatos("Ningún cliente compró en ese trimestre.") }
                } else {
                    items(reporte.clientes) { FilaClienteVigente(it) }
                }
            }

            TipoReporte.TOP_PRODUCTOS ->
                itemsIndexed(estado.topProductos) { indice, producto ->
                    FilaProductoTop(indice + 1, producto)
                }

            TipoReporte.CANALES -> {
                val mayor = estado.canales.maxOfOrNull { it.porcentaje } ?: 100.0
                items(estado.canales) { FilaCanal(it, mayor) }
            }

            TipoReporte.DIRECTORIO -> items(estado.directorio) { FilaDirectorio(it) }

            TipoReporte.SEGUIMIENTO -> {
                if (estado.seguimiento.isEmpty()) {
                    item {
                        MensajeSinDatos(
                            "Aquí aparecen los clientes que compran desde la app o el " +
                                "sitio web, para darles seguimiento."
                        )
                    }
                } else {
                    items(estado.seguimiento) { FilaSeguimiento(it) }
                }
            }
        }
    }
}

@Composable
private fun FilaVentaDelDia(venta: VentaDelDia) {
    TarjetaRegistro(
        titulo = venta.cliente,
        acento = colorDeCanal(venta.canal),
        clave = folioDe(venta.id_venta) + "  ·  " + venta.hora,
        lineas = listOf("Atendió ${venta.empleado}"),
        etiquetas = listOf(
            etiquetaCanal(venta.canal),
            DatosEtiqueta(conteo(venta.piezas, "pieza", "piezas"), Tono.NEUTRO),
        ),
        valor = venta.total.comoPesos(),
    )
}

@Composable
private fun FilaClienteVigente(cliente: ClienteVigente) {
    TarjetaRegistro(
        titulo = cliente.cliente,
        subtitulo = cliente.email_cliente,
        lineas = listOf(
            "Última compra: ${cliente.ultima_compra.fechaLegible()}",
        ),
        etiquetas = listOf(
            DatosEtiqueta(conteo(cliente.pedidos, "pedido", "pedidos"), Tono.ACENTO),
            DatosEtiqueta("hace ${cliente.dias_desde_ultima} d", Tono.NEUTRO),
        ),
        avatar = DatosAvatar(
            texto = iniciales(cliente.cliente),
            color = TemaFerreteria.acentoDe(cliente.id_cliente),
        ),
        acento = TemaFerreteria.acentoDe(cliente.id_cliente),
        clave = claveDe(cliente.id_cliente),
        valor = cliente.monto_total.comoPesos(),
        valorSecundario = "en el trimestre",
    )
}

@Composable
private fun FilaProductoTop(lugar: Int, producto: ProductoTop) {
    TarjetaRegistro(
        titulo = producto.nombre_producto,
        acento = TemaFerreteria.acentoDe(producto.id_producto),
        clave = "LUGAR $lugar  ·  " + claveDe(producto.id_producto),
        subtitulo = producto.nombre_categoria,
        lineas = listOf(
            "Aparece en ${conteo(producto.aparece_en_ventas, "venta", "ventas")}",
        ),
        etiquetas = listOf(
            DatosEtiqueta(
                conteo(producto.piezas_vendidas, "pieza vendida", "piezas vendidas"),
                if (lugar == 1) Tono.EXITO else Tono.NEUTRO,
            ),
        ),
        valor = producto.importe_vendido.comoPesos(),
    )
}

@Composable
private fun FilaCanal(canal: VentasPorCanal, porcentajeMayor: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    iconoDeCanal(canal.canal),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        nombreDeCanal(canal.canal),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        conteo(canal.num_ventas, "venta", "ventas"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        canal.monto_total.comoPesos(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        "${canal.porcentaje}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // La barra se normaliza contra el canal más grande, no contra 100:
            // así se ve la diferencia entre canales aunque ninguno domine.
            LinearProgressIndicator(
                progress = {
                    if (porcentajeMayor <= 0.0) 0f
                    else (canal.porcentaje / porcentajeMayor).toFloat().coerceIn(0f, 1f)
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun FilaDirectorio(persona: PersonaDirectorio) {
    val esEmpleado = persona.tipo.equals("Empleado", ignoreCase = true)

    TarjetaRegistro(
        titulo = persona.nombre,
        subtitulo = persona.email,
        etiquetas = listOfNotNull(
            DatosEtiqueta(persona.tipo, if (esEmpleado) Tono.EXITO else Tono.PRIMARIO),
            persona.detalle.takeIf { it.isNotBlank() }?.let { DatosEtiqueta(it, Tono.ACENTO) },
            DatosEtiqueta(persona.telefono, Tono.NEUTRO),
        ),
        avatar = DatosAvatar(
            texto = iniciales(persona.nombre),
            tono = if (esEmpleado) Tono.EXITO else Tono.PRIMARIO,
        ),
        acento = if (esEmpleado) {
            TemaFerreteria.estado.exito
        } else {
            MaterialTheme.colorScheme.primary
        },
    )
}

@Composable
private fun FilaSeguimiento(registro: SeguimientoCliente) {
    TarjetaRegistro(
        titulo = registro.nombre_cliente,
        clave = registro.id_venta?.let { folioDe(it) } ?: "SIN VENTA",
        subtitulo = registro.fecha_legible,
        lineas = listOfNotNull(
            registro.id_venta?.let { "Venta #$it" }
                ?: "La venta fue cancelada; el registro se conserva",
        ),
        etiquetas = listOf(
            etiquetaCanal(registro.canal),
            if (registro.atendido == 1) {
                DatosEtiqueta("Atendido", Tono.EXITO)
            } else {
                DatosEtiqueta("Pendiente", Tono.ALERTA)
            },
        ),
        icono = Icons.Default.NotificationsActive,
        acento = colorDeCanal(registro.canal),
        valor = registro.total_compra.comoPesos(),
    )
}

@Composable
private fun MensajeSinDatos(texto: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Text(
            texto,
            modifier = Modifier.padding(20.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChipSeleccion(
    texto: String,
    activo: Boolean,
    alTocar: () -> Unit,
) {
    Surface(
        onClick = alTocar,
        shape = RoundedCornerShape(50),
        color = if (activo) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        contentColor = if (activo) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
    ) {
        Text(
            texto,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

fun etiquetaCanal(canal: String): DatosEtiqueta = DatosEtiqueta(
    texto = nombreDeCanal(canal),
    tono = when (canal.uppercase()) {
        "APP" -> Tono.PRIMARIO
        "WEB" -> Tono.ACENTO
        else -> Tono.NEUTRO
    },
    icono = iconoDeCanal(canal),
)

/** Color de la franja según el canal por el que entró la venta. */
@Composable
fun colorDeCanal(canal: String): Color = when (canal.uppercase()) {
    "APP" -> MaterialTheme.colorScheme.primary
    "WEB" -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.outline
}

fun nombreDeCanal(canal: String): String = when (canal.uppercase()) {
    "APP" -> "App móvil"
    "WEB" -> "Sitio web"
    else -> "Mostrador"
}

private fun iconoDeCanal(canal: String) = when (canal.uppercase()) {
    "APP" -> Icons.Default.PointOfSale
    "WEB" -> Icons.AutoMirrored.Filled.TrendingUp
    else -> Icons.Default.Storefront
}

/** "2026-08-10" -> "10 ago 2026", sin hora. */
private fun String.fechaCortaLegible(): String = "$this 00:00:00".fechaLegible().substringBefore(",")
