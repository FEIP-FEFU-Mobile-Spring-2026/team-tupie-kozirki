package com.tupiekozirki.megakazino

private const val NEW_TAG = "New"

fun withNewCategory(categories: List<Category>): List<Category> {
    return buildList {
        add(Category(id = CatalogConstants.NEW_CATEGORY_ID, name = "Новинки"))
        addAll(categories)
    }
}

fun filterProductsByCategory(
    products: List<Product>,
    categoryId: String,
): List<Product> {
    return if (categoryId == CatalogConstants.NEW_CATEGORY_ID) {
        products.filter { it.tags.contains(NEW_TAG) }
    } else {
        products.filter { it.categoryId == categoryId }
    }
}

fun calculateCartTotal(items: List<CartProduct>): Long {
    return items.sumOf { it.totalPriceInKopecks }
}

fun isValidOrderForm(
    name: String,
    email: String,
    hasCartItems: Boolean,
): Boolean {
    val trimmedName = name.trim()
    val trimmedEmail = email.trim()
    return hasCartItems &&
        trimmedName.isNotEmpty() &&
        Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(trimmedEmail)
}

object CatalogConstants {
    const val NEW_CATEGORY_ID = "cat_new"
}
