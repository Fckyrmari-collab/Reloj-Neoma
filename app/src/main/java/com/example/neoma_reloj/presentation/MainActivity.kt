package com.example.neoma_reloj.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

/*
 * NEOMA · Reloj (Wear OS)
 * Pantalla 1: Ritmo cardíaco — con DATOS DE PRUEBA.
 *
 * Comparación con React:
 *   - Una función con @Composable  ≈  un componente de React.
 *   - Column / Row                 ≈  un <div> con display:flex (vertical / horizontal).
 *   - Modifier                     ≈  el "style" (tamaño, color de fondo, márgenes).
 *   - BasicText                    ≈  un <span> con texto.
 */

// ─────────────────────────────────────────────
// 1. Colores de NEOMA (modo oscuro)
// ─────────────────────────────────────────────
private val Fondo = Color(0xFF06100B)
private val Blanco = Color(0xFFFFFFFF)
private val Menta = Color(0xFFD2E6D8)
private val Gris = Color(0xFF9DB3A6)
private val VerdeLinea = Color(0xFF7FD1A0)
private val VerdeChip = Color(0xFF173A27)
private val VerdeChipTexto = Color(0xFFA6E6BE)
private val RojoCorazon = Color(0xFFF2777C)

// ─────────────────────────────────────────────
// 2. Los datos que mostrará la pantalla
//    (luego vendrán del sensor con Health Services)
// ─────────────────────────────────────────────
data class RitmoCardiaco(
    val bpm: Int,                 // latidos por minuto actuales
    val tendencia: List<Int>,     // últimos valores (para la mini gráfica)
    val haceMinutos: Int,         // cuándo fue la última medición
    val estado: String            // "Estable", "Elevado"...
)

val datosDePrueba = RitmoCardiaco(
    bpm = 78,
    tendencia = listOf(74, 76, 73, 77, 75, 79, 76, 80, 77, 79, 78),
    haceMinutos = 1,
    estado = "Estable"
)

// ─────────────────────────────────────────────
// 3. La Activity: el punto de entrada de la app
// ─────────────────────────────────────────────
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            PantallaRitmoCardiaco(datos = datosDePrueba)
        }
    }
}

// ─────────────────────────────────────────────
// 4. La pantalla completa
// ─────────────────────────────────────────────
@Composable
fun PantallaRitmoCardiaco(datos: RitmoCardiaco) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Título con el corazón
            Row(verticalAlignment = Alignment.CenterVertically) {
                Corazon(modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(6.dp))
                BasicText(
                    text = "Ritmo cardíaco",
                    style = TextStyle(color = Menta, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                )
            }

            // Número grande + "lpm"
            Row(verticalAlignment = Alignment.Bottom) {
                BasicText(
                    text = datos.bpm.toString(),
                    style = TextStyle(color = Blanco, fontSize = 52.sp, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.width(4.dp))
                BasicText(
                    text = "lpm",
                    modifier = Modifier.padding(bottom = 10.dp),
                    style = TextStyle(color = Gris, fontSize = 14.sp)
                )
            }

            // Mini gráfica de tendencia (no es un electrocardiograma)
            Tendencia(
                valores = datos.tendencia,
                modifier = Modifier
                    .width(120.dp)
                    .height(28.dp)
            )

            BasicText(
                text = "hace ${datos.haceMinutos} min",
                style = TextStyle(color = Gris, fontSize = 11.sp)
            )

            Spacer(modifier = Modifier.height(2.dp))

            EstadoChip(texto = datos.estado)
        }
    }
}

// ─────────────────────────────────────────────
// 5. Piezas pequeñas (como componentes hijos en React)
// ─────────────────────────────────────────────

/** Línea con los últimos valores del ritmo cardíaco. */
@Composable
fun Tendencia(valores: List<Int>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (valores.size < 2) return@Canvas

        val minimo = valores.min().toFloat()
        val maximo = valores.max().toFloat()
        val rango = (maximo - minimo).coerceAtLeast(1f)
        val margen = 4.dp.toPx()
        val alto = size.height - margen * 2
        val pasoX = size.width / (valores.size - 1)

        fun puntoY(valor: Int): Float = margen + alto - ((valor - minimo) / rango) * alto

        val linea = Path()
        valores.forEachIndexed { i, valor ->
            val x = i * pasoX
            val y = puntoY(valor)
            if (i == 0) linea.moveTo(x, y) else linea.lineTo(x, y)
        }

        drawPath(
            path = linea,
            color = VerdeLinea,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Punto al final = el valor más reciente
        drawCircle(
            color = VerdeLinea,
            radius = 3.dp.toPx(),
            center = Offset(size.width, puntoY(valores.last()))
        )
    }
}

/** Pastilla verde con el estado del paciente. */
@Composable
fun EstadoChip(texto: String) {
    Row(
        modifier = Modifier
            .background(VerdeChip, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(VerdeLinea, CircleShape)
        )
        Spacer(modifier = Modifier.width(5.dp))
        BasicText(
            text = texto,
            style = TextStyle(color = VerdeChipTexto, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        )
    }
}

/** Corazón dibujado con dos círculos y un triángulo. */
@Composable
fun Corazon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val r = w / 4
        drawCircle(RojoCorazon, radius = r, center = Offset(r, r))
        drawCircle(RojoCorazon, radius = r, center = Offset(w - r, r))
        val punta = Path().apply {
            moveTo(0f, r * 1.2f)
            lineTo(w, r * 1.2f)
            lineTo(w / 2, h)
            close()
        }
        drawPath(punta, RojoCorazon)
    }
}

// ─────────────────────────────────────────────
// 6. Vista previa dentro de Android Studio (sin emulador)
// ─────────────────────────────────────────────
@Preview(widthDp = 220, heightDp = 220, showBackground = true, backgroundColor = 0xFF06100B)
@Composable
fun VistaPreviaRitmo() {
    PantallaRitmoCardiaco(datos = datosDePrueba)
}