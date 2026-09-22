package com.example.ferreteria.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ferreteria.data.Repositorio
import com.example.ferreteria.data.Resultado
import com.example.ferreteria.data.Venta
import com.example.ferreteria.data.VentaItemRequest
import com.example.ferreteria.data.VentaRequest
import com.example.ferreteria.data.VentaResumen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoVentas(
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val ventas: List<VentaResumen> = emptyList(),
    val detalle: Venta? = null,
    val cargandoDetalle: Boolean = false,
    val error: String? = null,
    val aviso: String? = null,
)

/**
 * Las ventas no encajan en [CrudViewModel] porque el POST no manda la misma
 * forma que devuelve el GET: la app solo envía id_producto y cantidad, y el
 * servidor calcula precios, subtotales y total.
 */
class VentasViewModel : ViewModel() {

    private val _estado = MutableStateFlow(EstadoVentas())
    val estado: StateFlow<EstadoVentas> = _estado.asStateFlow()

    private var yaCargo = false

    fun cargarSiHaceFalta() {
        if (!yaCargo) cargar()
    }

    fun cargar() {
        yaCargo = true
        _estado.update { it.copy(cargando = true, error = null) }

        viewModelScope.launch {
            when (val r = Repositorio.listarVentas()) {
                is Resultado.Ok -> _estado.update {
                    it.copy(cargando = false, ventas = r.dato, error = null)
                }

                is Resultado.Error -> _estado.update {
                    it.copy(cargando = false, error = r.mensaje)
                }
            }
        }
    }

    /** Trae el ticket completo de una venta para mostrarlo. */
    fun abrirDetalle(idVenta: Int) {
        _estado.update { it.copy(cargandoDetalle = true, detalle = null, error = null) }

        viewModelScope.launch {
            when (val r = Repositorio.obtenerVenta(idVenta)) {
                is Resultado.Ok -> _estado.update {
                    it.copy(cargandoDetalle = false, detalle = r.dato)
                }

                is Resultado.Error -> _estado.update {
                    it.copy(cargandoDetalle = false, error = r.mensaje)
                }
            }
        }
    }

    fun cerrarDetalle() = _estado.update { it.copy(detalle = null) }

    /**
     * Registra o reescribe una venta. [idVenta] nulo = venta nueva.
     * Las cantidades se mandan tal cual: el servidor las agrupa por producto.
     */
    fun guardarVenta(
        idVenta: Int?,
        idCliente: Int,
        idEmpleado: Int,
        canal: String,
        lineas: List<VentaItemRequest>,
        alGuardar: (Venta) -> Unit,
    ) {
        if (lineas.isEmpty()) {
            _estado.update { it.copy(error = "Agrega al menos un producto a la venta.") }
            return
        }

        _estado.update { it.copy(guardando = true, error = null) }

        viewModelScope.launch {
            val peticion = VentaRequest(
                id_cliente = idCliente,
                id_empleado = idEmpleado,
                canal = canal,
                productos = lineas,
            )

            val r = if (idVenta == null) {
                Repositorio.crearVenta(peticion)
            } else {
                Repositorio.actualizarVenta(idVenta, peticion)
            }

            when (r) {
                is Resultado.Ok -> {
                    _estado.update {
                        it.copy(
                            guardando = false,
                            aviso = if (idVenta == null) {
                                "Venta #${r.dato.id_venta} registrada."
                            } else {
                                "Venta #${r.dato.id_venta} actualizada."
                            },
                        )
                    }
                    alGuardar(r.dato)
                    cargar()
                }

                is Resultado.Error -> _estado.update {
                    it.copy(guardando = false, error = r.mensaje)
                }
            }
        }
    }

    /** Cancela la venta; el servidor devuelve el stock al inventario. */
    fun cancelarVenta(idVenta: Int, alTerminar: () -> Unit) {
        _estado.update { it.copy(guardando = true, error = null) }

        viewModelScope.launch {
            when (val r = Repositorio.borrarVenta(idVenta)) {
                is Resultado.Ok -> {
                    _estado.update {
                        it.copy(
                            guardando = false,
                            detalle = null,
                            aviso = r.dato.mensaje ?: "Venta cancelada.",
                        )
                    }
                    alTerminar()
                    cargar()
                }

                is Resultado.Error -> _estado.update {
                    it.copy(guardando = false, error = r.mensaje)
                }
            }
        }
    }

    fun limpiarAviso() = _estado.update { it.copy(aviso = null) }
    fun limpiarError() = _estado.update { it.copy(error = null) }
}
