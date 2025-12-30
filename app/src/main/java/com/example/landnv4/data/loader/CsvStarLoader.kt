package com.example.landnv4.data.loader

import android.content.Context
import com.example.landnv4.data.db.StarEntity
import java.io.BufferedReader
import java.io.InputStreamReader

object CsvStarLoader {

    fun loadHygStars(context: Context, fileName: String, maxRows: Int = 5000): List<StarEntity> {
        val reader = BufferedReader(InputStreamReader(context.assets.open(fileName)))
        val headerLine = reader.readLine() ?: return emptyList()
        val headers = splitCsvLine(headerLine).map { it.trim().trim('"') }

        fun idx(name: String): Int {
            val i = headers.indexOf(name)
            if (i < 0) error("CSV header missing required column: $name")
            return i
        }

        val idIdx = idx("id")
        val hipIdx = idx("hip")
        val properIdx = idx("proper")
        val raIdx = idx("ra")
        val decIdx = idx("dec")
        val magIdx = idx("mag")

        val out = ArrayList<StarEntity>(maxRows)

        while (true) {
            val line = reader.readLine() ?: break
            if (line.isBlank()) continue

            val cols = splitCsvLine(line)
            if (cols.size <= decIdx) continue

            val id = cols[idIdx].toIntOrNull() ?: continue
            val hip = cols.getOrNull(hipIdx)?.toIntOrNull()
            val proper = cols.getOrNull(properIdx)?.trim()?.ifBlank { null }
            val ra = cols[raIdx].toDoubleOrNull() ?: continue
            val dec = cols[decIdx].toDoubleOrNull() ?: continue
            val mag = cols.getOrNull(magIdx)?.toDoubleOrNull()

            out.add(
                StarEntity(
                    id = id,
                    hip = hip,
                    proper = proper,
                    ra = ra,
                    dec = dec,
                    mag = mag
                )
            )

            // if (out.size >= maxRows) break

        }

        return out
    }

    // Minimal CSV split supporting quotes (works for this dataset)
    private fun splitCsvLine(line: String): List<String> {
        val result = ArrayList<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when (c) {
                '"' -> inQuotes = !inQuotes
                ',' -> {
                    if (inQuotes) sb.append(c) else {
                        result.add(sb.toString().trim().trim('"'))
                        sb.setLength(0)
                    }
                }
                else -> sb.append(c)
            }
            i++
        }
        result.add(sb.toString().trim().trim('"'))
        return result
    }
}
