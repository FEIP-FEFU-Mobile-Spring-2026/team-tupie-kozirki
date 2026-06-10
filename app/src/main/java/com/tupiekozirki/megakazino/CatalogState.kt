package com.tupiekozirki.megakazino

sealed class CatalogState {
    object Loading : CatalogState()

    data class Content(
        val categories: List<Category>,
        val products: List<Product>
    ) : CatalogState()

    data class Error(val message: String) : CatalogState()
}