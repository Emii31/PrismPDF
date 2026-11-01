package com.prismpdf.editor.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prismpdf.editor.graphics.PDFCoordinateMapper
import com.prismpdf.editor.ui.EditableTextElement

/**
 * Renders a dynamically positioned, glassmorphic text input component that anchors perfectly
 * to physical PDF points regardless of screen zoom or translation matrix shifts.
 */
@Composable
fun InteractiveTextOverlayWidget(
    textElement: EditableTextElement,
    mapper: PDFCoordinateMapper,
    onTextChangeCompleted: (String) -> Unit
) {
    var inputString by remember { mutableStateOf(textElement.text) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Dynamically re-map PDF element point origins to active canvas screen pixels
    val screenPosition = mapper.pdfToScreen(textElement.pdfX, textElement.pdfY)
    val currentZoomScale = mapper.getScale()

    // Request soft keyboard display immediately upon layout hydration
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .absoluteOffset(
                x = (screenPosition.x / mapper.getScale()).dp,
                y = (screenPosition.y / mapper.getScale()).dp
            )
    ) {
        BasicTextField(
            value = inputString,
            onValueChange = { updatedValue ->
                inputString = updatedValue
            },
            modifier = Modifier
                .wrapContentSize()
                .focusRequester(focusRequester)
                .border(1.dp, Color(0x8070A3FF)) // Soft ice blue alignment indicator frame
                .padding(2.dp),
            textStyle = TextStyle(
                color = Color.Black,
                // Automatically scales typography size with view matrix bounds
                fontSize = (textElement.fontSizePdfPoints * currentZoomScale).sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Default
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onTextChangeCompleted(inputString)
                }
            )
        )
    }
}
