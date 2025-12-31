package com.example.landnv4

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RequiredInputsDialog(
    onConfirmed: (dateIso: String, timeHundredth: String, utm14: String) -> Unit
) {
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var utm by remember { mutableStateOf("") }

    val dateErr = remember(date) { HomeInputValidation.validateDateIso(date) }
    val timeErr = remember(time) { HomeInputValidation.validateTimeHundredth(time) }
    val utmErr  = remember(utm)  { HomeInputValidation.validateUtm14(utm) }

    val allValid = dateErr == null && timeErr == null && utmErr == null
            && date.isNotBlank() && time.isNotBlank() && utm.isNotBlank()

    AlertDialog(
        onDismissRequest = { /* Block dismiss */ },
        title = { Text("Enter required inputs") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    isError = dateErr != null,
                    singleLine = true
                )
                if (dateErr != null) Text(dateErr, color = MaterialTheme.colorScheme.error)

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time (HH:mm:ss.SS)") },
                    isError = timeErr != null,
                    singleLine = true
                )
                if (timeErr != null) Text(timeErr, color = MaterialTheme.colorScheme.error)

                OutlinedTextField(
                    value = utm,
                    onValueChange = { utm = it },
                    label = { Text("UTM (14 digits)") },
                    isError = utmErr != null,
                    singleLine = true
                )
                if (utmErr != null) Text(utmErr, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmed(date.trim(), time.trim(), utm.trim().replace(" ", "")) },
                enabled = allValid
            ) { Text("Continue") }
        },
        dismissButton = {
            // Optional: no dismiss at all, or provide "Exit app"
        }
    )
}
