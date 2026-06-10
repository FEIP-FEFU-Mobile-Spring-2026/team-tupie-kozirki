package com.tupiekozirki.megakazino

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class CatalogViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProductRepository(application)

    private val _state = MutableLiveData<CatalogState>(CatalogState.Loading)
    val state: LiveData<CatalogState> = _state

    private var allProducts: List<Product> = emptyList()

    init {
        loadData()
    }

    fun loadData() {
        _state.value = CatalogState.Loading

        try {
            val response = repository.getCatalog()
            allProducts = response.items

            val newCategory = Category(id = "cat_new", name = "Новинки")

            val finalCategories = mutableListOf(newCategory)
            finalCategories.addAll(response.categories)

            val newProducts = allProducts.filter { it.tags.contains("New") }
            _state.value = CatalogState.Content(
                categories = finalCategories,
                products = newProducts
            )

        } catch (e: Exception) {
            _state.value = CatalogState.Error("Ошибка загрузки данных: ${e.message}")
        }
    }

    fun filterByCategory(categoryId: String) {
        val currentState = _state.value
        if (currentState is CatalogState.Content) {
            val filteredProducts = if (categoryId == "cat_new") {
                allProducts.filter { it.tags.contains("New") }
            } else {
                allProducts.filter { it.categoryId == categoryId }
            }
            _state.value = currentState.copy(products = filteredProducts)
        }
    }
}