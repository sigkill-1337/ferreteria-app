package com.example.ferreteria.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SearchOff
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
import com.example.ferreteria.data.Cliente
import com.example.ferreteria.data.ClienteRequest

@Composable
fun PantallaClientes(
    vm: ClientesViewModel,
    modifier: Modifier = Modifier,
) {
    val estado by vm.estado.collectAsState()

    var editando by remember { mutableStateOf<Cliente?>(null) }
    var creando by remember { mutableStateOf(false) }
    var porBorrar by remember { mutableStateOf<Cliente?>(null) }
    var busqueda by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.cargarSiHaceFalta() }

    val visibles = remember(estado.items, busqueda) {
        val texto = busqueda.trim().lowercase()
        if (texto.isEmpty()) {
            estado.items
        } else {
            estado.items.filter {
                nombreCompleto(it.nombre_cliente, it.ap_paterno_cliente, it.ap_materno_cliente)
                    .lowercase().contains(texto) ||
                    it.email_cliente.lowercase().contains(texto) ||
                    it.telefono_cliente.contains(texto)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (estado.items.isNotEmpty()) {
                CampoBusqueda(
                    valor = busqueda,
                    alCambiar = { busqueda = it },
                    marcador = "Buscar por nombre, correo o teléfono",
                )
            }

            ContenidoLista(
                modifier = Modifier.weight(1f),
                cargando = estado.cargando,
                error = estado.error,
                items = visibles,
                tituloVacio = if (busqueda.isBlank()) "Sin clientes" else "Sin coincidencias",
                iconoVacio = if (busqueda.isBlank()) Icons.Default.PersonOutline else Icons.Default.SearchOff,
                textoVacio = if (busqueda.isBlank()) {
                    "Todavía no hay clientes registrados."
                } else {
                    "Ningún cliente coincide con \"$busqueda\"."
                },
                alReintentar = vm::cargar,
            ) { cliente ->
                TarjetaRegistro(
                    titulo = nombreCompleto(
                        cliente.nombre_cliente,
                        cliente.ap_paterno_cliente,
                        cliente.ap_materno_cliente,
                    ),
                    lineas = listOf(cliente.email_cliente),
                    etiquetas = listOf(
                        DatosEtiqueta(cliente.telefono_cliente, Tono.NEUTRO, Icons.Default.PhoneAndroid),
                    ),
                    avatar = DatosAvatar(
                        texto = iniciales(cliente.nombre_cliente, cliente.ap_paterno_cliente),
                        tono = Tono.PRIMARIO,
                    ),
                    alEditar = { editando = cliente },
                    alBorrar = { porBorrar = cliente },
                )
            }
        }

        ExtendedFloatingActionButton(
            onClick = { creando = true },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Cliente") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
        )
    }

    if (creando || editando != null) {
        FormularioCliente(
            cliente = editando,
            guardando = estado.guardando,
            error = estado.error,
            alCerrar = {
                creando = false
                editando = null
                vm.limpiarError()
            },
            alGuardar = { req ->
                vm.guardar(editando?.id_cliente, req) {
                    creando = false
                    editando = null
                }
            },
        )
    }

    porBorrar?.let { cliente ->
        DialogoConfirmar(
            titulo = "¿Eliminar cliente?",
            mensaje = "Se va a borrar a ${cliente.nombre_cliente} ${cliente.ap_paterno_cliente}. " +
                "Si tiene ventas registradas, el servidor no lo va a permitir.",
            alConfirmar = {
                vm.eliminar(cliente.id_cliente)
                porBorrar = null
            },
            alCerrar = { porBorrar = null },
        )
    }
}

@Composable
private fun FormularioCliente(
    cliente: Cliente?,
    guardando: Boolean,
    error: String?,
    alCerrar: () -> Unit,
    alGuardar: (ClienteRequest) -> Unit,
) {
    var nombre by remember { mutableStateOf(cliente?.nombre_cliente ?: "") }
    var apPaterno by remember { mutableStateOf(cliente?.ap_paterno_cliente ?: "") }
    var apMaterno by remember { mutableStateOf(cliente?.ap_materno_cliente ?: "") }
    var telefono by remember { mutableStateOf(cliente?.telefono_cliente ?: "") }
    var email by remember { mutableStateOf(cliente?.email_cliente ?: "") }

    val completo = nombre.isNotBlank() &&
        apPaterno.isNotBlank() &&
        esTelefonoValido(telefono) &&
        esEmailValido(email)

    DialogoFormulario(
        titulo = if (cliente == null) "Nuevo cliente" else "Editar cliente",
        guardando = guardando,
        puedeGuardar = completo,
        error = error,
        alCerrar = alCerrar,
        alGuardar = {
            alGuardar(
                ClienteRequest(
                    nombre_cliente = nombre.trim(),
                    ap_paterno_cliente = apPaterno.trim(),
                    ap_materno_cliente = apMaterno.trim().ifBlank { null },
                    telefono_cliente = telefono.trim(),
                    email_cliente = email.trim(),
                )
            )
        },
    ) {
        TituloSeccion("Nombre")
        CampoFormulario("Nombre", nombre, { nombre = it }, icono = Icons.Default.Badge)
        CampoFormulario("Apellido paterno", apPaterno, { apPaterno = it })
        CampoFormulario("Apellido materno", apMaterno, { apMaterno = it }, obligatorio = false)

        TituloSeccion("Contacto")
        CampoFormulario(
            "Teléfono",
            telefono,
            { telefono = it },
            tipo = KeyboardType.Phone,
            icono = Icons.Default.PhoneAndroid,
            apoyo = "Entre 7 y 15 caracteres",
        )
        CampoFormulario(
            "Correo",
            email,
            { email = it },
            tipo = KeyboardType.Email,
            icono = Icons.Default.AlternateEmail,
            apoyo = "La API lo exige único: dos clientes no pueden compartirlo",
            ultimo = true,
        )
    }
}

/** Mismo criterio que valida el backend: 7-15 caracteres, dígitos, +, - y espacios. */
fun esTelefonoValido(texto: String): Boolean =
    Regex("""^[0-9+\- ]{7,15}$""").matches(texto.trim())

fun esEmailValido(texto: String): Boolean =
    Regex("""^[^@\s]+@[^@\s]+\.[^@\s]+$""").matches(texto.trim())
