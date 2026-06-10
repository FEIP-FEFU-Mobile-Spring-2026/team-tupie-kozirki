package com.tupiekozirki.megakazino

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: CatalogViewModel
    private val adapter = ProductAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализируем ViewModel
        viewModel = ViewModelProvider(this)[CatalogViewModel::class.java]

        // Находим наши вьюшки
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        val errorLayout: LinearLayout = findViewById(R.id.errorLayout)
        val errorText: TextView = findViewById(R.id.errorText)
        val btnRetry: Button = findViewById(R.id.btnRetry)

        // Настраиваем список
        recyclerView.adapter = adapter

        // Кнопка "Повторить"
        btnRetry.setOnClickListener {
            viewModel.loadData()
        }

        // Слушаем клики по табам (категориям)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Вытаскиваем ID категории из тега (мы положим его туда ниже)
                val categoryId = tab?.tag as? String
                if (categoryId != null) {
                    viewModel.filterByCategory(categoryId)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Подписываемся на изменения состояний из ViewModel
        viewModel.state.observe(this) { state ->
            when (state) {
                is CatalogState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    errorLayout.visibility = View.GONE
                }
                is CatalogState.Error -> {
                    progressBar.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                    errorLayout.visibility = View.VISIBLE
                    errorText.text = state.message
                }
                is CatalogState.Content -> {
                    progressBar.visibility = View.GONE
                    errorLayout.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE

                    // Отдаем товары в адаптер
                    adapter.submitList(state.products)

                    // Если табы еще не добавлены - добавляем их
                    if (tabLayout.tabCount == 0) {
                        state.categories.forEach { category ->
                            val tab = tabLayout.newTab().setText(category.name)
                            tab.tag = category.id // Сохраняем ID категории в тег таба
                            tabLayout.addTab(tab)
                        }
                        for (i in 0 until tabLayout.tabCount) {
                            val tabView = (tabLayout.getChildAt(0) as android.view.ViewGroup).getChildAt(i)
                            val p = tabView.layoutParams as android.view.ViewGroup.MarginLayoutParams
                            p.setMargins(10, 0, 10, 0) // Отступы между кнопками
                            tabView.requestLayout()
                        }
                    }
                }
            }
        }
    }
}