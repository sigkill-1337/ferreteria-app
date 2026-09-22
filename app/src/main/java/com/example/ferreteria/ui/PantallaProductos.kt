package com.example.ferreteria.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ferreteria.data.Categoria
import com.example.ferreteria.data.Producto
import com.example.ferreteria.data.ProductoRequest
import com.example.ferreteria.data.Proveedor
import java.math.BigDecimal

/**
 * Inventario: lista de productos con alta, edición y baja.
 * Las categorías y proveedores llegan de fuera porque el formulario los
 * necesita como desplegables y ya están cargados en las otras pestañas.
 */
@Composable
fun PantallaProductos(
    vm: ProductosViewModel,
    categorias: List<Categoria>,
    proveedores: List<Proveedor>,
    modifier: Modifier = Modifier,
) {
    val estado by vm.estado.collectAsState()

    var editando by remember { mutableStateOf<Producto?>(null) }
    var creando by remember { mutableStateOf(false) }
    var porBorrar by remember { mutableStateOf<Producto?>(null) }
    var busqueda by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.cargarSiHaceFalta() }

    val visibles = remember(estado.items, busqueda) {
        val texto = busqueda.trim().lowercase()
        if (texto.isEmpty()) {
            estado.items
        } else {
            estado.items.filter {
                it.nombre_producto.lowercase().contains(texto) ||
                    it.nombre_categoria.lowercase().contains(texto) ||
                    it.proveedor.lowercase().contains(texto) ||
                    it.descripcion_producto.orEmpty().lowercase().contains(texto)
            }
        }
    }

    val valorInventario = remember(estado.items) {
        estado.items.fold(BigDecimal.ZERO) { acumulado, producto ->
            acumulado + producto.precio_venta.aDecimal() * BigDecimal(producto.stock)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (estado.items.isNotEmpty()) {
                CampoBusqueda(
                    valor = busqueda,
                    alCambiar = { busqueda = it },
                    marcador = "Buscar por nombre, categoría o proveedor",
                )
            }

            ContenidoLista(
                modifier = Modifier.weight(1f),
                cargando = estado.cargando,
                error = estado.error,
                items = visibles,
                tituloVacio = if (busqueda.isBlank()) "Inventario vacío" else "Sin coincidencias",
                iconoVacio = if (busqueda.isBlank()) Icons.Default.Inventory2 else Icons.Default.SearchOff,
                textoVacio = if (busqueda.isBlank()) {
                    "Todavía no hay productos. Usa el botón de abajo para agregar el primero."
                } else {
                    "Ningún producto coincide con \"$busqueda\"."
                },
                alReintentar = vm::cargar,
                encabezado = if (busqueda.isBlank() && estado.items.isNotEmpty()) {
                    {
                        TarjetaResumen(
                            icono = Icons.Default.Warehouse,
                            etiqueta = "Valor del inventario",
                            valor = valorInventario.comoPesos(),
                            detalle = conteo(estado.items.size, "producto", "productos") + " · " +
                                conteo(estado.items.sumOf { it.stock }, "pieza", "piezas") +
                                " en existencia",
                        )
                    }
                } else {
                    null
                },
            ) { producto ->
                TarjetaRegistro(
                    titulo = producto.nombre_producto,
                    subtitulo = producto.descripcion_producto?.takeIf { it.isNotBlank() },
                    lineas = listOf("Compra ${producto.precio_compra.comoPesos()} · ${producto.proveedor}"),
                    etiquetas = listOf(
                        DatosEtiqueta(producto.nombre_categoria, Tono.ACENTO, Icons.Default.Category),
                        etiquetaStock(producto.stock),
                    ),
                    avatar = DatosAvatar(
                        texto = iniciales(producto.nombre_producto),
                        tono = tonoPorCategoria(producto.id_categoria),
                    ),
                    valor = producto.precio_venta.comoPesos(),
                    valorSecundario = margenDe(producto),
                    alEditar = { editando = producto },
                    alBorrar = { porBorrar = producto },
                )
            }
        }

        ExtendedFloatingActionButton(
            onClick = { creando = true },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Producto") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
        )
    }

    if (creando || editando != null) {
        FormularioProducto(
            producto = editando,
            categorias = categorias,
            proveedores = proveedores,
            guardando = estado.guardando,
            error = estado.error,
            alCerrar = {
                creando = false
                editando = null
                vm.limpiarError()
            },
            alGuardar = { req ->
                vm.guardar(editando?.id_producto, req) {
                    creando = false
                    editando = null
                }
            },
        )
    }

    porBorrar?.let { producto ->
        DialogoConfirmar(
            titulo = "¿Eliminar producto?",
            mensaje = "Se va a borrar \"${producto.nombre_producto}\". " +
                "Si ya aparece en alguna venta, el servidor no lo va a permitir.",
            alConfirmar = {
                vm.eliminar(producto.id_producto)
                porBorrar = null
            },
            alCerrar = { porBorrar = null },
        )
    }
}

/** Stock agotado, bajo o sano — el color hace el trabajo de leerlo. */
private fun etiquetaStock(stock: Int): DatosEtiqueta = when {
    stock == 0 -> DatosEtiqueta("Agotado", Tono.ERROR)
    stock <= 5 -> DatosEtiqueta("Quedan $stock", Tono.ALERTA)
    else -> DatosEtiqueta("Stock $stock", Tono.EXITO)
}

/** Color estable por categoría: la misma categoría siempre se ve igual. */
private fun tonoPorCategoria(idCategoria: Int): Tono {
    val tonos = listOf(Tono.PRIMARIO, Tono.ACENTO, Tono.EXITO, Tono.ALERTA)
    return tonos[(idCategoria - 1).mod(tonos.size)]
}

/** Utilidad por pieza, para tener a la vista si el precio de venta tiene sentido. */
private fun margenDe(producto: Producto): String? {
    val compra = producto.precio_compra.aDecimal()
    val venta = producto.precio_venta.aDecimal()
    if (compra <= BigDecimal.ZERO) return null
    return "margen " + (venta - compra).comoPesos()
}

@Composable
private fun FormularioProducto(
    producto: Producto?,
    categorias: List<Categoria>,
    proveedores: List<Proveedor>,
    guardando: Boolean,
    error: String?,
    alCerrar: () -> Unit,
    alGuardar: (ProductoRequest) -> Unit,
) {
    var nombre by remember { mutableStateOf(producto?.nombre_producto ?: "") }
    var descripcion by remember { mutableStateOf(producto?.descripcion_producto ?: "") }
    var precioCompra by remember { mutableStateOf(producto?.precio_compra ?: "") }
    var precioVenta by remember { mutableStateOf(producto?.precio_venta ?: "") }
    var stock by remember { mutableStateOf(producto?.stock?.toString() ?: "") }
    var categoria by remember {
        mutableStateOf(categorias.firstOrNull { it.id_categoria == producto?.id_categoria })
    }
    var proveedor by remember {
        mutableStateOf(proveedores.firstOrNull { it.id_proveedor == producto?.id_proveedor })
    }

    val completo = nombre.isNotBlank() &&
        esDecimalValido(precioCompra) &&
        esDecimalValido(precioVenta) &&
        stock.toIntOrNull()?.let { it >= 0 } == true &&
        categoria != null &&
        proveedor != null

    DialogoFormulario(
        titulo = if (producto == null) "Nuevo producto" else "Editar producto",
        guardando = guardando,
        puedeGuardar = completo,
        error = error,
        alCerrar = alCerrar,
        alGuardar = {
            alGuardar(
                ProductoRequest(
                    nombre_producto = nombre.trim(),
                    descripcion_producto = descripcion.trim().ifBlank { null },
                    precio_compra = precioCompra.trim(),
                    precio_venta = precioVenta.trim(),
                    stock = stock.toInt(),
                    id_categoria = categoria!!.id_categoria,
                    id_proveedor = proveedor!!.id_proveedor,
                )
            )
        },
    ) {
        TituloSeccion("Identificación")
        CampoFormulario("Nombre", nombre, { nombre = it }, icono = Icons.Default.Inventory2)
        CampoFormulario("Descripción", descripcion, { descripcion = it }, obligatorio = false)

        TituloSeccion("Precios y existencias")
        CampoFormulario(
            "Precio de compra",
            precioCompra,
            { precioCompra = it },
            tipo = KeyboardType.Decimal,
            icono = Icons.Default.AttachMoney,
            apoyo = "Hasta 2 decimales, por ejemplo 90.00",
        )
        CampoFormulario(
            "Precio de venta",
            precioVenta,
            { precioVenta = it },
            tipo = KeyboardType.Decimal,
            icono = Icons.Default.AttachMoney,
            apoyo = margenPrevisto(precioCompra, precioVenta),
        )
        CampoFormulario("Stock", stock, { stock = it }, tipo = KeyboardType.Number)

        TituloSeccion("Clasificación")
        SelectorOpcion(
            etiqueta = "Categoría *",
            opciones = categorias,
            seleccionado = categoria,
            textoDe = { it.nombre_categoria },
            alSeleccionar = { categoria = it },
            icono = Icons.Default.Category,
        )
        SelectorOpcion(
            etiqueta = "Proveedor *",
            opciones = proveedores,
            seleccionado = proveedor,
            textoDe = { it.nombre_empresa },
            alSeleccionar = { proveedor = it },
            icono = Icons.Default.LocalShipping,
        )
    }
}

/** Texto de apoyo bajo el precio de venta mientras se escribe. */
private fun margenPrevisto(compra: String, venta: String): String? {
    if (!esDecimalValido(compra) || !esDecimalValido(venta)) return null
    val diferencia = venta.aDecimal() - compra.aDecimal()
    return when {
        diferencia < BigDecimal.ZERO -> "Estarías vendiendo por debajo del costo"
        else -> "Ganas ${diferencia.comoPesos()} por pieza"
    }
}

/** Acepta "150", "150.5", "150.00"; rechaza texto y más de 2 decimales. */
fun esDecimalValido(texto: String): Boolean =
    Regex("""^\d{1,8}(\.\d{1,2})?$""").matches(texto.trim())
