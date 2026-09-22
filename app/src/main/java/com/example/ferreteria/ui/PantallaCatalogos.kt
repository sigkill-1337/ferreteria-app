package com.example.ferreteria.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ferreteria.data.Categoria
import com.example.ferreteria.data.CategoriaRequest
import com.example.ferreteria.data.Proveedor
import com.example.ferreteria.data.ProveedorRequest

/** Catálogos de apoyo: sin ellos no se puede dar de alta un producto. */

@Composable
fun PantallaCategorias(
    vm: CategoriasViewModel,
    modifier: Modifier = Modifier,
) {
    val estado by vm.estado.collectAsState()

    var editando by remember { mutableStateOf<Categoria?>(null) }
    var creando by remember { mutableStateOf(false) }
    var porBorrar by remember { mutableStateOf<Categoria?>(null) }

    LaunchedEffect(Unit) { vm.cargarSiHaceFalta() }

    Box(modifier = modifier.fillMaxSize()) {
        ContenidoLista(
            cargando = estado.cargando,
            error = estado.error,
            items = estado.items,
            tituloVacio = "Sin categorías",
            iconoVacio = Icons.Default.Category,
            textoVacio = "Agrega al menos una categoría para poder crear productos.",
            alReintentar = vm::cargar,
        ) { categoria ->
            TarjetaRegistro(
                titulo = categoria.nombre_categoria,
                subtitulo = categoria.descripcion?.takeIf { it.isNotBlank() }
                    ?: "Sin descripción",
                avatar = DatosAvatar(
                    icono = Icons.Default.Category,
                    tono = Tono.ACENTO,
                ),
                alEditar = { editando = categoria },
                alBorrar = { porBorrar = categoria },
            )
        }

        ExtendedFloatingActionButton(
            onClick = { creando = true },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Categoría") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
        )
    }

    if (creando || editando != null) {
        val actual = editando
        var nombre by remember(actual) { mutableStateOf(actual?.nombre_categoria ?: "") }
        var descripcion by remember(actual) { mutableStateOf(actual?.descripcion ?: "") }

        DialogoFormulario(
            titulo = if (actual == null) "Nueva categoría" else "Editar categoría",
            guardando = estado.guardando,
            puedeGuardar = nombre.isNotBlank(),
            error = estado.error,
            alCerrar = {
                creando = false
                editando = null
                vm.limpiarError()
            },
            alGuardar = {
                vm.guardar(
                    actual?.id_categoria,
                    CategoriaRequest(
                        nombre_categoria = nombre.trim(),
                        descripcion = descripcion.trim().ifBlank { null },
                    ),
                ) {
                    creando = false
                    editando = null
                }
            },
        ) {
            CampoFormulario("Nombre", nombre, { nombre = it }, icono = Icons.Default.Category)
            CampoFormulario(
                "Descripción",
                descripcion,
                { descripcion = it },
                obligatorio = false,
                ultimo = true,
            )
        }
    }

    porBorrar?.let { categoria ->
        DialogoConfirmar(
            titulo = "¿Eliminar categoría?",
            mensaje = "Se va a borrar \"${categoria.nombre_categoria}\". " +
                "Si tiene productos asignados, el servidor no lo va a permitir.",
            alConfirmar = {
                vm.eliminar(categoria.id_categoria)
                porBorrar = null
            },
            alCerrar = { porBorrar = null },
        )
    }
}

@Composable
fun PantallaProveedores(
    vm: ProveedoresViewModel,
    modifier: Modifier = Modifier,
) {
    val estado by vm.estado.collectAsState()

    var editando by remember { mutableStateOf<Proveedor?>(null) }
    var creando by remember { mutableStateOf(false) }
    var porBorrar by remember { mutableStateOf<Proveedor?>(null) }

    LaunchedEffect(Unit) { vm.cargarSiHaceFalta() }

    Box(modifier = modifier.fillMaxSize()) {
        ContenidoLista(
            cargando = estado.cargando,
            error = estado.error,
            items = estado.items,
            tituloVacio = "Sin proveedores",
            iconoVacio = Icons.Default.LocalShipping,
            textoVacio = "Agrega al menos un proveedor para poder crear productos.",
            alReintentar = vm::cargar,
        ) { proveedor ->
            TarjetaRegistro(
                titulo = proveedor.nombre_empresa,
                lineas = listOf(proveedor.email_proveedor),
                etiquetas = listOf(
                    DatosEtiqueta(proveedor.rfc, Tono.PRIMARIO, Icons.Default.Tag),
                    DatosEtiqueta(proveedor.telefono_proveedor, Tono.NEUTRO, Icons.Default.PhoneAndroid),
                ),
                avatar = DatosAvatar(
                    icono = Icons.Default.LocalShipping,
                    tono = Tono.ALERTA,
                ),
                alEditar = { editando = proveedor },
                alBorrar = { porBorrar = proveedor },
            )
        }

        ExtendedFloatingActionButton(
            onClick = { creando = true },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Proveedor") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
        )
    }

    if (creando || editando != null) {
        val actual = editando
        var rfc by remember(actual) { mutableStateOf(actual?.rfc ?: "") }
        var empresa by remember(actual) { mutableStateOf(actual?.nombre_empresa ?: "") }
        var telefono by remember(actual) { mutableStateOf(actual?.telefono_proveedor ?: "") }
        var email by remember(actual) { mutableStateOf(actual?.email_proveedor ?: "") }

        val completo = esRfcValido(rfc) &&
            empresa.isNotBlank() &&
            esTelefonoValido(telefono) &&
            esEmailValido(email)

        DialogoFormulario(
            titulo = if (actual == null) "Nuevo proveedor" else "Editar proveedor",
            guardando = estado.guardando,
            puedeGuardar = completo,
            error = estado.error,
            alCerrar = {
                creando = false
                editando = null
                vm.limpiarError()
            },
            alGuardar = {
                vm.guardar(
                    actual?.id_proveedor,
                    ProveedorRequest(
                        rfc = rfc.trim().uppercase(),
                        nombre_empresa = empresa.trim(),
                        telefono_proveedor = telefono.trim(),
                        email_proveedor = email.trim(),
                    ),
                ) {
                    creando = false
                    editando = null
                }
            },
        ) {
            TituloSeccion("Empresa")
            CampoFormulario("Empresa", empresa, { empresa = it }, icono = Icons.Default.Apartment)
            CampoFormulario(
                "RFC",
                rfc,
                { rfc = it },
                icono = Icons.Default.Tag,
                apoyo = "12 o 13 caracteres alfanuméricos, único por proveedor",
            )

            TituloSeccion("Contacto")
            CampoFormulario(
                "Teléfono",
                telefono,
                { telefono = it },
                tipo = KeyboardType.Phone,
                icono = Icons.Default.PhoneAndroid,
            )
            CampoFormulario(
                "Correo",
                email,
                { email = it },
                tipo = KeyboardType.Email,
                icono = Icons.Default.AlternateEmail,
                ultimo = true,
            )
        }
    }

    porBorrar?.let { proveedor ->
        DialogoConfirmar(
            titulo = "¿Eliminar proveedor?",
            mensaje = "Se va a borrar \"${proveedor.nombre_empresa}\". " +
                "Si surte algún producto, el servidor no lo va a permitir.",
            alConfirmar = {
                vm.eliminar(proveedor.id_proveedor)
                porBorrar = null
            },
            alCerrar = { porBorrar = null },
        )
    }
}

fun esRfcValido(texto: String): Boolean =
    Regex("""^[A-Za-z0-9&Ññ]{12,13}$""").matches(texto.trim())
