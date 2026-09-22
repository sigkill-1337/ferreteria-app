package com.example.ferreteria.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ferreteria.data.Cliente
import com.example.ferreteria.data.Empleado
import com.example.ferreteria.data.Producto
import com.example.ferreteria.data.Venta
import com.example.ferreteria.data.VentaItemRequest
import java.math.BigDecimal

/** Los mismos valores que acepta la columna VENTA.canal en la base de datos. */
private val CANALES = listOf("APP", "WEB", "MOSTRADOR")

/** "V-0007" — folio de venta con ceros a la izquierda. */
fun folioDe(idVenta: Int): String = "V-" + idVenta.toString().padStart(4, '0')

/**
 * Ventas: historial, ticket de una venta, alta de venta nueva, edición y
 * cancelación (que devuelve el stock al inventario).
 */
@Composable
fun PantallaVentas(
    vm: VentasViewModel,
    productos: List<Producto>,
    clientes: List<Cliente>,
    empleados: List<Empleado>,
    alCambiarInventario: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val estado by vm.estado.collectAsState()

    var creando by remember { mutableStateOf(false) }
    var editando by remember { mutableStateOf<Venta?>(null) }
    var porCancelar by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) { vm.cargarSiHaceFalta() }

    val totalVendido = remember(estado.ventas) {
        estado.ventas.fold(BigDecimal.ZERO) { acumulado, venta -> acumulado + venta.total.aDecimal() }
    }

    Box(modifier = modifier.fillMaxSize()) {
        ContenidoLista(
            cargando = estado.cargando,
            error = estado.error,
            items = estado.ventas,
            tituloVacio = "Sin ventas",
            iconoVacio = Icons.Default.PointOfSale,
            textoVacio = "Todavía no hay ventas registradas. Usa el botón de abajo para hacer la primera.",
            alReintentar = vm::cargar,
            encabezado = if (estado.ventas.isNotEmpty()) {
                {
                    TarjetaResumen(
                        icono = Icons.Default.PointOfSale,
                        etiqueta = "Total vendido",
                        valor = totalVendido.comoPesos(),
                        detalle = conteo(estado.ventas.size, "venta registrada", "ventas registradas"),
                    )
                }
            } else {
                null
            },
        ) { venta ->
            TarjetaRegistro(
                // El folio va como clave monoespaciada, no como título: lo que
                // importa leer primero es quién compró y cuánto.
                titulo = nombreCompleto(venta.nombre_cliente, venta.ap_paterno_cliente),
                acento = colorDeCanal(venta.canal),
                clave = folioDe(venta.id_venta) + "  ·  " + venta.fecha.fechaLegible(),
                etiquetas = listOf(
                    etiquetaCanal(venta.canal),
                    DatosEtiqueta(
                        conteo(venta.num_productos, "producto", "productos"),
                        Tono.NEUTRO,
                        Icons.Default.ShoppingBag,
                    ),
                ),
                valor = venta.total.comoPesos(),
                valorSecundario = "atendió ${venta.nombre_empleado}",
                alTocar = { vm.abrirDetalle(venta.id_venta) },
                alBorrar = { porCancelar = venta.id_venta },
            )
        }

        ExtendedFloatingActionButton(
            onClick = { creando = true },
            icon = { Icon(Icons.Default.ShoppingCartCheckout, contentDescription = null) },
            text = { Text("Nueva venta") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
        )

        if (estado.cargandoDetalle) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }

    estado.detalle?.let { venta ->
        DialogoTicket(
            venta = venta,
            alEditar = {
                editando = venta
                vm.cerrarDetalle()
            },
            alCancelarVenta = {
                porCancelar = venta.id_venta
                vm.cerrarDetalle()
            },
            alCerrar = vm::cerrarDetalle,
        )
    }

    if (creando || editando != null) {
        FormularioVenta(
            venta = editando,
            productos = productos,
            clientes = clientes,
            empleados = empleados,
            guardando = estado.guardando,
            error = estado.error,
            alCerrar = {
                creando = false
                editando = null
                vm.limpiarError()
            },
            alGuardar = { idCliente, idEmpleado, canal, lineas ->
                vm.guardarVenta(editando?.id_venta, idCliente, idEmpleado, canal, lineas) {
                    creando = false
                    editando = null
                    alCambiarInventario()
                }
            },
        )
    }

    porCancelar?.let { idVenta ->
        DialogoConfirmar(
            titulo = "¿Cancelar la venta ${folioDe(idVenta)}?",
            mensaje = "Se borra la venta y su detalle, y el stock de esos productos " +
                "regresa al inventario. No se puede deshacer.",
            textoConfirmar = "Cancelar venta",
            alConfirmar = {
                vm.cancelarVenta(idVenta) { alCambiarInventario() }
                porCancelar = null
            },
            alCerrar = { porCancelar = null },
        )
    }
}

/** Ticket de una venta ya registrada, a pantalla completa. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoTicket(
    venta: Venta,
    alEditar: () -> Unit,
    alCancelarVenta: () -> Unit,
    alCerrar: () -> Unit,
) {
    Dialog(
        onDismissRequest = alCerrar,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    TopAppBar(
                        title = { Text(folioDe(venta.id_venta), style = MaterialTheme.typography.titleLarge) },
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
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            ),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                DatoTicket(
                                    Icons.Default.CalendarMonth,
                                    "Fecha",
                                    venta.fecha.fechaLegible(),
                                )
                                Spacer(Modifier.height(10.dp))
                                DatoTicket(
                                    Icons.Default.ShoppingBag,
                                    "Cliente",
                                    nombreCompleto(
                                        venta.nombre_cliente,
                                        venta.ap_paterno_cliente,
                                        venta.ap_materno_cliente,
                                    ),
                                )
                                Spacer(Modifier.height(10.dp))
                                DatoTicket(
                                    Icons.Default.Storefront,
                                    "Atendió",
                                    nombreCompleto(
                                        venta.nombre_empleado,
                                        venta.ap_paterno_empleado,
                                    ),
                                )
                            }
                        }

                        TituloSeccion("Detalle")

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            ),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                venta.productos.forEachIndexed { indice, linea ->
                                    if (indice > 0) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant,
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                linea.nombre_producto,
                                                style = MaterialTheme.typography.bodyLarge,
                                            )
                                            Text(
                                                "${linea.cantidad} × ${linea.precio_unitario.comoPesos()}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        Text(
                                            linea.subtotal.comoPesos(),
                                            style = MaterialTheme.typography.titleSmall,
                                        )
                                    }
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "Total",
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    venta.total.comoPesos(),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextButton(onClick = alCancelarVenta) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.error,
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Cancelar venta", color = MaterialTheme.colorScheme.error)
                            }
                            Spacer(Modifier.weight(1f))
                            Button(onClick = alEditar, shape = RoundedCornerShape(14.dp)) {
                                Text("Editar")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DatoTicket(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    etiqueta: String,
    valor: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icono,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(12.dp))
        Text(
            etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        Text(
            valor,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * Alta/edición de venta. El carrito vive aquí como estado local: solo se manda
 * id_producto y cantidad, el servidor calcula precios y total.
 *
 * Al editar, el tope de cada producto es su stock actual más lo que esa misma
 * venta ya tenía apartado, porque el servidor repone el detalle viejo antes de
 * validar el nuevo.
 */
@Composable
private fun FormularioVenta(
    venta: Venta?,
    productos: List<Producto>,
    clientes: List<Cliente>,
    empleados: List<Empleado>,
    guardando: Boolean,
    error: String?,
    alCerrar: () -> Unit,
    alGuardar: (Int, Int, String, List<VentaItemRequest>) -> Unit,
) {
    val cantidadesOriginales = remember(venta) {
        venta?.productos?.associate { it.id_producto to it.cantidad } ?: emptyMap()
    }

    val carrito = remember(venta) {
        mutableStateMapOf<Int, Int>().apply { putAll(cantidadesOriginales) }
    }

    var cliente by remember(venta) {
        mutableStateOf(clientes.firstOrNull { it.id_cliente == venta?.id_cliente })
    }
    var empleado by remember(venta) {
        mutableStateOf(empleados.firstOrNull { it.id_empleado == venta?.id_empleado })
    }
    // Una venta capturada desde el telefono es canal APP; se puede cambiar para
    // registrar las que entraron por el sitio web o en el mostrador.
    var canal by remember(venta) { mutableStateOf(venta?.canal ?: "APP") }

    fun topeDe(producto: Producto): Int =
        producto.stock + (cantidadesOriginales[producto.id_producto] ?: 0)

    val enCarrito = productos.filter { carrito.containsKey(it.id_producto) }
    val disponibles = productos.filter { !carrito.containsKey(it.id_producto) && topeDe(it) > 0 }

    val total = enCarrito.fold(BigDecimal.ZERO) { acumulado, producto ->
        val cantidad = carrito[producto.id_producto] ?: 0
        acumulado + producto.precio_venta.aDecimal() * BigDecimal(cantidad)
    }

    val piezas = enCarrito.sumOf { carrito[it.id_producto] ?: 0 }
    val sinStock = enCarrito.any { (carrito[it.id_producto] ?: 0) > topeDe(it) }
    val completo = cliente != null && empleado != null && carrito.isNotEmpty() && !sinStock

    DialogoFormulario(
        titulo = if (venta == null) "Nueva venta" else "Editar ${folioDe(venta.id_venta)}",
        guardando = guardando,
        puedeGuardar = completo,
        error = error,
        textoGuardar = if (venta == null) "Registrar venta" else "Guardar cambios",
        alCerrar = alCerrar,
        alGuardar = {
            alGuardar(
                cliente!!.id_cliente,
                empleado!!.id_empleado,
                canal,
                carrito.map { (idProducto, cantidad) ->
                    VentaItemRequest(id_producto = idProducto, cantidad = cantidad)
                },
            )
        },
    ) {
        TituloSeccion("Quiénes")
        SelectorOpcion(
            etiqueta = "Cliente *",
            opciones = clientes,
            seleccionado = cliente,
            textoDe = { nombreCompleto(it.nombre_cliente, it.ap_paterno_cliente) },
            alSeleccionar = { cliente = it },
            icono = Icons.Default.ShoppingBag,
        )
        SelectorOpcion(
            etiqueta = "Empleado que atiende *",
            opciones = empleados,
            seleccionado = empleado,
            textoDe = { nombreCompleto(it.nombre_empleado, it.ap_paterno_empleado) },
            alSeleccionar = { empleado = it },
            icono = Icons.Default.Storefront,
        )
        SelectorOpcion(
            etiqueta = "Canal de compra",
            opciones = CANALES,
            seleccionado = canal,
            textoDe = { nombreDeCanal(it) },
            alSeleccionar = { canal = it },
            icono = Icons.Default.PointOfSale,
        )
        Text(
            "Las ventas por app o sitio web entran a la bitácora de seguimiento; " +
                "las de mostrador no.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        TituloSeccion("Carrito")

        if (enCarrito.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "El carrito está vacío. Agrega productos abajo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        enCarrito.forEach { producto ->
            LineaCarrito(
                producto = producto,
                cantidad = carrito[producto.id_producto] ?: 0,
                tope = topeDe(producto),
                alCambiarCantidad = { carrito[producto.id_producto] = it },
                alQuitar = { carrito.remove(producto.id_producto) },
            )
        }

        SelectorOpcion(
            etiqueta = "Agregar producto",
            opciones = disponibles,
            seleccionado = null,
            textoDe = { "${it.nombre_producto} — ${it.precio_venta.comoPesos()} (stock ${it.stock})" },
            alSeleccionar = { carrito[it.id_producto] = 1 },
            icono = Icons.Default.AddCircleOutline,
        )

        if (sinStock) {
            BannerError("Hay productos con cantidad mayor al stock disponible.")
        }

        TituloSeccion("Total")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        conteo(piezas, "pieza", "piezas"),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        total.comoPesos(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "El servidor recalcula precios y total al guardar; este monto es referencia.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

/** Renglón del carrito con su selector de cantidad. */
@Composable
private fun LineaCarrito(
    producto: Producto,
    cantidad: Int,
    tope: Int,
    alCambiarCantidad: (Int) -> Unit,
    alQuitar: () -> Unit,
) {
    val excedido = cantidad > tope

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        producto.nombre_producto,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "${producto.precio_venta.comoPesos()} c/u",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = alQuitar) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Quitar del carrito",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { if (cantidad > 1) alCambiarCantidad(cantidad - 1) },
                    enabled = cantidad > 1,
                ) {
                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Quitar una pieza")
                }

                Text(
                    cantidad.toString(),
                    modifier = Modifier.width(44.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )

                IconButton(
                    onClick = { if (cantidad < tope) alCambiarCantidad(cantidad + 1) },
                    enabled = cantidad < tope,
                ) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Agregar una pieza")
                }

                Spacer(Modifier.width(8.dp))

                Etiqueta(
                    DatosEtiqueta(
                        texto = if (excedido) "Solo hay $tope" else "Disponible $tope",
                        tono = if (excedido) Tono.ERROR else Tono.EXITO,
                    ),
                )

                Spacer(Modifier.weight(1f))

                Text(
                    (producto.precio_venta.aDecimal() * BigDecimal(cantidad)).comoPesos(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
