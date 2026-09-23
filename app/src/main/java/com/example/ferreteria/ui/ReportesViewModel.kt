package com.example.ferreteria.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ferreteria.data.PersonaDirectorio
import com.example.ferreteria.data.ProductoTop
import com.example.ferreteria.data.Repositorio
import com.example.ferreteria.data.ReporteClientesTrimestre
import com.example.ferreteria.data.ReporteVentasDia
import com.example.ferreteria.data.Resultado
import com.example.ferreteria.data.SeguimientoCliente
import com.example.ferreteria.data.VentasPorCanal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Reportes disponibles.
 *
 * Cada uno se resuelve en la base de datos con un procedimiento almacenado o una
 * consulta agrupada, pero eso no se anuncia en pantalla: a quien atiende el
 * mostrador no le dice nada. El detalle técnico está en el README.
 */
enum class TipoReporte(val etiqueta: String, val descripcion: String) {
    VENTAS_DIA("Ventas del día", "Lo vendido en una fecha"),
    CLIENTES_TRIMESTRE("Clientes activos", "Quién compró en el trimestre"),
    TOP_PRODUCTOS("Más vendidos", "Los productos con más salida"),
    CANALES("Por canal", "De dónde vienen las ventas"),
    DIRECTORIO("Directorio", "Clientes y personal juntos"),
    SEGUIMIENTO("Seguimiento", "Compras por atender"),
}

data class EstadoReportes(
    val tipo: TipoReporte = TipoReporte.VENTAS_DIA,
    val cargando: Boolean = false,
    val error: String? = null,
    val fecha: String = hoyEnTexto(),
    val anio: Int = anioActual(),
    val ventasDia: ReporteVentasDia? = null,
    val trimestre: ReporteClientesTrimestre? = null,
    val topProductos: List<ProductoTop> = emptyList(),
    val canales: List<VentasPorCanal> = emptyList(),
    val directorio: List<PersonaDirectorio> = emptyList(),
    val seguimiento: List<SeguimientoCliente> = emptyList(),
)

/**
 * Reportes de solo lectura. Cada uno pega a reportes.php, que a su vez llama a
 * un procedimiento almacenado o a una consulta agrupada en la base de datos.
 */
class ReportesViewModel : ViewModel() {

    private val _estado = MutableStateFlow(EstadoReportes())
    val estado: StateFlow<EstadoReportes> = _estado.asStateFlow()

    private var yaCargo = false

    fun cargarSiHaceFalta() {
        if (!yaCargo) {
            yaCargo = true
            cargar()
        }
    }

    fun seleccionar(tipo: TipoReporte) {
        if (_estado.value.tipo == tipo) return
        _estado.update { it.copy(tipo = tipo, error = null) }
        cargar()
    }

    /** La fecha llega del calendario, así que siempre viene bien formada. */
    fun cambiarFecha(fecha: String) {
        if (fecha == _estado.value.fecha) return
        _estado.update { it.copy(fecha = fecha) }
        cargar()
    }

    fun cambiarAnio(anio: Int) {
        if (anio == _estado.value.anio) return
        _estado.update { it.copy(anio = anio) }
        cargar()
    }

    fun cargar() {
        val actual = _estado.value
        _estado.update { it.copy(cargando = true, error = null) }

        viewModelScope.launch {
            when (actual.tipo) {
                TipoReporte.VENTAS_DIA -> resolver(Repositorio.ventasDelDia(actual.fecha)) { dato ->
                    _estado.update { it.copy(ventasDia = dato) }
                }

                TipoReporte.CLIENTES_TRIMESTRE ->
                    resolver(Repositorio.clientesDelTrimestre(actual.anio)) { dato ->
                        _estado.update { it.copy(trimestre = dato) }
                    }

                TipoReporte.TOP_PRODUCTOS -> resolver(Repositorio.topProductos()) { dato ->
                    _estado.update { it.copy(topProductos = dato) }
                }

                TipoReporte.CANALES -> resolver(Repositorio.ventasPorCanal()) { dato ->
                    _estado.update { it.copy(canales = dato) }
                }

                TipoReporte.DIRECTORIO -> resolver(Repositorio.directorio()) { dato ->
                    _estado.update { it.copy(directorio = dato) }
                }

                TipoReporte.SEGUIMIENTO -> resolver(Repositorio.seguimiento()) { dato ->
                    _estado.update { it.copy(seguimiento = dato) }
                }
            }
        }
    }

    private fun <T> resolver(resultado: Resultado<T>, alRecibir: (T) -> Unit) {
        when (resultado) {
            is Resultado.Ok -> {
                alRecibir(resultado.dato)
                _estado.update { it.copy(cargando = false, error = null) }
            }

            is Resultado.Error -> _estado.update {
                it.copy(cargando = false, error = resultado.mensaje)
            }
        }
    }

    fun limpiarError() = _estado.update { it.copy(error = null) }
}

private fun hoyEnTexto(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

private fun anioActual(): Int = Calendar.getInstance().get(Calendar.YEAR)
