package com.prismpdf.editor

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.prismpdf.editor.ui.screens.PrismPDFHomeScreen
import org.junit.Rule
import org.junit.Test

class PrismPDFUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun verifyLiquidGlassHomeUiLayoutHierarchy() {
        // Hydrate the master home screen workspace layout structure
        composeTestRule.setContent {
            PrismPDFHomeScreen()
        }

        // Validate the existence of identity text tokens inside the screen tree
        composeTestRule.onNodeWithText("PrismPDF").assertExists()
        composeTestRule.onNodeWithText("Advanced Sandbox").assertExists()
        composeTestRule.onNodeWithText("Standard Toolkit").assertExists()

        // Assert that the developer configuration infrastructure card loads successfully
        composeTestRule.onNodeWithText("Emam Hossain (Emii31)").assertExists()
    }
}
