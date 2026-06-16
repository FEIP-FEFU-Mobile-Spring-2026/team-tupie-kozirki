package com.tupiekozirki.megakazino

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle

class CatalogViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {

    private val repository = ProductRepository(application)

    private val KEY_CATEGORY = "selected_category_id"

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

            val savedCategoryId = savedStateHandle.get<String>(KEY_CATEGORY) ?: "cat_new"
            val initialProducts = filterProducts(savedCategoryId)

            _state.value = CatalogState.Content(
                categories = finalCategories,
                products = initialProducts,
            )
        } catch (e: Exception) {
            _state.value = CatalogState.Error("Ошибка: ${e.message}")
        }
    }

    fun filterByCategory(categoryId: String) {
        savedStateHandle[KEY_CATEGORY] = categoryId

        val currentState = _state.value
        if (currentState is CatalogState.Content) {
            _state.value = currentState.copy(products = filterProducts(categoryId))
        }
    }

    fun getSelectedCategoryId(): String {
        return savedStateHandle.get<String>(KEY_CATEGORY) ?: "cat_new"
    }

    private fun filterProducts(categoryId: String): List<Product> {
        return if (categoryId == "cat_new") {
            allProducts.filter { it.tags.contains("New") }
        } else {
            allProducts.filter { it.categoryId == categoryId }
        }
    }
}
