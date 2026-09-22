package com.example.ferreteria.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ferreteria.data.Categoria
import com.example.ferreteria.data.CategoriaRequest
import com.example.ferreteria.data.Cliente
import com.example.ferreteria.data.ClienteRequest
import com.example.ferreteria.data.Empleado
import com.example.ferreteria.data.EmpleadoRequest
import com.example.ferreteria.data.Mensaje
import com.example.ferreteria.data.Producto
import com.example.ferreteria.data.ProductoRequest
import com.example.ferreteria.data.Proveedor
import com.example.ferreteria.data.ProveedorRequest
import com.example.ferreteria.data.Repositorio
import com.example.ferreteria.data.Resultado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Lo que la pantalla necesita saber para dibujarse. */
data class EstadoCrud<T>(
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val items: List<T> = emptyList(),
    val error: String? = null,
    val aviso: String? = null,
)

/**
 * Base para las pantallas de catálogo. Cada entidad solo dice cómo llamar a la
 * API; la mecánica de cargar, guardar, borrar y reportar errores vive aquí.
 *
 * T = modelo que devuelve la API. R = cuerpo que se manda al crear/editar.
 */
abstract class CrudViewModel<T, R> : ViewModel() {

    private val _estado = MutableStateFlow(EstadoCrud<T>())
    val estado: StateFlow<EstadoCrud<T>> = _estado.asStateFlow()

    private var yaCargo = false

    protected abstract suspend fun listar(): Resultado<List<T>>
    protected abstract suspend fun crear(req: R): Resultado<T>
    protected abstract suspend fun actualizar(id: Int, req: R): Resultado<T>
    protected abstract suspend fun borrar(id: Int): Resultado<Mensaje>

    /** Se llama al abrir la pestaña; no vuelve a pedir datos si ya los tiene. */
    fun cargarSiHaceFalta() {
        if (!yaCargo) cargar()
    }

    fun cargar() {
        yaCargo = true
        _estado.update { it.copy(cargando = true, error = null) }

        viewModelScope.launch {
            when (val r = listar()) {
                is Resultado.Ok -> _estado.update {
                    it.copy(cargando = false, items = r.dato, error = null)
                }

                is Resultado.Error -> _estado.update {
                    it.copy(cargando = false, error = r.mensaje)
                }
            }
        }
    }

    /** id nulo = crear. [alGuardar] corre solo si el servidor aceptó. */
    fun guardar(id: Int?, req: R, alGuardar: () -> Unit) {
        _estado.update { it.copy(guardando = true, error = null) }

        viewModelScope.launch {
            val r = if (id == null) crear(req) else actualizar(id, req)

            when (r) {
                is Resultado.Ok -> {
                    _estado.update {
                        it.copy(
                            guardando = false,
                            aviso = if (id == null) "Registro creado." else "Cambios guardados.",
                        )
                    }
                    alGuardar()
                    cargar()
                }

                is Resultado.Error -> _estado.update {
                    it.copy(guardando = false, error = r.mensaje)
                }
            }
        }
    }

    fun eliminar(id: Int) {
        _estado.update { it.copy(guardando = true, error = null) }

        viewModelScope.launch {
            when (val r = borrar(id)) {
                is Resultado.Ok -> {
                    _estado.update {
                        it.copy(guardando = false, aviso = r.dato.mensaje ?: "Registro eliminado.")
                    }
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

class ProductosViewModel : CrudViewModel<Producto, ProductoRequest>() {
    override suspend fun listar() = Repositorio.listarProductos()
    override suspend fun crear(req: ProductoRequest) = Repositorio.crearProducto(req)
    override suspend fun actualizar(id: Int, req: ProductoRequest) =
        Repositorio.actualizarProducto(id, req)

    override suspend fun borrar(id: Int) = Repositorio.borrarProducto(id)
}

class ClientesViewModel : CrudViewModel<Cliente, ClienteRequest>() {
    override suspend fun listar() = Repositorio.listarClientes()
    override suspend fun crear(req: ClienteRequest) = Repositorio.crearCliente(req)
    override suspend fun actualizar(id: Int, req: ClienteRequest) =
        Repositorio.actualizarCliente(id, req)

    override suspend fun borrar(id: Int) = Repositorio.borrarCliente(id)
}

class EmpleadosViewModel : CrudViewModel<Empleado, EmpleadoRequest>() {
    override suspend fun listar() = Repositorio.listarEmpleados()
    override suspend fun crear(req: EmpleadoRequest) = Repositorio.crearEmpleado(req)
    override suspend fun actualizar(id: Int, req: EmpleadoRequest) =
        Repositorio.actualizarEmpleado(id, req)

    override suspend fun borrar(id: Int) = Repositorio.borrarEmpleado(id)
}

class CategoriasViewModel : CrudViewModel<Categoria, CategoriaRequest>() {
    override suspend fun listar() = Repositorio.listarCategorias()
    override suspend fun crear(req: CategoriaRequest) = Repositorio.crearCategoria(req)
    override suspend fun actualizar(id: Int, req: CategoriaRequest) =
        Repositorio.actualizarCategoria(id, req)

    override suspend fun borrar(id: Int) = Repositorio.borrarCategoria(id)
}

class ProveedoresViewModel : CrudViewModel<Proveedor, ProveedorRequest>() {
    override suspend fun listar() = Repositorio.listarProveedores()
    override suspend fun crear(req: ProveedorRequest) = Repositorio.crearProveedor(req)
    override suspend fun actualizar(id: Int, req: ProveedorRequest) =
        Repositorio.actualizarProveedor(id, req)

    override suspend fun borrar(id: Int) = Repositorio.borrarProveedor(id)
}
