package com.example.ferreteria.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException

/** Resultado de cualquier llamada a la API: o hay dato, o hay un mensaje que mostrar. */
sealed interface Resultado<out T> {
    data class Ok<T>(val dato: T) : Resultado<T>
    data class Error(val codigo: Int?, val mensaje: String) : Resultado<Nothing>
}

/**
 * Único punto de acceso a la API. Envuelve cada llamada para que la UI reciba
 * siempre un [Resultado] y nunca una excepción.
 */
object Repositorio {

    private val api = ApiClient.api
    private val adaptadorError = ApiClient.moshi.adapter(ApiError::class.java)

    // ---------- Productos ----------

    suspend fun listarProductos() = llamar { api.getProductos() }
    suspend fun crearProducto(req: ProductoRequest) = llamar { api.crearProducto(req) }
    suspend fun actualizarProducto(id: Int, req: ProductoRequest) =
        llamar { api.actualizarProducto(id, req) }

    suspend fun borrarProducto(id: Int) = llamar { api.borrarProducto(id) }

    // ---------- Clientes ----------

    suspend fun listarClientes() = llamar { api.getClientes() }
    suspend fun crearCliente(req: ClienteRequest) = llamar { api.crearCliente(req) }
    suspend fun actualizarCliente(id: Int, req: ClienteRequest) =
        llamar { api.actualizarCliente(id, req) }

    suspend fun borrarCliente(id: Int) = llamar { api.borrarCliente(id) }

    // ---------- Empleados ----------

    suspend fun listarEmpleados() = llamar { api.getEmpleados() }
    suspend fun crearEmpleado(req: EmpleadoRequest) = llamar { api.crearEmpleado(req) }
    suspend fun actualizarEmpleado(id: Int, req: EmpleadoRequest) =
        llamar { api.actualizarEmpleado(id, req) }

    suspend fun borrarEmpleado(id: Int) = llamar { api.borrarEmpleado(id) }

    // ---------- Categorías ----------

    suspend fun listarCategorias() = llamar { api.getCategorias() }
    suspend fun crearCategoria(req: CategoriaRequest) = llamar { api.crearCategoria(req) }
    suspend fun actualizarCategoria(id: Int, req: CategoriaRequest) =
        llamar { api.actualizarCategoria(id, req) }

    suspend fun borrarCategoria(id: Int) = llamar { api.borrarCategoria(id) }

    // ---------- Proveedores ----------

    suspend fun listarProveedores() = llamar { api.getProveedores() }
    suspend fun crearProveedor(req: ProveedorRequest) = llamar { api.crearProveedor(req) }
    suspend fun actualizarProveedor(id: Int, req: ProveedorRequest) =
        llamar { api.actualizarProveedor(id, req) }

    suspend fun borrarProveedor(id: Int) = llamar { api.borrarProveedor(id) }

    // ---------- Ventas ----------

    suspend fun listarVentas() = llamar { api.getVentas() }
    suspend fun obtenerVenta(id: Int) = llamar { api.getVenta(id) }
    suspend fun crearVenta(req: VentaRequest) = llamar { api.crearVenta(req) }
    suspend fun actualizarVenta(id: Int, req: VentaRequest) = llamar { api.actualizarVenta(id, req) }
    suspend fun borrarVenta(id: Int) = llamar { api.borrarVenta(id) }

    // ---------- Reportes ----------

    suspend fun ventasDelDia(fecha: String) = llamar { api.getVentasDelDia("ventas-dia", fecha) }
    suspend fun clientesDelTrimestre(anio: Int) =
        llamar { api.getClientesDelTrimestre("clientes-trimestre", anio) }

    suspend fun topProductos() = llamar { api.getTopProductos("top-productos") }
    suspend fun directorio() = llamar { api.getDirectorio("directorio") }
    suspend fun seguimiento() = llamar { api.getSeguimiento("seguimiento") }
    suspend fun ventasPorCanal() = llamar { api.getVentasPorCanal("ventas-por-canal") }

    // ---------- Plomería ----------

    private suspend fun <T> llamar(bloque: suspend () -> Response<T>): Resultado<T> =
        withContext(Dispatchers.IO) {
            if (ApiClient.faltaApiKey) {
                return@withContext Resultado.Error(
                    null,
                    "Falta FERRETERIA_API_KEY en local.properties. Agrégala y vuelve a compilar."
                )
            }

            try {
                val respuesta = bloque()
                val cuerpo = respuesta.body()

                if (respuesta.isSuccessful && cuerpo != null) {
                    Resultado.Ok(cuerpo)
                } else {
                    Resultado.Error(respuesta.code(), mensajeDeError(respuesta))
                }
            } catch (e: IOException) {
                Resultado.Error(null, "Sin conexión. Revisa tu internet e intenta de nuevo.")
            } catch (e: Exception) {
                Resultado.Error(null, "Ocurrió un problema inesperado. Vuelve a intentarlo.")
            }
        }

    /**
     * Saca el mensaje de {"error": "..."} que manda la API. Si el cuerpo no se
     * puede leer, cae a un texto según el código HTTP.
     */
    private fun mensajeDeError(respuesta: Response<*>): String {
        val texto = try {
            respuesta.errorBody()?.string()
        } catch (e: IOException) {
            null
        }

        val delServidor = texto?.takeIf { it.isNotBlank() }?.let { cuerpo ->
            try {
                adaptadorError.fromJson(cuerpo)?.error
            } catch (e: Exception) {
                null
            }
        }

        if (!delServidor.isNullOrBlank()) return delServidor

        return when (respuesta.code()) {
            401 -> "La aplicación no pudo identificarse. Avisa a soporte."
            404 -> "Ese registro ya no existe."
            405 -> "Esa operación no está disponible."
            409 -> "Ese dato ya está registrado, o hay información que depende de él."
            500 -> "Hubo un problema al procesar la solicitud. Intenta de nuevo."
            else -> "No se pudo completar la operación. Intenta de nuevo."
        }
    }
}
