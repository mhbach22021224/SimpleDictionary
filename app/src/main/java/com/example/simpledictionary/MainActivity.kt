package com.example.simpledictionary

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var dictionaryEntries: List<DictionaryEntry>
    private lateinit var historyAdapter: ArrayAdapter<String>
    private val searchHistory = mutableListOf<String>() // Danh sách lịch sử

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.search_input)
        val resultView = findViewById<TextView>(R.id.textViewDefinition)
        val historyListView = findViewById<ListView>(R.id.history_list)

        // Load dữ liệu từ file txt
        dictionaryEntries = DictionaryLoader.loadDictionary(this)
        val words = dictionaryEntries.map { it.word }

        // Adapter gợi ý với filter chính xác
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

        // Adapter cho lịch sử
        historyAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, searchHistory)
        historyListView.adapter = historyAdapter

        // Khi chọn từ trong gợi ý
        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedWord = adapter.getItem(position)?.lowercase()
            searchAndDisplay(selectedWord, resultView)
        }

        // Khi nhấn nút Search
        val button = findViewById<Button>(R.id.search_button)
        button.setOnClickListener {
            val query = autoCompleteTextView.text.toString().trim().lowercase()
            searchAndDisplay(query, resultView)
        }

        // Khi chọn từ trong lịch sử
        historyListView.setOnItemClickListener { _, _, position, _ ->
            val word = searchHistory[position]
            autoCompleteTextView.setText(word)
            searchAndDisplay(word, resultView)
        }
    }

    // Hàm xử lý tìm kiếm và cập nhật UI
    private fun searchAndDisplay(query: String?, resultView: TextView) {
        if (query.isNullOrBlank()) return

        val entry = dictionaryEntries.find { it.word == query }
        if (entry != null) {
            resultView.text = entry.definition

            // Thêm vào lịch sử nếu chưa có
            if (!searchHistory.contains(query)) {
                searchHistory.add(0, query) // thêm vào đầu danh sách
                historyAdapter.notifyDataSetChanged()
            }
        } else {
            resultView.text = "Không tìm thấy từ '$query'"
        }
    }
}
