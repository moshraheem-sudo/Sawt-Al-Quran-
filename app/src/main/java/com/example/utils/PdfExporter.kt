package com.example.utils

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.local.AyahEntity

object PdfExporter {

    fun exportSurahToPdf(context: Context, surahName: String, ayahs: List<AyahEntity>) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val webView = WebView(context)
        
        fun Int.toArabicNumerals(): String {
            val arabicNumerals = arrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
            return this.toString().map { if (it.isDigit()) arabicNumerals[it - '0'] else it }.joinToString("")
        }

        val textUthmani = ayahs.joinToString(" ") { it.textUthmani + " \u06DD" + it.ayahNumber.toArabicNumerals() }
        
        val htmlDocument = """
            <!DOCTYPE html>
            <html lang="ar" dir="rtl">
            <head>
                <meta charset="UTF-8">
                <style>
                    @page { margin: 2cm; }
                    body { 
                        font-family: 'serif'; 
                        direction: rtl; 
                        text-align: justify; 
                        line-height: 2.2; 
                        font-size: 28px; 
                        padding: 20px; 
                        color: #000;
                    }
                    .title { 
                        text-align: center; 
                        font-size: 38px; 
                        font-weight: bold; 
                        margin-bottom: 40px; 
                        color: #1a5f3d;
                        border-bottom: 2px solid #1a5f3d;
                        padding-bottom: 10px;
                        display: inline-block;
                    }
                    .center-wrapper {
                        text-align: center;
                    }
                    .content {
                        text-align: justify;
                        text-justify: inter-word;
                    }
                </style>
            </head>
            <body>
                <div class="center-wrapper">
                    <div class="title">سورة $surahName</div>
                </div>
                <div class="content">$textUthmani</div>
            </body>
            </html>
        """.trimIndent()

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val printAdapter = view.createPrintDocumentAdapter("سورة_$surahName")
                printManager.print("سورة_$surahName", printAdapter, PrintAttributes.Builder().build())
            }
        }
        
        webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null)
    }
}
