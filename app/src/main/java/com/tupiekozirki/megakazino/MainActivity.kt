package com.tupiekozirki.megakazino

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: CatalogViewModel
    private val adapter by lazy {
        ProductAdapter { product ->
            showProductDetails(product)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[CatalogViewModel::class.java]

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        val errorLayout: LinearLayout = findViewById(R.id.errorLayout)
        val errorText: TextView = findViewById(R.id.errorText)
        val btnRetry: Button = findViewById(R.id.btnRetry)
        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNavigation)

        recyclerView.adapter = adapter

        btnRetry.setOnClickListener {
            viewModel.loadData()
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_catalog -> true
                R.id.nav_cart -> {
                    Toast.makeText(this, "Корзина скоро будет!", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val categoryId = tab?.tag as? String
                if (categoryId != null) {
                    viewModel.filterByCategory(categoryId)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

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

                    adapter.submitList(state.products)

                    if (tabLayout.tabCount == 0) {
                        state.categories.forEach { category ->
                            val tab = tabLayout.newTab().setText(category.name)
                            tab.tag = category.id
                            tabLayout.addTab(tab)
                        }

                        val savedId = viewModel.getSelectedCategoryId()
                        for (i in 0 until tabLayout.tabCount) {
                            if (tabLayout.getTabAt(i)?.tag == savedId) {
                                tabLayout.getTabAt(i)?.select()
                                break
                            }
                        }

                        val tabStrip = tabLayout.getChildAt(0) as ViewGroup
                        for (i in 0 until tabStrip.childCount) {
                            val tabView = tabStrip.getChildAt(i)
                            val p = tabView.layoutParams as ViewGroup.MarginLayoutParams
                            p.setMargins(10, 0, 10, 0)
                            tabView.requestLayout()
                        }
                    }
                }
            }
        }
    }
    private fun showProductDetails(product: Product) {
        val sheet = ProductDetailsSheet(product)
        sheet.show(supportFragmentManager, "ProductDetails")
    }
}
