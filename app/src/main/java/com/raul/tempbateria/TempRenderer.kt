package com.raul.tempbateria

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

/** Dibuja la temperatura con dígitos de 7 segmentos, borde negro y fondo transparente. */
object TempRenderer {

    // Segmentos encendidos para cada dígito (a=arriba, b=arriba-dcha, c=abajo-dcha, d=abajo, e=abajo-izq, f=arriba-izq, g=medio)
    private val SEGMENTOS = mapOf(
        0 to "abcdef",
        1 to "bc",
        2 to "abged",
        3 to "abgcd",
        4 to "fgbc",
        5 to "afgcd",
        6 to "afgedc",
        7 to "abc",
        8 to "abcdefg",
        9 to "abcdfg"
    )

    /** Azul hasta 15 °C, verde en 30 °C, rojo desde 45 °C. Son valores orientativos. */
    fun colorPara(temp: Double): Int {
        val azul = intArrayOf(45, 123, 255)
        val verde = intArrayOf(34, 197, 94)
        val rojo = intArrayOf(239, 68, 68)
        val c = when {
            temp <= 15.0 -> azul
            temp <= 30.0 -> mezcla(azul, verde, (temp - 15.0) / 15.0)
            temp <= 45.0 -> mezcla(verde, rojo, (temp - 30.0) / 15.0)
            else -> rojo
        }
        return Color.rgb(c[0], c[1], c[2])
    }

    private fun mezcla(a: IntArray, b: IntArray, f: Double): IntArray {
        val k = f.coerceIn(0.0, 1.0)
        return IntArray(3) { i -> (a[i] + (b[i] - a[i]) * k).toInt() }
    }

    fun dibujar(temp: Double, ancho: Int, alto: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888)
        val c = Canvas(bitmap)

        val numero = Math.round(temp).toInt().coerceIn(0, 99)
        val decenas = if (numero >= 10) numero / 10 else -1   // sin cero delante, como en los Casio
        val unidades = numero % 10

        val h = alto * 0.80f
        val w = h * 0.55f
        val t = h * 0.14f
        val borde = t * 0.22f
        val hueco = w * 0.22f
        val r = t * 0.55f
        val totalW = w * 2 + hueco + r * 2.6f
        val x0 = (ancho - totalW) / 2f
        val y0 = (alto - h) / 2f
        val color = colorPara(temp)

        dibujarDigito(c, decenas, x0, y0, w, h, t, borde, color)
        dibujarDigito(c, unidades, x0 + w + hueco, y0, w, h, t, borde, color)

        // Símbolo de grados: aro pequeño arriba a la derecha
        val gx = x0 + 2 * w + hueco + r * 1.6f
        val gy = y0 + r * 1.4f
        val pNegro = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = r * 0.7f + borde * 2
            this.color = Color.BLACK
        }
        val pColor = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = r * 0.7f
            this.color = color
        }
        c.drawCircle(gx, gy, r, pNegro)
        c.drawCircle(gx, gy, r, pColor)

        return bitmap
    }

    private fun dibujarDigito(
        c: Canvas,
        digito: Int,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        t: Float,
        borde: Float,
        color: Int
    ) {
        if (digito < 0) return
        val segs = SEGMENTOS[digito] ?: return

        val rects = mapOf(
            'a' to RectF(x + t, y, x + w - t, y + t),
            'b' to RectF(x + w - t, y + t, x + w, y + h / 2),
            'c' to RectF(x + w - t, y + h / 2, x + w, y + h - t),
            'd' to RectF(x + t, y + h - t, x + w - t, y + h),
            'e' to RectF(x, y + h / 2, x + t, y + h - t),
            'f' to RectF(x, y + t, x + t, y + h / 2),
            'g' to RectF(x + t, y + h / 2 - t / 2, x + w - t, y + h / 2 + t / 2)
        )

        val pBorde = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = borde * 2
            strokeJoin = Paint.Join.ROUND
            this.color = Color.BLACK
        }
        val pColor = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            this.color = color
        }

        for (s in segs) {
            val rect = rects[s] ?: continue
            c.drawRect(rect, pBorde)   // borde negro
            c.drawRect(rect, pColor)   // relleno de color
        }
    }
}
