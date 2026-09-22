package com.example.ferreteria.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.WorkOutline
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
import com.example.ferreteria.data.Empleado
import com.example.ferreteria.data.EmpleadoRequest
import com.example.ferreteria.ui.theme.TemaFerreteria

@Composable
fun PantallaEmpleados(
    vm: EmpleadosViewModel,
    modifier: Modifier = Modifier,
) {
    val estado by vm.estado.collectAsState()

    var editando by remember { mutableStateOf<Empleado?>(null) }
    var creando by remember { mutableStateOf(false) }
    var porBorrar by remember { mutableStateOf<Empleado?>(null) }
    var busqueda by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.cargarSiHaceFalta() }

    val visibles = remember(estado.items, busqueda) {
        val texto = busqueda.trim().lowercase()
        if (texto.isEmpty()) {
            estado.items
        } else {
            estado.items.filter {
                nombreCompleto(it.nombre_empleado, it.ap_paterno_empleado, it.ap_materno_empleado)
                    .lowercase().contains(texto) ||
                    it.puesto.lowercase().contains(texto) ||
                    it.email_empleado.lowercase().contains(texto)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (estado.items.isNotEmpty()) {
                CampoBusqueda(
                    valor = busqueda,
                    alCambiar = { busqueda = it },
                    marcador = "Buscar por nombre o puesto",
                )
            }

            ContenidoLista(
                modifier = Modifier.weight(1f),
                cargando = estado.cargando,
                error = estado.error,
                items = visibles,
                tituloVacio = if (busqueda.isBlank()) "Sin empleados" else "Sin coincidencias",
                iconoVacio = if (busqueda.isBlank()) Icons.Default.Groups else Icons.Default.SearchOff,
                textoVacio = if (busqueda.isBlank()) {
                    "Registra al menos un empleado: cada venta necesita quién la atendió."
                } else {
                    "Ningún empleado coincide con \"$busqueda\"."
                },
                alReintentar = vm::cargar,
            ) { empleado ->
                val acento = TemaFerreteria.acentoDe(empleado.id_empleado)

                TarjetaRegistro(
                    titulo = nombreCompleto(
                        empleado.nombre_empleado,
                        empleado.ap_paterno_empleado,
                        empleado.ap_materno_empleado,
                    ),
                    acento = acento,
                    clave = claveDe(empleado.id_empleado),
                    lineas = listOf(empleado.email_empleado),
                    etiquetas = listOf(
                        DatosEtiqueta(empleado.puesto, Tono.NEUTRO, Icons.Default.WorkOutline),
                        DatosEtiqueta(empleado.telefono_empleado, Tono.NEUTRO, Icons.Default.PhoneAndroid),
                    ),
                    avatar = DatosAvatar(
                        texto = iniciales(empleado.nombre_empleado, empleado.ap_paterno_empleado),
                        color = acento,
                    ),
                    valor = empleado.sueldo.comoPesos(),
                    valorSecundario = "sueldo",
                    alEditar = { editando = empleado },
                    alBorrar = { porBorrar = empleado },
                )
            }
        }

        ExtendedFloatingActionButton(
            onClick = { creando = true },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Empleado") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
        )
    }

    if (creando || editando != null) {
        FormularioEmpleado(
            empleado = editando,
            guardando = estado.guardando,
            error = estado.error,
            alCerrar = {
                creando = false
                editando = null
                vm.limpiarError()
            },
            alGuardar = { req ->
                vm.guardar(editando?.id_empleado, req) {
                    creando = false
                    editando = null
                }
            },
        )
    }

    porBorrar?.let { empleado ->
        DialogoConfirmar(
            titulo = "¿Eliminar empleado?",
            mensaje = "Se va a borrar a ${empleado.nombre_empleado} ${empleado.ap_paterno_empleado}. " +
                "Si tiene ventas a su nombre, el servidor no lo va a permitir.",
            alConfirmar = {
                vm.eliminar(empleado.id_empleado)
                porBorrar = null
            },
            alCerrar = { porBorrar = null },
        )
    }
}

@Composable
private fun FormularioEmpleado(
    empleado: Empleado?,
    guardando: Boolean,
    error: String?,
    alCerrar: () -> Unit,
    alGuardar: (EmpleadoRequest) -> Unit,
) {
    var nombre by remember { mutableStateOf(empleado?.nombre_empleado ?: "") }
    var apPaterno by remember { mutableStateOf(empleado?.ap_paterno_empleado ?: "") }
    var apMaterno by remember { mutableStateOf(empleado?.ap_materno_empleado ?: "") }
    var puesto by remember { mutableStateOf(empleado?.puesto ?: "") }
    var sueldo by remember { mutableStateOf(empleado?.sueldo ?: "") }
    var telefono by remember { mutableStateOf(empleado?.telefono_empleado ?: "") }
    var email by remember { mutableStateOf(empleado?.email_empleado ?: "") }

    val completo = nombre.isNotBlank() &&
        apPaterno.isNotBlank() &&
        puesto.isNotBlank() &&
        esDecimalValido(sueldo) &&
        esTelefonoValido(telefono) &&
        esEmailValido(email)

    DialogoFormulario(
        titulo = if (empleado == null) "Nuevo empleado" else "Editar empleado",
        guardando = guardando,
        puedeGuardar = completo,
        error = error,
        alCerrar = alCerrar,
        alGuardar = {
            alGuardar(
                EmpleadoRequest(
                    nombre_empleado = nombre.trim(),
                    ap_paterno_empleado = apPaterno.trim(),
                    ap_materno_empleado = apMaterno.trim().ifBlank { null },
                    puesto = puesto.trim(),
                    sueldo = sueldo.trim(),
                    telefono_empleado = telefono.trim(),
                    email_empleado = email.trim(),
                )
            )
        },
    ) {
        TituloSeccion("Nombre")
        CampoFormulario("Nombre", nombre, { nombre = it }, icono = Icons.Default.Badge)
        CampoFormulario("Apellido paterno", apPaterno, { apPaterno = it })
        CampoFormulario("Apellido materno", apMaterno, { apMaterno = it }, obligatorio = false)

        TituloSeccion("Puesto")
        CampoFormulario("Puesto", puesto, { puesto = it }, icono = Icons.Default.WorkOutline)
        CampoFormulario(
            "Sueldo",
            sueldo,
            { sueldo = it },
            tipo = KeyboardType.Decimal,
            icono = Icons.Default.Payments,
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
