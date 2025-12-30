package com.example.landnv4

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.landnv4.domain.astro.AstroMath
import com.example.landnv4.domain.astro.TimeUtil
import com.example.landnv4.domain.geo.UtmConverter
import com.example.landnv4.domain.geo.UtmParser
import com.example.landnv4.domain.model.Observer
import com.example.landnv4.domain.model.Star
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StarPickDialog(
    star: Star,
    onDismiss: () -> Unit,
    onCompute: (utm12: String, zone: Int, northHemisphere: Boolean) -> Unit
) {
    var utm by remember { mutableStateOf("") }
    var zone by remember { mutableStateOf("36") }
    var northHemisphere by remember { mutableStateOf(true) }

    var resultText by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(star.name) },
        text = {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Enter your UTM coordinate (12 digits) to compute star angle from your position.")

                OutlinedTextField(
                    value = utm,
                    onValueChange = { utm = it; error = null; resultText = null },
                    label = { Text("UTM (12 digits)") },
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = zone,
                        onValueChange = { zone = it.filter { ch -> ch.isDigit() }; error = null; resultText = null },
                        label = { Text("Zone") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Column(Modifier.weight(1f)) {
                        Text("Hemisphere", style = MaterialTheme.typography.labelMedium)
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Switch(
                                checked = northHemisphere,
                                onCheckedChange = { northHemisphere = it; error = null; resultText = null }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(if (northHemisphere) "North" else "South")
                        }
                    }
                }

                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
                if (resultText != null) {
                    Text(resultText!!)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                try {
                    val z = zone.toIntOrNull() ?: error("Zone required")
                    val utmObj = UtmParser.parse12Digits(utm, z, northHemisphere)
                    val (lat, lon) = UtmConverter.toLatLonWgs84(utmObj)
                    val observer = Observer(latDeg = lat, lonDeg = lon)

                    val horiz = AstroMath.starToHorizontal(star, observer, TimeUtil.nowUtc())

                    val mils = horiz.azimuthDeg * (6400.0 / 360.0)

                    resultText =
                        "Observer: %.5f, %.5f\nAzimuth: %.2f° (≈ %d mils)\nAltitude: %.2f°"
                            .format(lat, lon, horiz.azimuthDeg, mils.roundToInt(), horiz.altitudeDeg)

                    onCompute(utm, z, northHemisphere)
                } catch (e: Exception) {
                    error = e.message ?: "Failed to compute"
                }
            }) { Text("Compute") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
