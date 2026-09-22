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
 * Tema violeta que sigue al sistema: oscuro si el teléfono está en oscuro.
 *
 * No se usa dynamicColor a propósito. Con colores dinámicos la paleta cambia de
 * teléfono en teléfono, y aquí los tonos cargan significado — el verde de
 * "stock sano" y el ámbar de "stock bajo" tienen que ser siempre los mismos.
 */

private val EsquemaOscuro = darkColorScheme(
    primary = LavandaOscuro,
    onPrimary = LavandaSobrePrimarioOscuro,
    primaryContainer = LavandaContenedorOscuro,
    onPrimaryContainer = LavandaSobreContenedorOscuro,
    secondary = RosaOscuro,
    onSecondary = RosaSobreSecundarioOscuro,
    secondaryContainer = RosaContenedorOscuro,
    onSecondaryContainer = RosaSobreContenedorOscuro,
    tertiary = AzulOscuro,
    onTertiary = AzulSobreTerciarioOscuro,
    tertiaryContainer = AzulContenedorOscuro,
    onTertiaryContainer = AzulSobreContenedorOscuro,
    background = FondoOscuro,
    onBackground = TextoOscuro,
    surface = FondoOscuro,
    onSurface = TextoOscuro,
    surfaceVariant = VarianteOscura,
    onSurfaceVariant = TextoVarianteOscuro,
    surfaceContainerLowest = TarjetaOscuraMinima,
    surfaceContainerLow = TarjetaOscuraBaja,
    surfaceContainer = SuperficieOscura,
    surfaceContainerHigh = TarjetaOscura,
    surfaceContainerHighest = TarjetaOscuraAlta,
    outline = BordeOscuro,
    outlineVariant = BordeVarianteOscuro,
    error = ErrorOscuro,
    onError = ErrorSobreOscuro,
    errorContainer = ErrorContenedorOscuro,
    onErrorContainer = ErrorSobreContenedorOscuro,
    inversePrimary = VioletaClaro,
)

private val EsquemaClaro = lightColorScheme(
    primary = VioletaClaro,
    onPrimary = Blanco,
    primaryContainer = VioletaContenedorClaro,
    onPrimaryContainer = VioletaSobreContenedorClaro,
    secondary = MagentaClaro,
    onSecondary = Blanco,
    secondaryContainer = MagentaContenedorClaro,
    onSecondaryContainer = MagentaSobreContenedorClaro,
    tertiary = AzulClaro,
    onTertiary = Blanco,
    tertiaryContainer = AzulContenedorClaro,
    onTertiaryContainer = AzulSobreContenedorClaro,
    background = FondoClaro,
    onBackground = TextoClaro,
    surface = FondoClaro,
    onSurface = TextoClaro,
    surfaceVariant = VarianteClara,
    onSurfaceVariant = TextoVarianteClaro,
    surfaceContainerLowest = TarjetaClaraMinima,
    surfaceContainerLow = TarjetaClaraBaja,
    surfaceContainer = SuperficieClara,
    surfaceContainerHigh = TarjetaClara,
    surfaceContainerHighest = TarjetaClaraAlta,
    outline = BordeClaro,
    outlineVariant = BordeVarianteClaro,
    error = ErrorClaro,
    onError = Blanco,
    errorContainer = ErrorContenedorClaro,
    onErrorContainer = ErrorSobreContenedorClaro,
    inversePrimary = LavandaOscuro,
)

/** Colores que Material 3 no define y el inventario sí necesita. */
data class ColoresEstado(
    val exito: Color,
    val exitoContenedor: Color,
    val sobreExitoContenedor: Color,
    val alerta: Color,
    val alertaContenedor: Color,
    val sobreAlertaContenedor: Color,
)

private val EstadoOscuro = ColoresEstado(
    exito = ExitoOscuro,
    exitoContenedor = ExitoContenedorOscuro,
    sobreExitoContenedor = ExitoSobreContenedorOscuro,
    alerta = AlertaOscuro,
    alertaContenedor = AlertaContenedorOscuro,
    sobreAlertaContenedor = AlertaSobreContenedorOscuro,
)

private val EstadoClaro = ColoresEstado(
    exito = ExitoClaro,
    exitoContenedor = ExitoContenedorClaro,
    sobreExitoContenedor = ExitoSobreContenedorClaro,
    alerta = AlertaClaro,
    alertaContenedor = AlertaContenedorClaro,
    sobreAlertaContenedor = AlertaSobreContenedorClaro,
)

private val LocalColoresEstado = staticCompositionLocalOf { EstadoOscuro }

/** Acceso a los colores de estado desde cualquier composable, al estilo MaterialTheme. */
object TemaFerreteria {
    val estado: ColoresEstado
        @Composable
        @ReadOnlyComposable
        get() = LocalColoresEstado.current
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
