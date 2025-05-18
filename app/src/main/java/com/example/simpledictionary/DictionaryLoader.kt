package com.example.simpledictionary

import android.content.Context

object DictionaryLoader {
    fun loadDictionary(context: Context): Map<String, String> {
        val inputStream = context.assets.open("dictionary.txt")
        val lines = inputStream.bufferedReader().readLines()
        val map = mutableMapOf<String, String>()

        for (line in lines) {
            val parts = line.split(":")
            if (parts.size == 2) {
                val word = parts[0].trim().lowercase()
                val definition = parts[1].trim()
                map[word] = definition
            }
        }

        return map
    }
}