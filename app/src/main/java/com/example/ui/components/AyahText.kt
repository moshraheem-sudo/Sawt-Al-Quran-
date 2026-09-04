package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun AyahText(
    textUthmani: String,
    ayahNumber: Int,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 22.sp,
    color: Color = Color.Black,
    textAlign: TextAlign = TextAlign.Center
) {
    fun Int.toArabicNumerals(): String {
        val arabicNumerals = arrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        return this.toString().map { if (it.isDigit()) arabicNumerals[it - '0'] else it }.joinToString("")
    }

    val inlineContentId = "ayahMarker"
    val annotatedString = buildAnnotatedString {
        append(textUthmani)
        append("\u00A0") // Non-breaking space
        appendInlineContent(inlineContentId, "[marker]")
    }

    val inlineContent = mapOf(
        inlineContentId to InlineTextContent(
            Placeholder(
                width = (fontSize.value * 1.6f).sp,
                height = (fontSize.value * 1.6f).sp,
                placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
            )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val goldColor = Color(0xFFC6A052)
                    
                    // Outer decorated circle
                    drawCircle(
                        color = goldColor,
                        radius = size.minDimension / 2 - 2f,
                        style = Stroke(width = 3f)
                    )
                    // Inner circle
                    drawCircle(
                        color = goldColor,
                        radius = size.minDimension / 2 - 8f,
                        style = Stroke(width = 1.5f)
                    )
                    // 4 Decorative dots
                    val dotRadius = 2.5f
                    drawCircle(goldColor, radius = dotRadius, center = Offset(size.width/2, 2f))
                    drawCircle(goldColor, radius = dotRadius, center = Offset(size.width/2, size.height - 2f))
                    drawCircle(goldColor, radius = dotRadius, center = Offset(2f, size.height/2))
                    drawCircle(goldColor, radius = dotRadius, center = Offset(size.width - 2f, size.height/2))
                }
                Text(
                    text = ayahNumber.toArabicNumerals(),
                    fontSize = (fontSize.value * 0.45f).sp,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )

    Text(
        text = annotatedString,
        inlineContent = inlineContent,
        fontSize = fontSize,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.Serif,
        color = color,
        textAlign = textAlign,
        lineHeight = (fontSize.value * 1.6f).sp,
        modifier = modifier
    )
}
