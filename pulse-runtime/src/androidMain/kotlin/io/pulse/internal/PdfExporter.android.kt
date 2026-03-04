package io.pulse.internal

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import io.pulse.model.HttpTransaction
import io.pulse.util.formatBytes
import io.pulse.util.formatDuration
import io.pulse.util.formatTimestamp
import java.io.File
import java.io.FileOutputStream

internal actual fun generateTransactionsPdf(
    context: ShareContext,
    transactions: List<HttpTransaction>,
): String? {
    return try {
        val document = PdfDocument()
        val pageWidth = 595 // A4 width in points
        val pageHeight = 842 // A4 height in points
        val margin = 40f
        val lineHeight = 14f
        val maxContentWidth = pageWidth - (margin * 2)

        val titlePaint = Paint().apply {
            textSize = 16f
            isFakeBoldText = true
            color = android.graphics.Color.BLACK
            isAntiAlias = true
        }
        val headerPaint = Paint().apply {
            textSize = 12f
            isFakeBoldText = true
            color = android.graphics.Color.DKGRAY
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            textSize = 10f
            color = android.graphics.Color.BLACK
            isAntiAlias = true
        }
        val dimPaint = Paint().apply {
            textSize = 10f
            color = android.graphics.Color.GRAY
            isAntiAlias = true
        }

        var pageNumber = 1
        var currentPage: PdfDocument.Page? = null
        var canvas: Canvas? = null
        var yPos = 0f

        fun ensurePage(): Canvas {
            if (currentPage == null || yPos > pageHeight - margin - lineHeight) {
                currentPage?.let { document.finishPage(it) }
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber++).create()
                currentPage = document.startPage(pageInfo)
                canvas = currentPage!!.canvas
                yPos = margin + lineHeight
            }
            return canvas!!
        }

        fun drawLine(text: String, paint: Paint) {
            val c = ensurePage()
            // Truncate long text to fit on page
            val maxChars = (maxContentWidth / paint.measureText("a")).toInt().coerceAtLeast(10)
            val displayText = if (text.length > maxChars) text.take(maxChars - 3) + "..." else text
            c.drawText(displayText, margin, yPos, paint)
            yPos += lineHeight
        }

        fun drawWrappedLine(text: String, paint: Paint) {
            val lines = text.split("\n")
            for (line in lines) {
                drawLine(line, paint)
            }
        }

        fun addSpacer() {
            yPos += lineHeight / 2
        }

        // Title page
        drawLine("Pulse - Transaction Export", titlePaint)
        drawLine("${transactions.size} transaction(s)", dimPaint)
        addSpacer()

        transactions.forEachIndexed { index, tx ->
            drawLine("--- Transaction ${index + 1} / ${transactions.size} ---", headerPaint)
            addSpacer()

            drawLine("URL: ${tx.url}", bodyPaint)
            drawLine("Method: ${tx.method}", bodyPaint)
            drawLine("Status: ${tx.responseCode ?: "N/A"} ${tx.responseMessage ?: ""}", bodyPaint)
            drawLine("Duration: ${formatDuration(tx.duration)}", bodyPaint)
            drawLine("Time: ${formatTimestamp(tx.timestamp)}", bodyPaint)
            addSpacer()

            if (tx.requestHeaders.isNotEmpty()) {
                drawLine("Request Headers:", headerPaint)
                tx.requestHeaders.forEach { (key, value) ->
                    drawLine("  $key: $value", dimPaint)
                }
                addSpacer()
            }

            if (!tx.requestBody.isNullOrBlank()) {
                drawLine("Request Body:", headerPaint)
                val truncated = if (tx.requestBody.length > 500) {
                    tx.requestBody.take(500) + "... (truncated)"
                } else {
                    tx.requestBody
                }
                drawWrappedLine(truncated, dimPaint)
                addSpacer()
            }

            if (tx.responseHeaders.isNotEmpty()) {
                drawLine("Response Headers:", headerPaint)
                tx.responseHeaders.forEach { (key, value) ->
                    drawLine("  $key: $value", dimPaint)
                }
                addSpacer()
            }

            if (!tx.responseBody.isNullOrBlank()) {
                drawLine("Response Body:", headerPaint)
                val truncated = if (tx.responseBody.length > 500) {
                    tx.responseBody.take(500) + "... (truncated)"
                } else {
                    tx.responseBody
                }
                drawWrappedLine(truncated, dimPaint)
                addSpacer()
            }

            if (tx.error != null) {
                drawLine("Error: ${tx.error}", bodyPaint)
                addSpacer()
            }

            addSpacer()
        }

        // Finish last page
        currentPage?.let { document.finishPage(it) }

        val cacheDir = File(context.context.cacheDir, "pulse_export")
        cacheDir.mkdirs()
        val file = File(cacheDir, "export_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()

        file.absolutePath
    } catch (_: Exception) {
        null
    }
}
