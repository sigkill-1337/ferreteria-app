# -*- coding: utf-8 -*-
"""
Genera el icono de la app: una llave de tuercas.

La geometria se define UNA vez en el espacio de 108 unidades del icono
adaptativo, y de ahi salen las dos representaciones:

  - pathData para los vector drawables (Android 8.0+)
  - bitmaps rasterizados con Pillow (Android 7.x, que no soporta adaptativos)

Asi no hay forma de que el icono se vea distinto segun la version de Android.
"""
import math, os
from PIL import Image, ImageDraw

RES = r"C:\Users\root\AndroidStudioProjects\ferreteria\app\src\main\res"

# ---------------------------------------------------------------------------
# Geometria, en el espacio de 108 del icono adaptativo.
#
# La zona segura es un circulo de 66 de diametro centrado: el sistema recorta
# todo lo que sobresalga, y cada launcher usa su propia mascara. Nada del dibujo
# puede pasar de radio 33 desde el centro.
# ---------------------------------------------------------------------------
C = (54.0, 54.0)
ANG = math.radians(225)              # eje de la llave: hacia arriba-izquierda
D = (math.cos(ANG), math.sin(ANG))   # en SVG la Y crece hacia abajo

DIST_CABEZA = 16.25   # del centro al centro de la cabeza
DIST_COLA   = 23.75   # del centro al extremo del mango
RADIO_MANGO = 6.5
RADIO_EXT   = 14.4    # exterior de la cabeza
RADIO_INT   = 7.9     # interior: el hueco de la tuerca
BOCA        = 33.0    # medio angulo de la abertura, en grados

H = (C[0] + D[0] * DIST_CABEZA, C[1] + D[1] * DIST_CABEZA)
T = (C[0] - D[0] * DIST_COLA,   C[1] - D[1] * DIST_COLA)

# El mango no llega hasta el centro de la cabeza: su tapa redondeada taparia el
# hueco de la tuerca. Termina a (RADIO_INT + RADIO_MANGO) de H, de modo que el
# borde de la tapa cae justo sobre el borde interior del anillo y ambos se
# funden sin invadir el hueco.
H2 = (H[0] - D[0] * (RADIO_INT + RADIO_MANGO),
      H[1] - D[1] * (RADIO_INT + RADIO_MANGO))

alcance = max(DIST_CABEZA + RADIO_EXT, DIST_COLA + RADIO_MANGO)
assert alcance < 33, f"se sale de la zona segura: {alcance:.1f} de 33"

def polar(centro, radio, grados):
    a = math.radians(grados)
    return (centro[0] + radio * math.cos(a), centro[1] + radio * math.sin(a))

ANG_D = math.degrees(ANG)
A1 = ANG_D + BOCA          # borde de la boca
A2 = ANG_D - BOCA + 360    # el otro borde, dando la vuelta larga

def n(v):
    return f"{v:.2f}".rstrip('0').rstrip('.')

def path_mango():
    """Capsula entre la cola y la cabeza: rectangulo con extremos redondeados."""
    px, py = -D[1], D[0]                       # perpendicular al eje
    r = RADIO_MANGO
    a1 = (T[0] + px * r, T[1] + py * r)
    b1 = (H2[0] + px * r, H2[1] + py * r)
    b2 = (H2[0] - px * r, H2[1] - py * r)
    a2 = (T[0] - px * r, T[1] - py * r)
    # Barrido 0, no 1: con la Y hacia abajo de SVG, el barrido positivo va en
    # sentido horario en pantalla y las tapas se curvarian hacia adentro,
    # comiendose los extremos del mango en vez de redondearlos.
    return (f"M{n(a1[0])},{n(a1[1])} L{n(b1[0])},{n(b1[1])} "
            f"A{n(r)},{n(r)} 0 0 0 {n(b2[0])},{n(b2[1])} "
            f"L{n(a2[0])},{n(a2[1])} "
            f"A{n(r)},{n(r)} 0 0 0 {n(a1[0])},{n(a1[1])} Z")

def path_cabeza():
    """Anillo abierto: la boca de la llave."""
    e1 = polar(H, RADIO_EXT, A1)
    e2 = polar(H, RADIO_EXT, A2)
    i2 = polar(H, RADIO_INT, A2)
    i1 = polar(H, RADIO_INT, A1)
    return (f"M{n(e1[0])},{n(e1[1])} "
            f"A{n(RADIO_EXT)},{n(RADIO_EXT)} 0 1 1 {n(e2[0])},{n(e2[1])} "
            f"L{n(i2[0])},{n(i2[1])} "
            f"A{n(RADIO_INT)},{n(RADIO_INT)} 0 1 0 {n(i1[0])},{n(i1[1])} Z")

MANGO, CABEZA = path_mango(), path_cabeza()

# ---------------------------------------------------------------------------
# Vector drawables
# ---------------------------------------------------------------------------
def vector_llave(color):
    return f'''<?xml version="1.0" encoding="utf-8"?>
<!-- Generado. La geometria vive en scripts del proyecto, no se edita a mano. -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="{color}"
        android:pathData="{MANGO}" />
    <path
        android:fillColor="{color}"
        android:pathData="{CABEZA}" />
</vector>
'''

FONDO = '''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <!-- canvas.subtle -> canvas.default del tema oscuro de GitHub -->
    <path android:pathData="M0,0h108v108h-108z">
        <aapt:attr name="android:fillColor">
            <gradient
                android:type="linear"
                android:startX="0"
                android:startY="0"
                android:endX="108"
                android:endY="108">
                <item android:offset="0" android:color="#FF1C2430" />
                <item android:offset="1" android:color="#FF0D1117" />
            </gradient>
        </aapt:attr>
    </path>
</vector>
'''

ADAPTATIVO = '''<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <!-- Capa para los iconos tematicos de Android 13+. Va en blanco plano: el
         sistema la tine con el color del tema del usuario. -->
    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />
</adaptive-icon>
'''

def escribe(ruta, contenido):
    os.makedirs(os.path.dirname(ruta), exist_ok=True)
    with open(ruta, 'w', encoding='utf-8') as f:
        f.write(contenido)
    print("  ", os.path.relpath(ruta, RES))

print("Vectores:")
escribe(os.path.join(RES, "drawable", "ic_launcher_background.xml"), FONDO)
escribe(os.path.join(RES, "drawable", "ic_launcher_foreground.xml"), vector_llave("#FF58A6FF"))
escribe(os.path.join(RES, "drawable", "ic_launcher_monochrome.xml"), vector_llave("#FFFFFFFF"))
escribe(os.path.join(RES, "mipmap-anydpi-v26", "ic_launcher.xml"), ADAPTATIVO)
escribe(os.path.join(RES, "mipmap-anydpi-v26", "ic_launcher_round.xml"), ADAPTATIVO)

# ---------------------------------------------------------------------------
# Bitmaps para Android 7.x, que no entiende iconos adaptativos.
#
# Se dibuja la misma geometria a 2048 px y se reduce con LANCZOS: el suavizado
# sale del reescalado, porque ImageDraw no tiene antialiasing propio.
# ---------------------------------------------------------------------------
LIENZO = 2048
ESCALA = 24.0          # 108-unidades -> px
AZUL = (88, 166, 255, 255)
BORRA = (0, 0, 0, 0)

DENSIDADES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

def a_px(p):
    return ((p[0] - C[0]) * ESCALA + LIENZO / 2,
            (p[1] - C[1]) * ESCALA + LIENZO / 2)

def caja(centro_px, radio_px):
    return [centro_px[0] - radio_px, centro_px[1] - radio_px,
            centro_px[0] + radio_px, centro_px[1] + radio_px]

def capa_llave():
    capa = Image.new("RGBA", (LIENZO, LIENZO), BORRA)
    d = ImageDraw.Draw(capa)

    t, h2, h = a_px(T), a_px(H2), a_px(H)
    r = RADIO_MANGO * ESCALA
    px, py = -D[1], D[0]

    # Mango: rectangulo con las dos tapas redondeadas.
    d.polygon([(t[0] + px * r, t[1] + py * r), (h2[0] + px * r, h2[1] + py * r),
               (h2[0] - px * r, h2[1] - py * r), (t[0] - px * r, t[1] - py * r)], fill=AZUL)
    d.ellipse(caja(t, r), fill=AZUL)
    d.ellipse(caja(h2, r), fill=AZUL)

    # Cabeza: disco solido, se le saca el hueco y luego la boca.
    d.ellipse(caja(h, RADIO_EXT * ESCALA), fill=AZUL)
    d.ellipse(caja(h, RADIO_INT * ESCALA), fill=BORRA)
    d.pieslice(caja(h, RADIO_EXT * ESCALA * 1.8), ANG_D - BOCA, ANG_D + BOCA, fill=BORRA)
    return capa

def fondo(redondo):
    capa = Image.new("RGBA", (LIENZO, LIENZO), BORRA)
    d = ImageDraw.Draw(capa)
    margen = 48
    caja_fondo = [margen, margen, LIENZO - margen, LIENZO - margen]

    if redondo:
        d.ellipse(caja_fondo, fill=(13, 17, 23, 255))
    else:
        d.rounded_rectangle(caja_fondo, radius=int(LIENZO * 0.22), fill=(13, 17, 23, 255))

    # Degradado diagonal, del mismo par de grises que el vector del fondo.
    grad = Image.new("RGBA", (LIENZO, LIENZO), BORRA)
    pg = grad.load()
    for y in range(0, LIENZO, 4):
        for x in range(0, LIENZO, 4):
            t = (x + y) / (2.0 * LIENZO)
            c = (int(28 + (13 - 28) * t), int(36 + (17 - 36) * t), int(48 + (23 - 48) * t), 255)
            for dy in range(4):
                for dx in range(4):
                    pg[x + dx, y + dy] = c
    salida = Image.new("RGBA", (LIENZO, LIENZO), BORRA)
    salida.paste(grad, (0, 0), capa)
    return salida

print("Bitmaps:")
llave = capa_llave()
for redondo, nombre in ((False, "ic_launcher"), (True, "ic_launcher_round")):
    base = fondo(redondo)
    base.alpha_composite(llave)
    for carpeta, lado in DENSIDADES.items():
        destino = os.path.join(RES, carpeta, nombre + ".webp")
        os.makedirs(os.path.dirname(destino), exist_ok=True)
        base.resize((lado, lado), Image.LANCZOS).save(
            destino, "WEBP", lossless=True, quality=100, method=6)
        print("  ", os.path.relpath(destino, RES), f"{lado}x{lado}")
