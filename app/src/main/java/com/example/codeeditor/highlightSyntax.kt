package com.example.codeeditor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

fun highlightSyntax(text: String, rules: SyntaxRules): AnnotatedString {
    return buildAnnotatedString {
        append(text)

        // 🔹 First, mark comments (so keywords inside comments are ignored)
        val commentRanges = mutableListOf<IntRange>()
        rules.comments.forEach { comment ->
            if (comment == "/*") {
                // Multi-line comment
                Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL).findAll(text).forEach { match ->
                    addStyle(
                        SpanStyle(color = Color(0xFF6A9955), fontStyle = FontStyle.Italic),
                        match.range.first,
                        match.range.last + 1
                    )
                    commentRanges.add(match.range)
                }
            } else {
                // Single-line comment
                Regex("${Regex.escape(comment)}.*").findAll(text).forEach { match ->
                    addStyle(
                        SpanStyle(color = Color(0xFF6A9955), fontStyle = FontStyle.Italic),
                        match.range.first,
                        match.range.last + 1
                    )
                    commentRanges.add(match.range)
                }
            }
        }

        // 🔹 Keywords (skip ranges inside comments)
        rules.keywords.forEach { keyword ->
            "\\b$keyword\\b".toRegex().findAll(text).forEach { match ->
                if (commentRanges.none { it.contains(match.range.first) }) {
                    addStyle(
                        SpanStyle(color = Color(0xFF569CD6), fontWeight = FontWeight.Bold),
                        match.range.first,
                        match.range.last + 1
                    )
                }
            }
        }

        // 🔹 Strings (skip inside comments)
        Regex("\".*?\"|'.*?'").findAll(text).forEach { match ->
            if (commentRanges.none { it.contains(match.range.first) }) {
                addStyle(
                    SpanStyle(color = Color(0xFFD69D85)),
                    match.range.first,
                    match.range.last + 1
                )
            }
        }

        // 🔹 Annotations (skip inside comments)
        rules.annotations.forEach { annotation ->
            "\\b$annotation\\b".toRegex().findAll(text).forEach { match ->
                if (commentRanges.none { it.contains(match.range.first) }) {
                    addStyle(
                        SpanStyle(color = Color(0xFFB000B0), fontWeight = FontWeight.Medium),
                        match.range.first,
                        match.range.last + 1
                    )
                }
            }
        }
    }
}
