# -*- coding: utf-8 -*-
"""
Comprueba que el pathData de los vectores dibuje lo mismo que las primitivas
de Pillow. Implementa el arco elliptico de SVG (A) para poder rasterizar el
pathData y comparar pixel a pixel.

Sin esto, un flag de barrido mal puesto no se notaria hasta instalar la app.
"""
import math, re
from PIL import Image, ImageDraw, ImageChops
import generar_icono as ic

def arco(p0, rx, ry, rot, arco_grande, barrido, p1, pasos=180):
    """Arco de SVG -> lista de puntos. Implementa la conversion a parametros
    de centro del apendice F.6 de la especificacion."""
    x0, y0 = p0; x1, y1 = p1
    if (x0, y0) == (x1, y1):
        return []
    fi = math.radians(rot)
    dx2, dy2 = (x0 - x1) / 2.0, (y0 - y1) / 2.0
    x1p = math.cos(fi) * dx2 + math.sin(fi) * dy2
    y1p = -math.sin(fi) * dx2 + math.cos(fi) * dy2

    lam = x1p**2 / rx**2 + y1p**2 / ry**2
    if lam > 1:
        rx *= math.sqrt(lam); ry *= math.sqrt(lam)

    num = rx**2 * ry**2 - rx**2 * y1p**2 - ry**2 * x1p**2
    den = rx**2 * y1p**2 + ry**2 * x1p**2
    coef = math.sqrt(max(num / den, 0))
    if arco_grande == barrido:
        coef = -coef
    cxp, cyp = coef * rx * y1p / ry, -coef * ry * x1p / rx
    cx = math.cos(fi) * cxp - math.sin(fi) * cyp + (x0 + x1) / 2.0
    cy = math.sin(fi) * cxp + math.cos(fi) * cyp + (y0 + y1) / 2.0

    def ang(ux, uy, vx, vy):
        n = math.sqrt((ux**2 + uy**2) * (vx**2 + vy**2))
        c = max(-1.0, min(1.0, (ux * vx + uy * vy) / n))
        s = 1 if ux * vy - uy * vx >= 0 else -1
        return s * math.acos(c)

    th1 = ang(1, 0, (x1p - cxp) / rx, (y1p - cyp) / ry)
    dth = ang((x1p - cxp) / rx, (y1p - cyp) / ry, (-x1p - cxp) / rx, (-y1p - cyp) / ry)
    if not barrido and dth > 0:
        dth -= 2 * math.pi
    elif barrido and dth < 0:
        dth += 2 * math.pi

    return [(cx + rx * math.cos(fi) * math.cos(th1 + dth * i / pasos)
                - ry * math.sin(fi) * math.sin(th1 + dth * i / pasos),
             cy + rx * math.sin(fi) * math.cos(th1 + dth * i / pasos)
                + ry * math.cos(fi) * math.sin(th1 + dth * i / pasos))
            for i in range(1, pasos + 1)]

FICHA = re.compile(r"([MLAZ])([^MLAZ]*)")

def a_poligono(pathdata):
    pts, actual = [], (0.0, 0.0)
    for cmd, resto in FICHA.findall(pathdata):
        nums = [float(v) for v in re.findall(r"-?\d*\.?\d+", resto)]
        if cmd == "M":
            actual = (nums[0], nums[1]); pts.append(actual)
        elif cmd == "L":
            actual = (nums[0], nums[1]); pts.append(actual)
        elif cmd == "A":
            rx, ry, rot, gr, ba, x, y = nums[:7]
            seg = arco(actual, rx, ry, rot, int(gr), int(ba), (x, y))
            pts.extend(seg); actual = (x, y)
    return pts

LADO = 512
ESC = LADO / 108.0

def pinta(poligonos):
    img = Image.new("L", (LADO, LADO), 0)
    d = ImageDraw.Draw(img)
    for poly in poligonos:
        d.polygon([(x * ESC, y * ESC) for x, y in poly], fill=255)
    return img

# --- Version pathData (la que va al vector drawable) ---
desde_path = pinta([a_poligono(ic.MANGO), a_poligono(ic.CABEZA)])

# --- Version primitivas (la que va a los bitmaps) ---
prim = Image.new("L", (LADO, LADO), 0)
d = ImageDraw.Draw(prim)
def pt(p): return ((p[0]) * ESC, (p[1]) * ESC)
def bx(c, r): 
    c = pt(c); r = r * ESC
    return [c[0]-r, c[1]-r, c[0]+r, c[1]+r]
px, py = -ic.D[1], ic.D[0]
r = ic.RADIO_MANGO
d.polygon([pt((ic.T[0]+px*r, ic.T[1]+py*r)), pt((ic.H2[0]+px*r, ic.H2[1]+py*r)),
           pt((ic.H2[0]-px*r, ic.H2[1]-py*r)), pt((ic.T[0]-px*r, ic.T[1]-py*r))], fill=255)
d.ellipse(bx(ic.T, r), fill=255)
d.ellipse(bx(ic.H2, r), fill=255)
d.ellipse(bx(ic.H, ic.RADIO_EXT), fill=255)
d.ellipse(bx(ic.H, ic.RADIO_INT), fill=0)
d.pieslice(bx(ic.H, ic.RADIO_EXT*1.8), ic.ANG_D-ic.BOCA, ic.ANG_D+ic.BOCA, fill=0)

dif = ImageChops.difference(desde_path, prim)
distintos = sum(1 for v in dif.getdata() if v > 128)
total = LADO * LADO
print(f"pixeles distintos: {distintos} de {total}  ({100.0*distintos/total:.2f}%)")

Image.merge("RGB", (desde_path, prim, Image.new("L", (LADO, LADO), 0))).save("comparacion.png")
prim.save("previa_llave.png")
print("rojo = solo en el pathData, verde = solo en las primitivas, amarillo = ambos")
