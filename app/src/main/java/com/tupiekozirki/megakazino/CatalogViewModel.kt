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
    private val keyCategory = "selected_category_id"

    private val _state = MutableLiveData<CatalogState>(CatalogState.Loading)
    val state: LiveData<CatalogState> = _state

    private var allProducts: List<Product> = emptyList()

    init {
        loadData()
    }

    fun loadData() {
        _state.value = CatalogState.Loading
        repository.loadCatalog(
            object : ProductRepository.CatalogCallback {
                override fun onCache(catalog: CatalogResponse) {
                    showCatalog(
                        response = catalog,
                        isRefreshing = true,
                        isOffline = false,
                    )
                }

                override fun onRemote(catalog: CatalogResponse) {
                    showCatalog(
                        response = catalog,
                        isRefreshing = false,
                        isOffline = false,
                    )
                }

                override fun onError(
                    message: String,
                    hasCache: Boolean,
                ) {
                    if (hasCache) {
                        val currentState = _state.value
                        if (currentState is CatalogState.Content) {
                            _state.value =
                                currentState.copy(
                                    isRefreshing = false,
                                    isOffline = true,
                                )
                        }
                    } else {
                        _state.value = CatalogState.Error("Ошибка загрузки каталога: $message")
                    }
                }
            },
        )
    }

    fun filterByCategory(categoryId: String) {
        savedStateHandle[keyCategory] = categoryId

        val currentState = _state.value
        if (currentState is CatalogState.Content) {
            _state.value = currentState.copy(products = filterProducts(categoryId))
        }
    }

    fun getSelectedCategoryId(): String {
        return savedStateHandle.get<String>(keyCategory) ?: NEW_CATEGORY_ID
    }

    private fun showCatalog(
        response: CatalogResponse,
        isRefreshing: Boolean,
        isOffline: Boolean,
    ) {
        allProducts = response.items

        val categories =
            buildList {
                add(Category(id = NEW_CATEGORY_ID, name = "Новинки"))
                addAll(response.categories)
            }

        val savedCategoryId = savedStateHandle.get<String>(keyCategory) ?: NEW_CATEGORY_ID
        _state.value =
            CatalogState.Content(
                categories = categories,
                products = filterProducts(savedCategoryId),
                isRefreshing = isRefreshing,
                isOffline = isOffline,
            )
    }

    private fun filterProducts(categoryId: String): List<Product> {
        return if (categoryId == NEW_CATEGORY_ID) {
            allProducts.filter { it.tags.contains("New") }
        } else {
            allProducts.filter { it.categoryId == categoryId }
        }
    }

    companion object {
        private const val NEW_CATEGORY_ID = "cat_new"
    }
}
