package com.chimera.zpqmxr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

val KEYWORD_COLOR = Color(0xFFC678DD)
val MODIFIER_COLOR = Color(0xFFE5C07B)
val STRING_COLOR = Color(0xFF98C379)
val COMMENT_COLOR = Color(0xFF5C6370)
val DEFAULT_COLOR = Color(0xFFABB2BF)
val EDITOR_BG = Color(0xFF282C34)
val LINE_NUM_BG = Color(0xFF21252B)
val LINE_NUM_COLOR = Color(0xFF4B5263)

val DUCKY_KEYWORDS = listOf(
    "DELAY", "STRING", "ENTER", "REM", "GUI", "WINDOWS", "APP", "MENU", "SHIFT", "ALT", "CONTROL", "CTRL",
    "DOWNARROW", "DOWN", "LEFTARROW", "LEFT", "RIGHTARROW", "RIGHT", "UPARROW", "UP", "BREAK", "PAUSE",
    "CAPSLOCK", "DELETE", "END", "ESC", "ESCAPE", "HOME", "INSERT", "NUMLOCK", "PAGEUP", "PAGEDOWN", "PRINTSCREEN",
    "SPACE", "TAB"
)

val DUCKY_MODIFIERS = listOf("CTRL", "CONTROL", "SHIFT", "ALT", "GUI", "WINDOWS")

class DuckyScriptVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val annotatedString = buildAnnotatedString {
            val lines = text.text.split("\n")
            lines.forEachIndexed { index, line ->
                highlightLine(line)
                if (index < lines.size - 1) append("\n")
            }
        }
        return TransformedText(annotatedString, OffsetMapping.Identity)
    }

    private fun AnnotatedString.Builder.highlightLine(line: String) {
        if (line.trim().startsWith("REM", ignoreCase = true)) {
            withStyle(SpanStyle(color = COMMENT_COLOR)) {
                append(line)
            }
            return
        }

        val parts = line.split(" ", limit = 2)
        if (parts.isNotEmpty()) {
            val keyword = parts[0]
            val isKeyword = DUCKY_KEYWORDS.any { it.equals(keyword, ignoreCase = true) }
            val isModifier = DUCKY_MODIFIERS.any { it.equals(keyword, ignoreCase = true) }
            
            val color = when {
                isKeyword && !isModifier -> KEYWORD_COLOR
                isModifier -> MODIFIER_COLOR
                else -> DEFAULT_COLOR
            }
            withStyle(SpanStyle(color = color, fontWeight = if (isKeyword || isModifier) FontWeight.Bold else FontWeight.Normal)) {
                append(keyword)
            }
            
            if (parts.size > 1) {
                append(" ")
                val rest = parts[1]
                if (keyword.equals("STRING", ignoreCase = true)) {
                    withStyle(SpanStyle(color = STRING_COLOR)) {
                        append(rest)
                    }
                } else if (isModifier || isKeyword) {
                     val subParts = rest.split(" ")
                     subParts.forEachIndexed { i, p ->
                         val subColor = if (DUCKY_KEYWORDS.any { it.equals(p, ignoreCase = true) }) MODIFIER_COLOR else DEFAULT_COLOR
                         withStyle(SpanStyle(color = subColor)) {
                             append(p)
                         }
                         if (i < subParts.size - 1) append(" ")
                     }
                } else {
                    withStyle(SpanStyle(color = DEFAULT_COLOR)) {
                        append(rest)
                    }
                }
            }
        }
    }
}

@Composable
fun DuckyScriptEditor(
    script: String,
    onScriptChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = script,
                selection = TextRange(script.length)
            )
        )
    }

    LaunchedEffect(script) {
        if (script != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(text = script, selection = TextRange(script.length))
        }
    }

    var showAutocomplete by remember { mutableStateOf(false) }
    var autocompleteOptions by remember { mutableStateOf(listOf<String>()) }
    var autocompletePrefix by remember { mutableStateOf("") }
    
    LaunchedEffect(textFieldValue) {
        val cursorPosition = textFieldValue.selection.start
        val text = textFieldValue.text
        if (cursorPosition > 0 && cursorPosition <= text.length) {
            val textBeforeCursor = text.substring(0, cursorPosition)
            val currentLine = textBeforeCursor.substringAfterLast("\n")
            val words = currentLine.split(" ")
            if (words.isNotEmpty()) {
                val currentWord = words.last()
                if (currentWord.isNotEmpty() && currentWord.length >= 2) {
                    val suggestions = DUCKY_KEYWORDS.filter { 
                        it.startsWith(currentWord, ignoreCase = true) && !it.equals(currentWord, ignoreCase = true)
                    }
                    if (suggestions.isNotEmpty()) {
                        autocompleteOptions = suggestions.take(5)
                        autocompletePrefix = currentWord
                        showAutocomplete = true
                        return@LaunchedEffect
                    }
                }
            }
        }
        showAutocomplete = false
    }

    val linesCount = textFieldValue.text.count { it == '\n' } + 1
    val scrollState = rememberScrollState()

    Box(modifier = modifier
        .clip(RoundedCornerShape(12.dp))
        .background(EDITOR_BG)
        .padding(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .width(36.dp)
                    .fillMaxHeight()
                    .background(LINE_NUM_BG)
                    .padding(top = 12.dp, bottom = 12.dp, end = 6.dp),
                horizontalAlignment = Alignment.End
            ) {
                for (i in 1..linesCount) {
                    Text(
                        text = i.toString(),
                        color = LINE_NUM_COLOR,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.height(20.sp.value.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                 BasicTextField(
                    value = textFieldValue,
                    onValueChange = { 
                        textFieldValue = it
                        onScriptChange(it.text)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = DEFAULT_COLOR,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 20.sp
                    ),
                    cursorBrush = SolidColor(Color.White),
                    visualTransformation = DuckyScriptVisualTransformation(),
                )
                
                if (showAutocomplete) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 16.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF3B4048),
                        shadowElevation = 8.dp
                    ) {
                        Column {
                            autocompleteOptions.forEach { option ->
                                Text(
                                    text = option,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .clickable {
                                            val text = textFieldValue.text
                                            val cursor = textFieldValue.selection.start
                                            val prefixStart = text.substring(0, cursor).lastIndexOf(autocompletePrefix)
                                            if (prefixStart != -1) {
                                                val newText = text.substring(0, prefixStart) + option + text.substring(cursor)
                                                val newCursor = prefixStart + option.length
                                                textFieldValue = TextFieldValue(newText, TextRange(newCursor))
                                                onScriptChange(newText)
                                                showAutocomplete = false
                                            }
                                        }
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
