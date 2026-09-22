package com.example.ferreteria.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VpnKeyOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ferreteria.data.ApiClient
import com.example.ferreteria.ui.theme.TemaFerreteria

/** Pestañas de la barra inferior. */
private enum class Pestana(val etiqueta: String, val icono: ImageVector) {
    PRODUCTOS("Inventario", Icons.Default.Inventory2),
    VENTAS("Ventas", Icons.Default.PointOfSale),
    CLIENTES("Clientes", Icons.Default.People),
    REPORTES("Reportes", Icons.Default.Assessment),
    MAS("Más", Icons.Default.MoreHoriz),
}

/** Pantallas que viven dentro de la pestaña "Más". */
private enum class SubPantalla(val titulo: String) {
    EMPLEADOS("Empleados"),
    CATEGORIAS("Categorías"),
    PROVEEDORES("Proveedores"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppFerreteria() {
    if (ApiClient.faltaApiKey) {
        EstadoVacio(
            icono = Icons.Default.VpnKeyOff,
            titulo = "Falta configurar la aplicación",
            detalle = "Agrega FERRETERIA_API_KEY=<tu key> en local.properties " +
                "y vuelve a compilar la app.",
            tonoError = true,
        )
        return
    }

    val productosVm: ProductosViewModel = viewModel()
    val clientesVm: ClientesViewModel = viewModel()
    val empleadosVm: EmpleadosViewModel = viewModel()
    val categoriasVm: CategoriasViewModel = viewModel()
    val proveedoresVm: ProveedoresViewModel = viewModel()
    val ventasVm: VentasViewModel = viewModel()
    val reportesVm: ReportesViewModel = viewModel()

    val productos by productosVm.estado.collectAsState()
    val clientes by clientesVm.estado.collectAsState()
    val empleados by empleadosVm.estado.collectAsState()
    val categorias by categoriasVm.estado.collectAsState()
    val proveedores by proveedoresVm.estado.collectAsState()
    val ventas by ventasVm.estado.collectAsState()

    var pestana by remember { mutableStateOf(Pestana.PRODUCTOS) }
    var subPantalla by remember { mutableStateOf<SubPantalla?>(null) }

    val snackbar = remember { SnackbarHostState() }

    // Los catálogos se cargan de entrada porque los formularios de producto y
    // de venta los necesitan como desplegables, sin importar en qué pestaña estés.
    LaunchedEffect(Unit) {
        productosVm.cargarSiHaceFalta()
        clientesVm.cargarSiHaceFalta()
        empleadosVm.cargarSiHaceFalta()
        categoriasVm.cargarSiHaceFalta()
        proveedoresVm.cargarSiHaceFalta()
    }

    // El error solo va al snackbar cuando la lista ya tiene contenido: si está
    // vacía, la propia pantalla lo muestra con un botón de reintentar y no hay
    // que borrarlo cuando el snackbar se va.
    Aviso(productos.aviso, productos.error.siHayContenido(productos.items), productosVm::limpiarAviso, productosVm::limpiarError, snackbar)
    Aviso(clientes.aviso, clientes.error.siHayContenido(clientes.items), clientesVm::limpiarAviso, clientesVm::limpiarError, snackbar)
    Aviso(empleados.aviso, empleados.error.siHayContenido(empleados.items), empleadosVm::limpiarAviso, empleadosVm::limpiarError, snackbar)
    Aviso(categorias.aviso, categorias.error.siHayContenido(categorias.items), categoriasVm::limpiarAviso, categoriasVm::limpiarError, snackbar)
    Aviso(proveedores.aviso, proveedores.error.siHayContenido(proveedores.items), proveedoresVm::limpiarAviso, proveedoresVm::limpiarError, snackbar)
    Aviso(ventas.aviso, ventas.error.siHayContenido(ventas.ventas), ventasVm::limpiarAviso, ventasVm::limpiarError, snackbar)

    BackHandler(enabled = subPantalla != null) { subPantalla = null }

    val titulo = subPantalla?.titulo ?: pestana.etiqueta

    val subtitulo = when (subPantalla) {
        SubPantalla.EMPLEADOS -> conteo(empleados.items.size, "empleado", "empleados")
        SubPantalla.CATEGORIAS -> conteo(categorias.items.size, "categoría", "categorías")
        SubPantalla.PROVEEDORES -> conteo(proveedores.items.size, "proveedor", "proveedores")
        null -> when (pestana) {
            Pestana.PRODUCTOS -> conteo(productos.items.size, "producto", "productos")
            Pestana.VENTAS -> conteo(ventas.ventas.size, "venta", "ventas")
            Pestana.CLIENTES -> conteo(clientes.items.size, "cliente", "clientes")
            Pestana.REPORTES -> "Resumen del negocio"
            Pestana.MAS -> "Personal y catálogos"
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(titulo)
                        Text(
                            subtitulo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    if (subPantalla != null) {
                        IconButton(onClick = { subPantalla = null }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Regresar",
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            when (subPantalla) {
                                SubPantalla.EMPLEADOS -> empleadosVm.cargar()
                                SubPantalla.CATEGORIAS -> categoriasVm.cargar()
                                SubPantalla.PROVEEDORES -> proveedoresVm.cargar()
                                null -> when (pestana) {
                                    Pestana.PRODUCTOS -> productosVm.cargar()
                                    Pestana.VENTAS -> ventasVm.cargar()
                                    Pestana.CLIENTES -> clientesVm.cargar()
                                    Pestana.REPORTES -> reportesVm.cargar()
                                    Pestana.MAS -> Unit
                                }
                            }
                        },
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                Pestana.entries.forEach { destino ->
                    val activa = pestana == destino && subPantalla == null
                    NavigationBarItem(
                        selected = activa,
                        onClick = {
                            pestana = destino
                            subPantalla = null
                        },
                        icon = { Icon(destino.icono, contentDescription = null) },
                        label = { Text(destino.etiqueta) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { relleno ->
        val modificador = Modifier
            .fillMaxSize()
            .padding(relleno)

        // Crossfade suaviza el salto entre pestañas; sin él el cambio se siente
        // brusco cuando una lista tarda en llegar.
        Crossfade(
            targetState = subPantalla to pestana,
            label = "pantalla",
        ) { (sub, tab) ->
            when (sub) {
                SubPantalla.EMPLEADOS -> PantallaEmpleados(empleadosVm, modificador)
                SubPantalla.CATEGORIAS -> PantallaCategorias(categoriasVm, modificador)
                SubPantalla.PROVEEDORES -> PantallaProveedores(proveedoresVm, modificador)
                null -> when (tab) {
                    Pestana.PRODUCTOS -> PantallaProductos(
                        vm = productosVm,
                        categorias = categorias.items,
                        proveedores = proveedores.items,
                        modifier = modificador,
                    )

                    Pestana.VENTAS -> PantallaVentas(
                        vm = ventasVm,
                        productos = productos.items,
                        clientes = clientes.items,
                        empleados = empleados.items,
                        alCambiarInventario = productosVm::cargar,
                        modifier = modificador,
                    )

                    Pestana.CLIENTES -> PantallaClientes(clientesVm, modificador)

                    Pestana.REPORTES -> PantallaReportes(reportesVm, modificador)

                    Pestana.MAS -> MenuMas(
                        empleados = empleados.items.size,
                        categorias = categorias.items.size,
                        proveedores = proveedores.items.size,
                        modifier = modificador,
                        alElegir = { subPantalla = it },
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuMas(
    empleados: Int,
    categorias: Int,
    proveedores: Int,
    modifier: Modifier = Modifier,
    alElegir: (SubPantalla) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TarjetaRegistro(
            titulo = "Empleados",
            icono = Icons.Default.Groups,
            acento = TemaFerreteria.acentoDe(1),
            etiquetas = listOf(DatosEtiqueta(conteo(empleados, "registro", "registros"))),
            alTocar = { alElegir(SubPantalla.EMPLEADOS) },
        )
        TarjetaRegistro(
            titulo = "Categorías",
            icono = Icons.Default.Category,
            acento = TemaFerreteria.acentoDe(2),
            etiquetas = listOf(DatosEtiqueta(conteo(categorias, "registro", "registros"))),
            alTocar = { alElegir(SubPantalla.CATEGORIAS) },
        )
        TarjetaRegistro(
            titulo = "Proveedores",
            icono = Icons.Default.LocalShipping,
            acento = TemaFerreteria.acentoDe(3),
            etiquetas = listOf(DatosEtiqueta(conteo(proveedores, "registro", "registros"))),
            alTocar = { alElegir(SubPantalla.PROVEEDORES) },
        )
    }
}

/** Manda avisos y errores de un ViewModel a la barra inferior. */
@Composable
private fun Aviso(
    aviso: String?,
    error: String?,
    limpiarAviso: () -> Unit,
    limpiarError: () -> Unit,
    host: SnackbarHostState,
) {
    LaunchedEffect(aviso) {
        if (aviso != null) {
            host.showSnackbar(aviso)
            limpiarAviso()
        }
    }
    LaunchedEffect(error) {
        if (error != null) {
            host.showSnackbar(error)
            limpiarError()
        }
    }
}

private fun String?.siHayContenido(items: List<*>): String? =
    if (items.isEmpty()) null else this
