package com.example.ferreteria.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Tema estilo GitHub, siguiendo al sistema: oscuro si el teléfono está en oscuro.
 *
 * No se usa dynamicColor a propósito. Con colores dinámicos la paleta cambia de
 * teléfono en teléfono, y aquí los tonos cargan significado — el verde de
 * "stock sano" y el ámbar de "stock bajo" tienen que ser siempre los mismos.
 */

private val EsquemaOscuro = darkColorScheme(
    primary = GhAzul,
    onPrimary = GhFondo,
    primaryContainer = GhAzulFondo,
    onPrimaryContainer = GhAzulTexto,
    secondary = GhMorado,
    onSecondary = GhFondo,
    secondaryContainer = GhMoradoFondo,
    onSecondaryContainer = GhMoradoTexto,
    tertiary = GhRosa,
    onTertiary = GhFondo,
    tertiaryContainer = GhRosaFondo,
    onTertiaryContainer = GhRosaTexto,
    background = GhFondo,
    onBackground = GhTexto,
    surface = GhFondo,
    onSurface = GhTexto,
    surfaceVariant = GhElevado,
    onSurfaceVariant = GhTextoTenue,
    surfaceContainerLowest = GhNegro,
    surfaceContainerLow = GhFondo,
    surfaceContainer = GhSuperficie,
    surfaceContainerHigh = GhElevado,
    surfaceContainerHighest = GhBorde,
    outline = GhGrisMedio,
    outlineVariant = GhBorde,
    error = GhRojo,
    onError = GhFondo,
    errorContainer = GhRojoFondo,
    onErrorContainer = GhRojoTexto,
    inversePrimary = GhcAzul,
)

private val EsquemaClaro = lightColorScheme(
    primary = GhcAzul,
    onPrimary = GhcBlanco,
    primaryContainer = GhcAzulFondo,
    onPrimaryContainer = GhcAzulTexto,
    secondary = GhcMorado,
    onSecondary = GhcBlanco,
    secondaryContainer = GhcMoradoFondo,
    onSecondaryContainer = GhcMoradoTexto,
    tertiary = GhcRosa,
    onTertiary = GhcBlanco,
    tertiaryContainer = GhcRosaFondo,
    onTertiaryContainer = GhcRosaTexto,
    background = GhcBlanco,
    onBackground = GhcTexto,
    surface = GhcBlanco,
    onSurface = GhcTexto,
    surfaceVariant = GhcElevado,
    onSurfaceVariant = GhcTextoTenue,
    surfaceContainerLowest = GhcBlanco,
    surfaceContainerLow = GhcSuperficie,
    surfaceContainer = GhcSuperficie,
    surfaceContainerHigh = GhcElevado,
    surfaceContainerHighest = GhcBorde,
    outline = GhcGrisMedio,
    outlineVariant = GhcBorde,
    error = GhcRojo,
    onError = GhcBlanco,
    errorContainer = GhcRojoFondo,
    onErrorContainer = GhcRojoTexto,
    inversePrimary = GhAzul,
)

/** Colores que Material 3 no define y el inventario sí necesita. */
data class ColoresEstado(
    val exito: Color,
    val exitoContenedor: Color,
    val sobreExitoContenedor: Color,
    val alerta: Color,
    val alertaContenedor: Color,
    val sobreAlertaContenedor: Color,
    /** Acentos de categoría, para las franjas de las tarjetas. */
    val acentos: List<Color>,
)

private val EstadoOscuro = ColoresEstado(
    exito = GhVerde,
    exitoContenedor = GhVerdeFondo,
    sobreExitoContenedor = GhVerdeTexto,
    alerta = GhAmarillo,
    alertaContenedor = GhAmarilloFondo,
    sobreAlertaContenedor = GhAmarilloTexto,
    acentos = AcentosOscuro,
)

private val EstadoClaro = ColoresEstado(
    exito = GhcVerde,
    exitoContenedor = GhcVerdeFondo,
    sobreExitoContenedor = GhcVerdeTexto,
    alerta = GhcAmarillo,
    alertaContenedor = GhcAmarilloFondo,
    sobreAlertaContenedor = GhcAmarilloTexto,
    acentos = AcentosClaro,
)

private val LocalColoresEstado = staticCompositionLocalOf { EstadoOscuro }

/** Acceso a los colores de estado desde cualquier composable, al estilo MaterialTheme. */
object TemaFerreteria {
    val estado: ColoresEstado
        @Composable
        @ReadOnlyComposable
        get() = LocalColoresEstado.current

    /** Color estable para un identificador: la misma categoría siempre igual. */
    @Composable
    @ReadOnlyComposable
    fun acentoDe(id: Int): Color {
        val acentos = LocalColoresEstado.current.acentos
        return acentos[((id - 1) % acentos.size + acentos.size) % acentos.size]
    }
}

@Composable
fun FerreteriaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalColoresEstado provides if (darkTheme) EstadoOscuro else EstadoClaro,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
            typography = Typography,
            content = content,
        )
    }
}
