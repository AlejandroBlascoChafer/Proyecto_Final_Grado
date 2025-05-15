package com.example.proyecto_final_grado.utils

import android.content.Context
import android.widget.TextView
import io.noties.markwon.Markwon
import io.noties.markwon.html.HtmlPlugin

object MarkdownUtils {
    private var markwon: Markwon? = null

    fun setMarkdownText(context: Context, textView: TextView, markdownText: String?) {
        if (markwon == null) {
            markwon = Markwon.builder(context)
                .usePlugin(HtmlPlugin.create())
                .build()
        }
        markwon?.setMarkdown(textView, markdownText ?: "Sin descripción disponible")
    }
}

