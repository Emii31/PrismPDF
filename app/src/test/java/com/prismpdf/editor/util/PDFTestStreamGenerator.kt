package com.prismpdf.editor.util

import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

object PDFTestStreamGenerator {

    /**
     * Generates a fully compliant, uncompressed plain-text PDF containing a target placeholder token.
     * Use this mock file to validate that your lossless byte manipulation code modifies data streams safely.
     *
     * @param destinationFile The target workspace path where the generated document will be saved.
     * @param placeholderToken The raw text token (must match length limits) to inject into the PDF content stream.
     */
    fun createMockDocument(destinationFile: File, placeholderToken: String) {
        val contentStreamText = "BT /F1 12 Tf 72 712 Td ($placeholderToken) Tj ET"
        val streamLength = contentStreamText.toByteArray(StandardCharsets.UTF_8).size

        // Build string objects manually to maintain precise cross-reference table calculations
        val header = "%PDF-1.4\n%\n"

        val obj1 = "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n"
        val obj2 = "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n"
        val obj3 = "3 0 obj\n<< /Type /Page /Parent 2 0 R /Resources << /Font << /F1 << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> >> >> /MediaBox [0 0 612 792] /Contents 4 0 R >>\nendobj\n"

        val obj4Header = "4 0 obj\n<< /Length $streamLength >>\nstream\n"
        val obj4Footer = "\nendstream\nendobj\n"

        // Concatenate binary layouts to track accurate file offset markers
        val sb = StringBuilder()
        sb.append(header)

        val offsetObj1 = sb.length
        sb.append(obj1)

        val offsetObj2 = sb.length
        sb.append(obj2)

        val offsetObj3 = sb.length
        sb.append(obj3)

        val offsetObj4 = sb.length
        sb.append(obj4Header).append(contentStreamText).append(obj4Footer)

        val startXref = sb.length

        // Build the mandatory PDF cross-reference layout mapping object byte positions
        sb.append("xref\n")
        sb.append("0 5\n")
        sb.append("0000000000 65535 f \n")
        sb.append(String.format("%010d 00000 n \n", offsetObj1))
        sb.append(String.format("%010d 00000 n \n", offsetObj2))
        sb.append(String.format("%010d 00000 n \n", offsetObj3))
        sb.append(String.format("%010d 00000 n \n", offsetObj4))

        sb.append("trailer\n")
        sb.append("<< /Size 5 /Root 1 0 R >>\n")
        sb.append("startxref\n")
        sb.append(startXref).append("\n")
        sb.append("%%EOF")

        FileOutputStream(destinationFile).use { fos ->
            fos.write(sb.toString().toByteArray(StandardCharsets.ISO_8859_1))
        }
    }
}
