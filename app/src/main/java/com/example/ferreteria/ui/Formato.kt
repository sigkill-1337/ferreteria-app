package com.example.ferreteria.ui

import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Formato de importes y fechas.
 *
 * La API manda los DECIMAL como String ("150.00"). Se convierten a BigDecimal,
 * nunca a Double: con Double, 0.1 + 0.2 no da 0.3 y los totales acaban con
 * centavos fantasma.
 */

private val MEXICO: Locale = Locale.forLanguageTag("es-MX")

/** "150.00" -> BigDecimal(150.00). Cualquier basura devuelve 0. */
fun String?.aDecimal(): BigDecimal =
    this?.trim()?.toBigDecimalOrNull() ?: BigDecimal.ZERO

/** "1150.00" -> "$1,150.00" */
fun String?.comoPesos(): String = aDecimal().comoPesos()

fun BigDecimal.comoPesos(): String =
    "$" + String.format(MEXICO, "%,.2f", this)

/** "2026-08-10 10:15:00" -> "10 ago 2026, 10:15". Si no parsea, regresa el original. */
fun String?.fechaLegible(): String {
    if (this.isNullOrBlank()) return "—"
    return try {
        val entrada = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", MEXICO)
        val salida = SimpleDateFormat("d MMM yyyy, HH:mm", MEXICO)
        val fecha = entrada.parse(this) ?: return this
        salida.format(fecha)
    } catch (e: Exception) {
        this
    }
}

/** Junta nombre y apellidos ignorando los nulos. */
fun nombreCompleto(vararg partes: String?): String =
    partes.filter { !it.isNullOrBlank() }.joinToString(" ")

/** "1 producto" / "3 productos". Evita los feos "producto(s)". */
fun conteo(cantidad: Int, singular: String, plural: String): String =
    if (cantidad == 1) "1 $singular" else "$cantidad $plural"
