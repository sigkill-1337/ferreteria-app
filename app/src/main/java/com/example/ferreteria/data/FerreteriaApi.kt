package com.example.ferreteria.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

/**
 * Contrato de la API. Cada recurso expone lista, detalle, crear, editar y borrar.
 * El header X-API-Key lo agrega el interceptor de [ApiClient], no va aquí.
 *
 * PUT es reemplazo completo: hay que mandar todos los campos editables.
 */
interface FerreteriaApi {

    // ---------- Productos ----------

    @GET("productos.php")
    suspend fun getProductos(): Response<List<Producto>>

    @GET("productos.php")
    suspend fun getProducto(@Query("id") id: Int): Response<Producto>

    @POST("productos.php")
    suspend fun crearProducto(@Body producto: ProductoRequest): Response<Producto>

    @PUT("productos.php")
    suspend fun actualizarProducto(
        @Query("id") id: Int,
        @Body producto: ProductoRequest,
    ): Response<Producto>

    @DELETE("productos.php")
    suspend fun borrarProducto(@Query("id") id: Int): Response<Mensaje>

    // ---------- Clientes ----------

    @GET("clientes.php")
    suspend fun getClientes(): Response<List<Cliente>>

    @GET("clientes.php")
    suspend fun getCliente(@Query("id") id: Int): Response<Cliente>

    @POST("clientes.php")
    suspend fun crearCliente(@Body cliente: ClienteRequest): Response<Cliente>

    @PUT("clientes.php")
    suspend fun actualizarCliente(
        @Query("id") id: Int,
        @Body cliente: ClienteRequest,
    ): Response<Cliente>

    @DELETE("clientes.php")
    suspend fun borrarCliente(@Query("id") id: Int): Response<Mensaje>

    // ---------- Empleados ----------

    @GET("empleados.php")
    suspend fun getEmpleados(): Response<List<Empleado>>

    @POST("empleados.php")
    suspend fun crearEmpleado(@Body empleado: EmpleadoRequest): Response<Empleado>

    @PUT("empleados.php")
    suspend fun actualizarEmpleado(
        @Query("id") id: Int,
        @Body empleado: EmpleadoRequest,
    ): Response<Empleado>

    @DELETE("empleados.php")
    suspend fun borrarEmpleado(@Query("id") id: Int): Response<Mensaje>

    // ---------- Categorías ----------

    @GET("categorias.php")
    suspend fun getCategorias(): Response<List<Categoria>>

    @POST("categorias.php")
    suspend fun crearCategoria(@Body categoria: CategoriaRequest): Response<Categoria>

    @PUT("categorias.php")
    suspend fun actualizarCategoria(
        @Query("id") id: Int,
        @Body categoria: CategoriaRequest,
    ): Response<Categoria>

    @DELETE("categorias.php")
    suspend fun borrarCategoria(@Query("id") id: Int): Response<Mensaje>

    // ---------- Proveedores ----------

    @GET("proveedores.php")
    suspend fun getProveedores(): Response<List<Proveedor>>

    @POST("proveedores.php")
    suspend fun crearProveedor(@Body proveedor: ProveedorRequest): Response<Proveedor>

    @PUT("proveedores.php")
    suspend fun actualizarProveedor(
        @Query("id") id: Int,
        @Body proveedor: ProveedorRequest,
    ): Response<Proveedor>

    @DELETE("proveedores.php")
    suspend fun borrarProveedor(@Query("id") id: Int): Response<Mensaje>

    // ---------- Ventas ----------

    @GET("ventas.php")
    suspend fun getVentas(): Response<List<VentaResumen>>

    @GET("ventas.php")
    suspend fun getVenta(@Query("id") id: Int): Response<Venta>

    @POST("ventas.php")
    suspend fun crearVenta(@Body venta: VentaRequest): Response<Venta>

    @PUT("ventas.php")
    suspend fun actualizarVenta(
        @Query("id") id: Int,
        @Body venta: VentaRequest,
    ): Response<Venta>

    /** Cancela la venta y devuelve el stock al inventario. */
    @DELETE("ventas.php")
    suspend fun borrarVenta(@Query("id") id: Int): Response<Mensaje>

    // ---------- Reportes ----------
    //
    // Todos cuelgan de reportes.php y se distinguen por el parametro "tipo".
    // El tipo se manda desde el repositorio y no como valor por omision del
    // parametro: Retrofit lee la firma por reflexion y los argumentos por
    // omision de Kotlin generan un metodo sintetico que no reconoce.

    /** Procedimiento sp_ventas_del_dia. */
    @GET("reportes.php")
    suspend fun getVentasDelDia(
        @Query("tipo") tipo: String,
        @Query("fecha") fecha: String,
    ): Response<ReporteVentasDia>

    /** Procedimiento sp_clientes_vigentes_trimestre. */
    @GET("reportes.php")
    suspend fun getClientesDelTrimestre(
        @Query("tipo") tipo: String,
        @Query("anio") anio: Int,
        @Query("trimestre") trimestre: Int,
    ): Response<ReporteClientesTrimestre>

    /** GROUP BY sobre el detalle de ventas. */
    @GET("reportes.php")
    suspend fun getTopProductos(@Query("tipo") tipo: String): Response<List<ProductoTop>>

    /** UNION de clientes y empleados. */
    @GET("reportes.php")
    suspend fun getDirectorio(@Query("tipo") tipo: String): Response<List<PersonaDirectorio>>

    /** Bitacora que llena el trigger de atencion a clientes. */
    @GET("reportes.php")
    suspend fun getSeguimiento(@Query("tipo") tipo: String): Response<List<SeguimientoCliente>>

    /** GROUP BY sobre el canal de compra. */
    @GET("reportes.php")
    suspend fun getVentasPorCanal(@Query("tipo") tipo: String): Response<List<VentasPorCanal>>
}
