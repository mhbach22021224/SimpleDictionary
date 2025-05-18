package com.example.simpledictionary

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var dictionary = DictionaryLoader.loadDictionary(this)

        val editText = findViewById<EditText>(R.id.search_input)
        val button = findViewById<Button>(R.id.search_button)
        val resultView = findViewById<TextView>(R.id.textViewDefinition)

        button.setOnClickListener {
            val input = editText.text.toString().trim().lowercase()
            val definition = dictionary[input]
            if (definition != null) {
                resultView.text = "$definition"
            } else {
                resultView.text = "Không tìm thấy từ '$input'"
            }
        }
    }
}