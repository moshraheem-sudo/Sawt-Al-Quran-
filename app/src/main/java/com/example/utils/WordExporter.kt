package com.example.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.local.AyahEntity
import java.io.File
import java.io.FileOutputStream

object WordExporter {

    fun exportSurahToWord(context: Context, surahName: String, ayahs: List<AyahEntity>) {
        fun Int.toArabicNumerals(): String {
            val arabicNumerals = arrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
            return this.toString().map { if (it.isDigit()) arabicNumerals[it - '0'] else it }.joinToString("")
        }

        val textUthmani = ayahs.joinToString(" ") { it.textUthmani + " \u06DD" + it.ayahNumber.toArabicNumerals() }
        
        val htmlContent = """
            <html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:word' xmlns='http://www.w3.org/TR/REC-html40'>
            <head>
                <meta charset="utf-8">
                <style>
                    body { 
                        font-family: 'Amiri', 'Traditional Arabic', serif; 
                        direction: rtl; 
                        text-align: justify; 
                        line-height: 2.2; 
                        font-size: 28px; 
                        color: #000;
                    }
                    .title { 
                        text-align: center; 
                        font-size: 38px; 
                        font-weight: bold; 
                        margin-bottom: 40px; 
                        color: #1a5f3d;
                    }
                    .content {
                        text-align: justify;
                        text-justify: inter-word;
                    }
                </style>
            </head>
            <body>
                <div class="title">سورة $surahName</div>
                <div class="content">$textUthmani</div>
            </body>
            </html>
        """.trimIndent()

        try {
            val fileName = "سورة_${surahName.replace(" ", "_")}.doc"
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use {
                it.write(htmlContent.toByteArray())
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/msword"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "سورة $surahName")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(Intent.createChooser(intent, "مشاركة السورة كملف Word"))

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
