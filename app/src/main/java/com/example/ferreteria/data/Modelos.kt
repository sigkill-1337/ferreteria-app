package com.example.ferreteria.data

/**
 * Modelos de la API de la ferretería.
 *
 * Los nombres van en snake_case a propósito: son exactamente las llaves que
 * manda el backend, así que no hace falta anotar nada con @Json.
 *
 * Los importes (precios, sueldo, total, subtotal) llegan como String porque en
 * MariaDB son DECIMAL(10,2) y PDO los devuelve como texto. Se dejan en String
 * para no perder centavos; si necesitas operar con ellos, usa BigDecimal
 * (ver [aDecimal] en Formato.kt), nunca Double.
 */

// ---------- Productos ----------

data class Producto(
    val id_producto: Int,
    val nombre_producto: String,
    val descripcion_producto: String?,
    val precio_compra: String,
    val precio_venta: String,
    val stock: Int,
    val id_categoria: Int,
    val nombre_categoria: String,
    val id_proveedor: Int,
    val proveedor: String,
)

data class ProductoRequest(
    val nombre_producto: String,
    val descripcion_producto: String?,
    val precio_compra: String,
    val precio_venta: String,
    val stock: Int,
    val id_categoria: Int,
    val id_proveedor: Int,
)

// ---------- Clientes ----------

data class Cliente(
    val id_cliente: Int,
    val nombre_cliente: String,
    val ap_paterno_cliente: String,
    val ap_materno_cliente: String?,
    val telefono_cliente: String,
    val email_cliente: String,
)

data class ClienteRequest(
    val nombre_cliente: String,
    val ap_paterno_cliente: String,
    val ap_materno_cliente: String?,
    val telefono_cliente: String,
    val email_cliente: String,
)

// ---------- Empleados ----------

data class Empleado(
    val id_empleado: Int,
    val nombre_empleado: String,
    val ap_paterno_empleado: String,
    val ap_materno_empleado: String?,
    val puesto: String,
    val sueldo: String,
    val telefono_empleado: String,
    val email_empleado: String,
)

data class EmpleadoRequest(
    val nombre_empleado: String,
    val ap_paterno_empleado: String,
    val ap_materno_empleado: String?,
    val puesto: String,
    val sueldo: String,
    val telefono_empleado: String,
    val email_empleado: String,
)

// ---------- Categorías ----------

data class Categoria(
    val id_categoria: Int,
    val nombre_categoria: String,
    val descripcion: String?,
)

data class CategoriaRequest(
    val nombre_categoria: String,
    val descripcion: String?,
)

// ---------- Proveedores ----------

data class Proveedor(
    val id_proveedor: Int,
    val rfc: String,
    val nombre_empresa: String,
    val telefono_proveedor: String,
    val email_proveedor: String,
)

data class ProveedorRequest(
    val rfc: String,
    val nombre_empresa: String,
    val telefono_proveedor: String,
    val email_proveedor: String,
)

// ---------- Ventas ----------

data class DetalleVenta(
    val id_detalle: Int,
    val cantidad: Int,
    val precio_unitario: String,
    val subtotal: String,
    val id_producto: Int,
    val nombre_producto: String,
)

/** Venta completa: la devuelve GET ?id=, POST y PUT. */
data class Venta(
    val id_venta: Int,
    val fecha: String,
    val total: String,
    val canal: String,
    val id_cliente: Int,
    val nombre_cliente: String,
    val ap_paterno_cliente: String,
    val ap_materno_cliente: String?,
    val id_empleado: Int,
    val nombre_empleado: String,
    val ap_paterno_empleado: String,
    val productos: List<DetalleVenta>,
)

/** Renglón del listado: igual que Venta pero sin el detalle. */
data class VentaResumen(
    val id_venta: Int,
    val fecha: String,
    val total: String,
    val canal: String,
    val id_cliente: Int,
    val nombre_cliente: String,
    val ap_paterno_cliente: String,
    val ap_materno_cliente: String?,
    val id_empleado: Int,
    val nombre_empleado: String,
    val ap_paterno_empleado: String,
    val num_productos: Int,
)

data class VentaItemRequest(
    val id_producto: Int,
    val cantidad: Int,
)

data class VentaRequest(
    val id_cliente: Int,
    val id_empleado: Int,
    /** APP, WEB o MOSTRADOR. El trigger de seguimiento solo registra APP y WEB. */
    val canal: String,
    val productos: List<VentaItemRequest>,
)

// ---------- Respuestas genéricas ----------

/** Cuerpo de los DELETE: {"mensaje": "...", "id_x": 3}. */
data class Mensaje(
    val mensaje: String?,
)

/** Todos los errores de la API tienen esta forma: {"error": "..."}. */
data class ApiError(
    val error: String?,
)

// ---------- Reportes ----------
//
// Cada uno viene de reportes.php y corresponde a una estructura de SQL del
// proyecto: procedimiento almacenado, GROUP BY, UNION o el trigger de bitacora.

/** Salida de sp_ventas_del_dia: el desglose mas la suma del dia. */
data class ReporteVentasDia(
    val fecha: String,
    val num_ventas: Int,
    val monto_total: String,
    val ticket_promedio: String,
    val venta_mayor: String,
    val ventas: List<VentaDelDia>,
)

data class VentaDelDia(
    val id_venta: Int,
    val fecha: String,
    val hora: String,
    val canal: String,
    val total: String,
    val id_cliente: Int,
    val cliente: String,
    val empleado: String,
    val renglones: Int,
    val piezas: Int,
)

/** Salida de sp_clientes_vigentes_trimestre. */
data class ReporteClientesTrimestre(
    val anio: Int,
    val trimestre: Int,
    val periodo: String,
    val clientes: List<ClienteVigente>,
)

data class ClienteVigente(
    val id_cliente: Int,
    val cliente: String,
    val email_cliente: String,
    val telefono_cliente: String,
    val pedidos: Int,
    val monto_total: String,
    val primera_compra: String,
    val ultima_compra: String,
    val dias_desde_ultima: Int,
)

/** GROUP BY sobre DETALLE_VENTA. */
data class ProductoTop(
    val id_producto: Int,
    val nombre_producto: String,
    val nombre_categoria: String,
    val piezas_vendidas: Int,
    val importe_vendido: String,
    val aparece_en_ventas: Int,
)

/** UNION de CLIENTE y EMPLEADO. */
data class PersonaDirectorio(
    val tipo: String,
    val id: Int,
    val nombre: String,
    val telefono: String,
    val email: String,
    val detalle: String,
)

/** Bitacora que llena el trigger trg_venta_seguimiento. */
data class SeguimientoCliente(
    val id_seguimiento: Int,
    val id_cliente: Int,
    val nombre_cliente: String,
    val canal: String,
    val total_compra: String,
    val fecha_hora: String,
    val fecha_legible: String,
    val atendido: Int,
    val id_venta: Int?,
)

/** GROUP BY sobre VENTA.canal. */
data class VentasPorCanal(
    val canal: String,
    val num_ventas: Int,
    val monto_total: String,
    val porcentaje: Double,
)
