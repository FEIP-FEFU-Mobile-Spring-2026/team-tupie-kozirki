package com.tupiekozirki.megakazino

sealed class CatalogState {
    data object Loading : CatalogState()

    data class Content(
        val categories: List<Category>,
        val products: List<Product>,
        val isRefreshing: Boolean = false,
        val isOffline: Boolean = false,
    ) : CatalogState()

    data class Error(val message: String) : CatalogState()
}
