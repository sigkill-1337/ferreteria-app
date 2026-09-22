package com.example.ferreteria.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta tomada del tema oscuro de GitHub (Primer).
 *
 * El modo claro se derivó del tema claro de GitHub, no del violeta anterior: si
 * solo se cambiara el oscuro, la app tendría dos identidades distintas según
 * cómo esté configurado el teléfono, y cambiar de modo se sentiría como abrir
 * otra aplicación.
 *
 * Lo que caracteriza a este tema no es solo el color: es el gris azulado muy
 * oscuro del fondo, el escalón corto entre fondo y tarjeta, y el borde de 1px
 * que separa cada superficie. Sin el borde, el mismo color se ve plano.
 */

// ---------- Oscuro: escala de grises de GitHub ----------

val GhNegro = Color(0xFF010409)      // canvas.inset
val GhFondo = Color(0xFF0D1117)      // canvas.default
val GhSuperficie = Color(0xFF161B22) // canvas.subtle, las tarjetas
val GhElevado = Color(0xFF21262D)    // neutral 7
val GhBorde = Color(0xFF30363D)      // border.default
val GhBordeTenue = Color(0xFF21262D) // border.muted
val GhGrisMedio = Color(0xFF6E7681)  // neutral 4
val GhTextoTenue = Color(0xFF8B949E) // fg.muted
val GhTexto = Color(0xFFE6EDF3)      // fg.default

// ---------- Oscuro: colores con significado ----------

val GhAzul = Color(0xFF58A6FF)            // accent.fg — enlaces y acciones
val GhAzulFondo = Color(0xFF183051)       // accent.muted
val GhAzulTexto = Color(0xFFCAE8FF)       // accent.subtle sobre fondo azul
val GhAzulProfundo = Color(0xFF0C2D6B)

val GhMorado = Color(0xFFA371F7)          // done.fg
val GhMoradoFondo = Color(0xFF2D2144)
val GhMoradoTexto = Color(0xFFE2D4FF)
val GhMoradoProfundo = Color(0xFF2B1560)

val GhRosa = Color(0xFFDB61A2)            // sponsors.fg
val GhRosaFondo = Color(0xFF3C1D30)
val GhRosaTexto = Color(0xFFFFD3EC)
val GhRosaProfundo = Color(0xFF51173C)

val GhVerde = Color(0xFF3FB950)           // success.fg
val GhVerdeFondo = Color(0xFF12331B)
val GhVerdeTexto = Color(0xFFAFF5B4)

val GhAmarillo = Color(0xFFD29922)        // attention.fg
val GhAmarilloFondo = Color(0xFF342A0E)
val GhAmarilloTexto = Color(0xFFF8E3A1)

val GhRojo = Color(0xFFF85149)            // danger.fg
val GhRojoFondo = Color(0xFF49141A)
val GhRojoTexto = Color(0xFFFFDCD7)
val GhRojoProfundo = Color(0xFF67060C)

val GhNaranja = Color(0xFFDB6D28)         // severe.fg

// ---------- Claro: tema claro de GitHub ----------

val GhcBlanco = Color(0xFFFFFFFF)         // canvas.default
val GhcSuperficie = Color(0xFFF6F8FA)     // canvas.subtle
val GhcElevado = Color(0xFFEAEEF2)        // neutral 2
val GhcBorde = Color(0xFFD1D9E0)          // border.default
val GhcBordeTenue = Color(0xFFD8DEE4)     // border.muted
val GhcGrisMedio = Color(0xFF818B98)
val GhcTextoTenue = Color(0xFF59636E)     // fg.muted
val GhcTexto = Color(0xFF1F2328)          // fg.default

val GhcAzul = Color(0xFF0969DA)           // accent.fg
val GhcAzulFondo = Color(0xFFDDF4FF)      // accent.subtle
val GhcAzulTexto = Color(0xFF0A3069)

val GhcMorado = Color(0xFF8250DF)         // done.fg
val GhcMoradoFondo = Color(0xFFFBEFFF)
val GhcMoradoTexto = Color(0xFF3C1E70)

val GhcRosa = Color(0xFFBF3989)           // sponsors.fg
val GhcRosaFondo = Color(0xFFFFEFF7)
val GhcRosaTexto = Color(0xFF6B1F57)

val GhcVerde = Color(0xFF1A7F37)          // success.fg
val GhcVerdeFondo = Color(0xFFDAFBE1)
val GhcVerdeTexto = Color(0xFF003D16)

val GhcAmarillo = Color(0xFF9A6700)       // attention.fg
val GhcAmarilloFondo = Color(0xFFFFF8C5)
val GhcAmarilloTexto = Color(0xFF4D2D00)

val GhcRojo = Color(0xFFD1242F)           // danger.fg
val GhcRojoFondo = Color(0xFFFFEBE9)
val GhcRojoTexto = Color(0xFF82071E)

val GhcNaranja = Color(0xFFBC4C00)        // severe.fg

// ---------- Acentos de categoría ----------
//
// Son los colores con los que GitHub pinta sus etiquetas. Cada categoría de
// producto recibe uno de forma estable y se dibuja como franja en el borde
// izquierdo de la tarjeta.

val AcentosOscuro = listOf(
    GhAzul,
    GhMorado,
    GhVerde,
    GhAmarillo,
    GhRosa,
    GhNaranja,
)

val AcentosClaro = listOf(
    GhcAzul,
    GhcMorado,
    GhcVerde,
    GhcAmarillo,
    GhcRosa,
    GhcNaranja,
)
