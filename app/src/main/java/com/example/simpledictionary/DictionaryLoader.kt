package com.example.simpledictionary

import android.content.Context

data class DictionaryEntry(
    val word: String,
    val definition: String
)

object DictionaryLoader {
    fun loadDictionary(context: Context): List<DictionaryEntry> {
        val inputStream = context.assets.open("anhViet.txt")
        val lines = inputStream.bufferedReader().readLines()

        val entries = mutableListOf<DictionaryEntry>()

        var currentWord: String? = null
        val currentDefinition = StringBuilder()

        for (line in lines + "") { // Dòng trống cuối giúp xử lý mục cuối
            val trimmed = line.trim()

            if (trimmed.startsWith("@")) {
                // Lưu mục cũ nếu đang xử lý
                if (currentWord != null && currentDefinition.isNotEmpty()) {
                    entries.add(DictionaryEntry(currentWord!!, currentDefinition.toString().trim()))
                    currentDefinition.clear()
                }

                // Loại bỏ ký tự '@'
                val rawLine = trimmed.removePrefix("@")
                // Lấy từ chính đến trước dấu / hoặc hết chuỗi
                val wordOnly = rawLine.split("/", limit = 2).firstOrNull()?.trim()?.lowercase() ?: "unknown"

                currentWord = wordOnly
                currentDefinition.appendLine(rawLine) // thêm tiêu đề sạch
            } else if (trimmed.isBlank()) {
                if (currentWord != null && currentDefinition.isNotEmpty()) {
                    entries.add(DictionaryEntry(currentWord!!, currentDefinition.toString().trim()))
                    currentWord = null
                    currentDefinition.clear()
                }
            } else {
                currentDefinition.appendLine(trimmed)
            }
        }

        return entries
    }


}
