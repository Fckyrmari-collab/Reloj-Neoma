package com.example.neoma_reloj.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.launch

/*
 * NEOMA · Reloj (Wear OS)
 * 5 pantallas que se recorren deslizando ARRIBA / ABAJO:
 *   1. Ritmo cardíaco   2. Oxígeno y pasos   3. Sueño
 *   4. Próxima toma     5. SOS
 * Todos los datos son DE PRUEBA. Luego vendrán del sensor y de la API.
 */

// ═════════════════════════════════════════════
// 1. COLORES DE NEOMA (modo oscuro)
// ═════════════════════════════════════════════
private val Fondo = Color(0xFF06100B)
private val Blanco = Color(0xFFFFFFFF)
private val Menta = Color(0xFFD2E6D8)
private val Gris = Color(0xFF9DB3A6)
private val GrisOscuro = Color(0xFF1F2E26)
private val PuntoApagado = Color(0xFF3B4A42)
private val VerdeLinea = Color(0xFF7FD1A0)
private val VerdeBorde = Color(0xFF3E6B54)
private val VerdeChip = Color(0xFF173A27)
private val VerdeChipTexto = Color(0xFFA6E6BE)
private val RojoCorazon = Color(0xFFF2777C)
private val RojoSos = Color(0xFFE5484D)
private val RojoSosClaro = Color(0xFFFFB3B5)
private val AnilloSos = Color(0xFF3A1416)
private val Azul = Color(0xFF7CC4D6)
private val Ambar = Color(0xFFE8B36A)
private val Lila = Color(0xFF6F62C7)
private val LilaClaro = Color(0xFFA99BF2)

// ═════════════════════════════════════════════
// 2. DATOS DE PRUEBA
// ═════════════════════════════════════════════
data class RitmoCardiaco(val bpm: Int, val tendencia: List<Int>, val haceMinutos: Int, val estado: String)
data class Oxigeno(val porcentaje: Int, val haceMinutos: Int)
data class Pasos(val actuales: Int, val meta: Int)
data class Sueno(val horas: Int, val minutos: Int, val ultimasNoches: List<Float>, val dias: List<String>, val metaHoras: Float)
data class Toma(val medicamento: String, val dosis: String, val hora: String)

val datosRitmo = RitmoCardiaco(
    bpm = 78,
    tendencia = listOf(74, 76, 73, 77, 75, 79, 76, 80, 77, 79, 78),
    haceMinutos = 1,
    estado = "Estable"
)
val datosOxigeno = Oxigeno(porcentaje = 98, haceMinutos = 12)
val datosPasos = Pasos(actuales = 4820, meta = 8000)
val datosSueno = Sueno(
    horas = 6, minutos = 40,
    ultimasNoches = listOf(7.2f, 6.5f, 5.1f, 6.8f, 4.9f, 5.3f, 6.7f),
    dias = listOf("J", "V", "S", "D", "L", "M", "Hoy"),
    metaHoras = 7f
)
val datosToma = Toma(medicamento = "Losartán", dosis = "50 mg · 1 tableta", hora = "20:00")

private const val TOTAL_PANTALLAS = 5

// ═════════════════════════════════════════════
// 3. PUERTA DE ENTRADA
// ═════════════════════════════════════════════
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            AppNeoma()
        }
    }
}

// ═════════════════════════════════════════════
// 4. LA APP: un "pasador" vertical de páginas
// ═════════════════════════════════════════════
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppNeoma() {
    // Recuerda en qué página estamos (0 a 4)
    val estadoPaginas = rememberPagerState(pageCount = { TOTAL_PANTALLAS })

    Box(modifier = Modifier.fillMaxSize().background(Fondo)) {
        VerticalPager(
            state = estadoPaginas,
            modifier = Modifier.fillMaxSize()
        ) { pagina ->
            when (pagina) {
                0 -> PantallaRitmoCardiaco(datosRitmo)
                1 -> PantallaOxigenoPasos(datosOxigeno, datosPasos)
                2 -> PantallaSueno(datosSueno)
                3 -> PantallaProximaToma(datosToma)
                else -> PantallaSos()
            }
        }

        // Puntitos a la derecha que muestran en qué pantalla vas
        IndicadorPaginas(
            estado = estadoPaginas,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
        )
    }
}

/** Marco común de todas las pantallas: fondo oscuro y contenido centrado. */
@Composable
fun PantallaBase(contenido: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Fondo),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            contenido()
        }
    }
}

// ═════════════════════════════════════════════
// 5. LAS 5 PANTALLAS
// ═════════════════════════════════════════════

// ── Pantalla 1: Ritmo cardíaco ──────────────
@Composable
fun PantallaRitmoCardiaco(datos: RitmoCardiaco) {
    PantallaBase {
        Titulo(texto = "Ritmo cardíaco") { Corazon(modifier = Modifier.size(12.dp)) }

        Row(verticalAlignment = Alignment.Bottom) {
            BasicText(datos.bpm.toString(), style = TextStyle(color = Blanco, fontSize = 52.sp, fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.width(4.dp))
            BasicText("lpm", modifier = Modifier.padding(bottom = 10.dp), style = TextStyle(color = Gris, fontSize = 14.sp))
        }

        Tendencia(valores = datos.tendencia, modifier = Modifier.width(120.dp).height(28.dp))

        BasicText("hace ${datos.haceMinutos} min", style = TextStyle(color = Gris, fontSize = 11.sp))
        Spacer(modifier = Modifier.height(2.dp))
        EstadoChip(texto = datos.estado)
    }
}

// ── Pantalla 2: Oxígeno y pasos ─────────────
@Composable
fun PantallaOxigenoPasos(oxigeno: Oxigeno, pasos: Pasos) {
    PantallaBase {
        Titulo(texto = "Oxígeno en sangre") {
            Box(modifier = Modifier.size(9.dp).background(Azul, CircleShape))
        }

        Row(verticalAlignment = Alignment.Bottom) {
            BasicText(oxigeno.porcentaje.toString(), style = TextStyle(color = Blanco, fontSize = 44.sp, fontWeight = FontWeight.Bold))
            BasicText("%", modifier = Modifier.padding(bottom = 8.dp, start = 2.dp), style = TextStyle(color = Gris, fontSize = 18.sp, fontWeight = FontWeight.SemiBold))
        }
        BasicText("Medido hace ${oxigeno.haceMinutos} min", style = TextStyle(color = Gris, fontSize = 11.sp))

        BotonContorno(texto = "Medir ahora (30 s)", alTocar = { /* Más adelante: pedir medición al sensor */ })

        Spacer(modifier = Modifier.height(6.dp))

        // Barra de pasos
        Column(modifier = Modifier.width(140.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                BasicText("Pasos", style = TextStyle(color = Menta, fontSize = 12.sp, fontWeight = FontWeight.SemiBold))
                BasicText("${conPuntos(pasos.actuales)} / ${conPuntos(pasos.meta)}", style = TextStyle(color = Blanco, fontSize = 11.sp))
            }
            BarraProgreso(fraccion = pasos.actuales.toFloat() / pasos.meta, color = Ambar)
        }
    }
}

// ── Pantalla 3: Sueño ───────────────────────
@Composable
fun PantallaSueno(datos: Sueno) {
    val nochesCortas = datos.ultimasNoches.count { it < datos.metaHoras }

    PantallaBase {
        Titulo(texto = "Sueño de anoche") {
            Box(modifier = Modifier.size(9.dp).background(LilaClaro, CircleShape))
        }

        Row(verticalAlignment = Alignment.Bottom) {
            BasicText("${datos.horas}", style = TextStyle(color = Blanco, fontSize = 38.sp, fontWeight = FontWeight.Bold))
            BasicText(" h ", modifier = Modifier.padding(bottom = 6.dp), style = TextStyle(color = Gris, fontSize = 14.sp))
            BasicText("${datos.minutos}", style = TextStyle(color = Blanco, fontSize = 38.sp, fontWeight = FontWeight.Bold))
            BasicText(" min", modifier = Modifier.padding(bottom = 6.dp), style = TextStyle(color = Gris, fontSize = 14.sp))
        }

        // Barras de las últimas 7 noches (naranja = menos de la meta)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
            datos.ultimasNoches.forEachIndexed { i, horas ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val color = when {
                        i == datos.ultimasNoches.lastIndex -> LilaClaro
                        horas < datos.metaHoras -> Ambar
                        else -> Lila
                    }
                    Box(
                        modifier = Modifier
                            .width(11.dp)
                            .height((horas / 9f * 44f).dp)
                            .background(color, RoundedCornerShape(3.dp))
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    BasicText(datos.dias[i], style = TextStyle(color = Gris, fontSize = 8.sp))
                }
            }
        }

        if (nochesCortas > 0) {
            Spacer(modifier = Modifier.height(2.dp))
            Chip(texto = "$nochesCortas noches < 7 h", fondo = Color(0xFF3A2C12), colorTexto = Color(0xFFF2C987))
        }
    }
}

// ── Pantalla 4: Próxima toma ────────────────
@Composable
fun PantallaProximaToma(toma: Toma) {
    // "Memoria" de la pantalla: ¿ya la marcó como tomada?
    var tomada by remember { mutableStateOf(false) }

    PantallaBase {
        Box(
            modifier = Modifier.size(34.dp).background(Menta, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            BasicText("💊", style = TextStyle(fontSize = 16.sp))
        }
        BasicText("PRÓXIMA TOMA · ${toma.hora}", style = TextStyle(color = Gris, fontSize = 10.sp, fontWeight = FontWeight.SemiBold))
        BasicText(toma.medicamento, style = TextStyle(color = Blanco, fontSize = 22.sp, fontWeight = FontWeight.Bold))
        BasicText(toma.dosis, style = TextStyle(color = Menta, fontSize = 12.sp))

        Spacer(modifier = Modifier.height(6.dp))

        if (tomada) {
            Chip(texto = "✓ Registrada", fondo = VerdeChip, colorTexto = VerdeChipTexto)
            BasicText(
                "Deshacer",
                modifier = Modifier.clickable { tomada = false }.padding(6.dp),
                style = TextStyle(color = Gris, fontSize = 11.sp)
            )
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BotonRelleno(texto = "✓ Tomada", alTocar = { tomada = true })
                BotonContorno(texto = "10 min", alTocar = { /* Más adelante: reprogramar el aviso */ })
            }
        }
    }
}

// ── Pantalla 5: SOS ─────────────────────────
@Composable
fun PantallaSos() {
    val progreso = remember { Animatable(0f) }      // de 0 (vacío) a 1 (lleno)
    var activado by remember { mutableStateOf(false) }
    val alcance = rememberCoroutineScope()

    PantallaBase {
        Box(
            modifier = Modifier
                .size(118.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            if (!activado) {
                                // Empieza a llenar el anillo durante 3 segundos
                                val carga = alcance.launch {
                                    progreso.animateTo(1f, animationSpec = tween(durationMillis = 3000, easing = LinearEasing))
                                    activado = true
                                }
                                val soltoBien = tryAwaitRelease()
                                // Si soltó antes de los 3 s, se cancela y el anillo se vacía
                                if (!activado || !soltoBien) {
                                    carga.cancel()
                                    if (!activado) alcance.launch { progreso.animateTo(0f, tween(300)) }
                                }
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Anillo de progreso
            Canvas(modifier = Modifier.fillMaxSize()) {
                val grosor = 5.dp.toPx()
                val esquina = Offset(grosor / 2, grosor / 2)
                val tamano = Size(size.width - grosor, size.height - grosor)
                drawArc(color = AnilloSos, startAngle = -90f, sweepAngle = 360f, useCenter = false, topLeft = esquina, size = tamano, style = Stroke(width = grosor))
                drawArc(color = RojoCorazon, startAngle = -90f, sweepAngle = 360f * progreso.value, useCenter = false, topLeft = esquina, size = tamano, style = Stroke(width = grosor, cap = StrokeCap.Round))
            }
            // Botón rojo
            Box(
                modifier = Modifier.size(94.dp).background(RojoSos, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                BasicText("SOS", style = TextStyle(color = Blanco, fontSize = 26.sp, fontWeight = FontWeight.Bold))
            }
        }

        if (activado) {
            BasicText("Alerta enviada (simulación)", style = TextStyle(color = RojoSosClaro, fontSize = 12.sp, fontWeight = FontWeight.SemiBold))
            BasicText(
                "Cancelar",
                modifier = Modifier
                    .clickable {
                        activado = false
                        alcance.launch { progreso.snapTo(0f) }
                    }
                    .padding(6.dp),
                style = TextStyle(color = Gris, fontSize = 12.sp)
            )
        } else {
            BasicText("Mantén presionado 3 s", style = TextStyle(color = RojoSosClaro, fontSize = 12.sp, fontWeight = FontWeight.SemiBold))
            BasicText(
                "Avisa a tu cuidador y\ncomparte tu ubicación",
                style = TextStyle(color = Gris, fontSize = 10.sp, textAlign = TextAlign.Center)
            )
        }
    }
}

// ═════════════════════════════════════════════
// 6. PIEZAS REUTILIZABLES
// ═════════════════════════════════════════════

/** Título con un icono pequeño a la izquierda. */
@Composable
fun Titulo(texto: String, icono: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icono()
        Spacer(modifier = Modifier.width(6.dp))
        BasicText(texto, style = TextStyle(color = Menta, fontSize = 13.sp, fontWeight = FontWeight.SemiBold))
    }
}

/** Pastilla de color con texto. */
@Composable
fun Chip(texto: String, fondo: Color, colorTexto: Color) {
    BasicText(
        texto,
        modifier = Modifier
            .background(fondo, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        style = TextStyle(color = colorTexto, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    )
}

/** Pastilla verde con un puntito: "● Estable". */
@Composable
fun EstadoChip(texto: String) {
    Row(
        modifier = Modifier
            .background(VerdeChip, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(6.dp).background(VerdeLinea, CircleShape))
        Spacer(modifier = Modifier.width(5.dp))
        BasicText(texto, style = TextStyle(color = VerdeChipTexto, fontSize = 12.sp, fontWeight = FontWeight.SemiBold))
    }
}

/** Botón verde relleno. */
@Composable
fun BotonRelleno(texto: String, alTocar: () -> Unit) {
    BasicText(
        texto,
        modifier = Modifier
            .background(VerdeLinea, RoundedCornerShape(50))
            .clickable { alTocar() }
            .padding(horizontal = 14.dp, vertical = 9.dp),
        style = TextStyle(color = Fondo, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    )
}

/** Botón con borde (sin relleno). */
@Composable
fun BotonContorno(texto: String, alTocar: () -> Unit) {
    BasicText(
        texto,
        modifier = Modifier
            .border(1.5.dp, VerdeBorde, RoundedCornerShape(50))
            .clickable { alTocar() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        style = TextStyle(color = Menta, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    )
}

/** Barra horizontal que se llena según la fracción (0 a 1). */
@Composable
fun BarraProgreso(fraccion: Float, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .background(GrisOscuro, RoundedCornerShape(50))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraccion.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(color, RoundedCornerShape(50))
        )
    }
}

/** Mini gráfica con los últimos valores. */
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
        drawPath(path = linea, color = VerdeLinea, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawCircle(color = VerdeLinea, radius = 3.dp.toPx(), center = Offset(size.width, puntoY(valores.last())))
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

/** Puntitos verticales: el de la página actual es más largo. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IndicadorPaginas(estado: PagerState, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 0 until TOTAL_PANTALLAS) {
            val actual = i == estado.currentPage
            val color = when {
                actual && i == TOTAL_PANTALLAS - 1 -> RojoCorazon
                actual -> Menta
                else -> PuntoApagado
            }
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(if (actual) 12.dp else 4.dp)
                    .background(color, RoundedCornerShape(50))
            )
        }
    }
}

/** 4820 → "4.820" */
fun conPuntos(numero: Int): String =
    numero.toString().reversed().chunked(3).joinToString(".").reversed()

// ═════════════════════════════════════════════
// 7. VISTA PREVIA (sin emulador)
// ═════════════════════════════════════════════
@Preview(widthDp = 220, heightDp = 220, showBackground = true, backgroundColor = 0xFF06100B)
@Composable
fun VistaPreviaRitmo() {
    PantallaRitmoCardiaco(datosRitmo)
}

@Preview(widthDp = 220, heightDp = 220, showBackground = true, backgroundColor = 0xFF06100B)
@Composable
fun VistaPreviaSos() {
    PantallaSos()
}