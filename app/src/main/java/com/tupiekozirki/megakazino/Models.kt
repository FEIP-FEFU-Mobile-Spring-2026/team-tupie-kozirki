package com.tupiekozirki.megakazino

data class CatalogResponse(
    val categories: List<Category>,
    val items: List<Product>,
)

data class Category(
    val id: String,
    val name: String,
)

data class Size(
    val id: String,
    val name: String,
)

data class Product(
    val id: String,
    val name: String,
    val shortDescription: String,
    val longDescription: String,
    val priceInKopecks: Long,
    val imageUrl: String,
    val tags: List<String>,
    val sizes: List<Size>,
    val categoryId: String,
    val material: String,
    val weight: String,
    val season: String,
    val countryOfOrigin: String,
)
