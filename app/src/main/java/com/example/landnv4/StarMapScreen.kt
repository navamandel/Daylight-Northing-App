package com.example.landnv4

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.graphics.Color
import com.example.landnv4.domain.astro.AstroMath
import com.example.landnv4.domain.astro.TimeUtil
import com.example.landnv4.domain.model.Observer
import com.example.landnv4.domain.model.Star
import com.example.landnv4.ui.northing.starmap.StarMapViewModel
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StarMapScreen(vm: StarMapViewModel) {
    val ui by vm.ui.collectAsState()

    // For now: default observer in Israel-ish; user will input UTM AFTER selecting a star.
    // This observer is only for drawing the sky "right now".
    val drawObserver = remember { Observer(latDeg = 31.78, lonDeg = 35.22) } // Jerusalem-ish
    val utcNow = remember { TimeUtil.nowUtc() }

    var scale by remember { mutableStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }

    var selectedStar by remember { mutableStateOf<Star?>(null) }
    var showPickDialog by remember { mutableStateOf(false) }

    val points = remember(ui.stars, utcNow) {
        // Precompute horizontal coords for drawing
        ui.stars.mapNotNull { s ->
            val h = AstroMath.starToHorizontal(s, drawObserver, utcNow)
            if (h.altitudeDeg <= 0.0) null else StarDrawPoint(s, h.azimuthDeg, h.altitudeDeg)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Northing • Star Map (Offline)") })
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {

            when {
                ui.loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                ui.error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("Error: ${ui.error}")
                    }
                }
                else -> {
                    StarMapCanvas(
                        points = points,
                        scale = scale,
                        pan = pan,
                        onTransform = { zoomChange, panChange ->
                            scale = (scale * zoomChange).coerceIn(0.7f, 5f)
                            pan += panChange
                        },
                        onTap = { tapPos, size ->
                            val hit = pickNearest(points, tapPos, size, scale, pan)
                            if (hit != null) {
                                selectedStar = hit.star
                                showPickDialog = true
                            }
                        }
                    )
                }
            }

            if (showPickDialog && selectedStar != null) {
                StarPickDialog(
                    star = selectedStar!!,
                    onDismiss = { showPickDialog = false },
                    onCompute = { utm12, zone, northHemisphere ->
                        // You’ll compute in the dialog and show results there (or navigate).
                        // For now we keep dialog showing results inside it.
                    }
                )
            }
        }
    }
}

private data class StarDrawPoint(
    val star: Star,
    val azDeg: Double,
    val altDeg: Double
)

@Composable
private fun StarMapCanvas(
    points: List<StarDrawPoint>,
    scale: Float,
    pan: Offset,
    onTransform: (zoomChange: Float, panChange: Offset) -> Unit,
    onTap: (tapPos: Offset, canvasSize: Size) -> Unit
) {
    var intSize by remember { mutableStateOf(IntSize.Zero) }

    Canvas(

        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { intSize = it }   // ✅ capture layout size (IntSize)
            .pointerInput(Unit) {
                detectTransformGestures { _, panChange, zoomChange, _ ->
                    onTransform(zoomChange, panChange)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { pos ->
                    // ✅ convert IntSize -> Size
                    onTap(pos, Size(intSize.width.toFloat(), intSize.height.toFloat()))
                }
            }
    ) {
        drawRect(
            color = Color.Black,
            size = size
        )

        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f) + pan
        val radius = min(w, h) * 0.45f * scale

        drawCircle(
            color = Color(0xFF050814),
            radius = radius,
            center = center
        )

        points.forEach { p ->
            val xy = projectToDome(p.azDeg, p.altDeg, center, radius)

            drawCircle(
                color = Color.White,
                radius = 2.5f,
                center = xy
            )
        }

    }
}

private fun projectToDome(
    azDeg: Double,
    altDeg: Double,
    center: Offset,
    radius: Float
): Offset {
    // Clamp altitude into [-90, 90] just to keep projection stable
    val altClamped = altDeg.coerceIn(-90.0, 90.0)

    // Map altitude to [0..1] where 90 => center, 0 => edge, -90 => outside edge
    val t = ((90.0 - altClamped) / 90.0).toFloat()  // 0 at zenith, 1 at horizon, 2 at nadir

    val r = radius * t.coerceIn(0f, 1f)            // clamp to dome
    val azRad = Math.toRadians(azDeg)

    val x = center.x + (r * kotlin.math.sin(azRad)).toFloat()
    val y = center.y - (r * kotlin.math.cos(azRad)).toFloat()

    return Offset(x, y)
}


private fun pickNearest(
    points: List<StarDrawPoint>,
    tap: Offset,
    canvasSize: androidx.compose.ui.geometry.Size,
    scale: Float,
    pan: Offset
): StarDrawPoint? {
    val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f) + pan
    val radius = min(canvasSize.width, canvasSize.height) * 0.45f * scale

    var best: StarDrawPoint? = null
    var bestDist = Float.MAX_VALUE
    val hitRadius = 24f // tap tolerance

    for (p in points) {
        val xy = projectToDome(p.azDeg, p.altDeg, center, radius)
        val d = (xy - tap).getDistance()
        if (d < bestDist) {
            bestDist = d
            best = p
        }
    }

    return if (bestDist <= hitRadius) best else null
}
