package com.tupiekozirki.megakazino

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class BusinessLogicTest {
    @Test
    fun filterProductsByCategory_returnsOnlySelectedCategory() {
        val products = sampleProducts()

        val result = filterProductsByCategory(products, "cat_outerwear")

        assertEquals(listOf("coat"), result.map { it.id })
    }

    @Test
    fun filterProductsByCategory_returnsNewProductsForVirtualCategory() {
        val products = sampleProducts()

        val result = filterProductsByCategory(products, CatalogConstants.NEW_CATEGORY_ID)

        assertEquals(listOf("cardigan", "coat"), result.map { it.id })
    }

    @Test
    fun withNewCategory_prependsVirtualNewCategory() {
        val categories = listOf(Category(id = "cat_tops", name = "Tops"))

        val result = withNewCategory(categories)

        assertEquals(CatalogConstants.NEW_CATEGORY_ID, result.first().id)
        assertEquals("Tops", result[1].name)
    }

    @Test
    fun calculateCartTotal_sumsPriceWithQuantity() {
        val products = sampleProducts()
        val cart =
            listOf(
                CartProduct(products[0], products[0].sizes[0], quantity = 2),
                CartProduct(products[1], products[1].sizes[0], quantity = 1),
            )

        val total = calculateCartTotal(cart)

        assertEquals(32_968_00L, total)
    }

    @Test
    fun isValidOrderForm_requiresNameValidEmailAndCartItems() {
        assertTrue(isValidOrderForm("Anton", "anton@example.com", hasCartItems = true))
        assertFalse(isValidOrderForm("", "anton@example.com", hasCartItems = true))
        assertFalse(isValidOrderForm("Anton", "broken-email", hasCartItems = true))
        assertFalse(isValidOrderForm("Anton", "anton@example.com", hasCartItems = false))
    }

    @Test
    fun toRubles_formatsKopecksAsRubles() {
        Locale.setDefault(Locale.US)

        assertEquals("14 999 ₽", 1_499_900L.toRubles())
    }

    private fun sampleProducts(): List<Product> {
        return listOf(
            Product(
                id = "cardigan",
                name = "Cardigan",
                shortDescription = "Cotton cardigan",
                longDescription = "Cotton cardigan",
                priceInKopecks = 1_499_900L,
                imageUrl = "",
                tags = listOf("New"),
                sizes = listOf(Size(id = "xxl", name = "XXL")),
                categoryId = "cat_tops",
                material = "Cotton",
                weight = "300 g",
                season = "All season",
                countryOfOrigin = "Turkey",
            ),
            Product(
                id = "blazer",
                name = "Blazer",
                shortDescription = "Gray blazer",
                longDescription = "Gray blazer",
                priceInKopecks = 2_970_00L,
                imageUrl = "",
                tags = emptyList(),
                sizes = listOf(Size(id = "xxxl", name = "XXXL")),
                categoryId = "cat_jackets",
                material = "Wool",
                weight = "600 g",
                season = "Spring",
                countryOfOrigin = "Italy",
            ),
            Product(
                id = "coat",
                name = "Coat",
                shortDescription = "Warm coat",
                longDescription = "Warm coat",
                priceInKopecks = 9_000_00L,
                imageUrl = "",
                tags = listOf("New"),
                sizes = listOf(Size(id = "m", name = "M")),
                categoryId = "cat_outerwear",
                material = "Wool",
                weight = "900 g",
                season = "Winter",
                countryOfOrigin = "Poland",
            ),
        )
    }
}
