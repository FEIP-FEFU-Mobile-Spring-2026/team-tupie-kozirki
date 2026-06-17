package com.tupiekozirki.megakazino

import android.content.Context
import com.google.gson.Gson

class ProductRepository(private val context: Context) {
    fun getCatalog(): CatalogResponse {
        val jsonString = context.assets.open("products.json").bufferedReader().use { it.readText() }

        return Gson().fromJson(jsonString, CatalogResponse::class.java)
    }
}
