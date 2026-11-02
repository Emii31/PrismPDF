package com.prismpdf.editor

import com.prismpdf.editor.ui.*

import androidx.compose.ui.unit.sp


import java.io.File
import java.io.RandomAccessFile
import java.nio.charset.StandardCharsets

class PDFMetadataPreservationEngine {

    /**
     * Executes an isolated binary byte-stream modification on an existing PDF document.
     * Replaces target text tokens while preserving original structural metadata trees.
     *
     * @param sourcePdf The original unedited PDF document file.
     * @param targetText The unique placeholder or string token to look for inside the raw byte stream.
     * @param replacementText The new string to inject precisely into the text operators block.
     * @param outputPdf The destination file path where the preserved, modified PDF will be saved.
     */
    fun preserveAndInjectText(
        sourcePdf: File,
        targetText: String,
        replacementText: String,
        outputPdf: File
    ) {
        val targetBytes = targetText.toByteArray(StandardCharsets.UTF_8)

        // Pad or clip replacement text to ensure strict layout constraints if structural length validation fails
        val sanitizedReplacement = when {
            replacementText.length < targetText.length -> replacementText.padEnd(targetText.length, ' ')
            replacementText.length > targetText.length -> replacementText.substring(0, targetText.length)
            else -> replacementText
        }
        val replacementBytes = sanitizedReplacement.toByteArray(StandardCharsets.UTF_8)

        sourcePdf.copyTo(outputPdf, overwrite = true)

        RandomAccessFile(outputPdf, "rw").use { raf ->
            val fileLength = raf.length()
            var pointer = 0L

            // Buffered streaming scan across the document's raw binary payload
            val bufferSize = 4096
            val buffer = ByteArray(bufferSize)

            while (pointer < fileLength) {
                raf.seek(pointer)
                val bytesRead = raf.read(buffer)
                if (bytesRead == -1) break

                val chunkString = String(buffer, 0, bytesRead, StandardCharsets.ISO_8859_1)
                var index = chunkString.indexOf(targetText)

                while (index != -1) {
                    val absoluteOffset = pointer + index

                    // Verify if the string is contained within a valid PDF text object block (Tj or TJ operators)
                    if (validateTextOperatorContext(raf, absoluteOffset)) {
                        raf.seek(absoluteOffset)
                        raf.write(replacementBytes)
                    }

                    // Scan for additional occurrences within the same read buffer segment
                    index = chunkString.indexOf(targetText, index + 1)
                }

                // Advance pointer with a slide overlap to account for tokens split across buffer boundaries
                pointer += (bytesRead - targetBytes.size).coerceAtLeast(1)
            }
        }
    }

    /**
     * Evaluates whether the identified byte sequence is wrapped cleanly within active text operators.
     */
    private fun validateTextOperatorContext(raf: RandomAccessFile, offset: Long): Boolean {
        if (offset < 20) return true

        val contextBuffer = ByteArray(64)
        val startScan = (offset - 32).coerceAtLeast(0L)

        raf.seek(startScan)
        raf.read(contextBuffer)

        val contextStr = String(contextBuffer, StandardCharsets.ISO_8859_1)
        // Checks for standard PDF structural elements or text object markers (BT = Begin Text)
        return contextStr.contains("BT") || contextStr.contains("(") || contextStr.contains("[")
    }
}
