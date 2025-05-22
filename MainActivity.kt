package com.example.simpledictionary

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech
    private var currentWord: String = ""
    private lateinit var dictionaryEntries: List<DictionaryEntry>
    private lateinit var historyAdapter: ArrayAdapter<String>
    private val searchHistory = mutableListOf<String>()
    private lateinit var speakButton: Button  // ✅ Khai báo biến speakButton
    private lateinit var tvSynonyms: TextView
    private lateinit var tvAntonyms: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvSynonyms = findViewById<TextView>(R.id.textViewSynonyms)
        tvAntonyms = findViewById<TextView>(R.id.textViewAntonyms)

        tts = TextToSpeech(this, this)

        val searchButton = findViewById<Button>(R.id.search_button)
        speakButton = findViewById(R.id.speak_button) // ✅ Gán biến speakButton
        val resultView = findViewById<TextView>(R.id.textViewDefinition)
        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.search_input)
        val historyListView = findViewById<ListView>(R.id.history_list)

        dictionaryEntries = DictionaryLoader.loadDictionary(this)
        val words = dictionaryEntries.map { it.word }

        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            words.toMutableList()
        ) {
            override fun getFilter(): Filter {
                return object : Filter() {
                    override fun performFiltering(constraint: CharSequence?): FilterResults {
                        val results = FilterResults()
                        if (!constraint.isNullOrBlank()) {
                            val filtered = words.filter {
                                it.startsWith(constraint.toString(), ignoreCase = true)
                            }
                            results.values = filtered
                            results.count = filtered.size
                        }
                        return results
                    }

                    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                        clear()
                        if (results?.values != null) {
                            @Suppress("UNCHECKED_CAST")
                            addAll(results.values as List<String>)
                        }
                        notifyDataSetChanged()
                    }

                    override fun convertResultToString(resultValue: Any?): CharSequence {
                        return resultValue as String
                    }
                }
            }
        }

        autoCompleteTextView.setAdapter(adapter)

        historyAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, searchHistory)
        historyListView.adapter = historyAdapter

        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedWord = adapter.getItem(position)?.lowercase()
            searchAndDisplay(selectedWord, resultView)
        }

        autoCompleteTextView.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                historyListView.visibility = if (s.isNullOrEmpty()) View.VISIBLE else View.GONE
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        searchButton.setOnClickListener {
            val query = autoCompleteTextView.text.toString().trim().lowercase()
            currentWord = query // ✅ lưu lại từ để phát âm
            searchAndDisplay(query, resultView)
        }

        historyListView.setOnItemClickListener { _, _, position, _ ->
            val word = searchHistory[position]
            autoCompleteTextView.setText(word)
            currentWord = word // ✅ lưu lại từ để phát âm
            searchAndDisplay(word, resultView)
        }

        speakButton.setOnClickListener {
            Toast.makeText(this, "Từ hiện tại: $currentWord", Toast.LENGTH_SHORT).show()
            if (currentWord.isNotEmpty()) {
                speak(currentWord)
            } else {
                Toast.makeText(this, "Không có từ để phát âm", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchAndDisplay(query: String?, resultView: TextView) {
        if (query.isNullOrBlank()) return

        val entry = dictionaryEntries.find { it.word == query }
        if (entry != null) {
            resultView.text = entry.definition
            tvSynonyms.text = "Đồng nghĩa: (đang tải...)"
            tvAntonyms.text = "Trái nghĩa: (đang tải...)"

            if (!searchHistory.contains(query)) {
                searchHistory.add(0, query)
                historyAdapter.notifyDataSetChanged()
            }

            DictionaryEnricher.enrichWithSynonymsAntonyms(entry) { enriched ->
                runOnUiThread {
                    tvSynonyms.text = "Đồng nghĩa: ${if (enriched.synonyms.isEmpty()) "Không có" else enriched.synonyms.joinToString(", ")}"
                    tvAntonyms.text = "Trái nghĩa: ${if (enriched.antonyms.isEmpty()) "Không có" else enriched.antonyms.joinToString(", ")}"
                }
            }
        } else {
            resultView.text = "Không tìm thấy từ '$query'"
            tvSynonyms.text = ""
            tvAntonyms.text = ""
        }
    }


    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Ngôn ngữ không được hỗ trợ!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "TTS không khả dụng!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun speak(word: String) {
        tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }

}
