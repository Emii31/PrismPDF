package com.prismpdf.editor

import com.prismpdf.editor.core.PDFMetadataPreservationEngine
import com.prismpdf.editor.util.PDFTestStreamGenerator
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class PDFPatchRestorationTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun verifyLosslessMetadataPreservationEngine() {
        val mockInputFile = tempFolder.newFile("mock_passport_input.pdf")
        val mockOutputFile = tempFolder.newFile("mock_passport_output.pdf")

        // Define strict fixed matching tokens to prevent xref allocation corruptions
        val placeholder = "MOCK_HOLDER_IDENTITY_TOKEN"
        val replacement = "EMAM_HOSSAIN_EMII31_TEST" // Padded matching length configurations

        // 1. Generate the clean test binary stream file on disk
        PDFTestStreamGenerator.createMockDocument(mockInputFile, placeholder)
        assertTrue("Mock source file setup failed", mockInputFile.exists())

        // 2. Execute the byte-manipulation engine over the mock document
        val systemEngine = PDFMetadataPreservationEngine()
        systemEngine.preserveAndInjectText(mockInputFile, placeholder, replacement, mockOutputFile)

        // 3. Extract the modified payload content to verify processing success
        val targetOutputBytes = mockOutputFile.readText(Charsets.ISO_8859_1)

        assertTrue("Injected token was not recovered inside the PDF Content stream", targetOutputBytes.contains(replacement))
        assertTrue("File trailer footprint broken", targetOutputBytes.contains("%%EOF"))
    }
}
