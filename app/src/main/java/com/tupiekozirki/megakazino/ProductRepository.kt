package com.tupiekozirki.megakazino

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class ProductRepository(private val context: Context) {
    private val dao = CatalogDatabase.getInstance(context).catalogDao()
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val gson = Gson()

    fun loadCatalog(callback: CatalogCallback) {
        executor.execute {
            val cachedCatalog = getCachedCatalog()
            if (cachedCatalog != null) {
                post { callback.onCache(cachedCatalog) }
            }

            try {
                val remoteCatalog = fetchCatalog()
                dao.replaceCatalog(
                    categories = remoteCatalog.toCategoryEntities(),
                    products = remoteCatalog.toProductEntities(),
                )
                post { callback.onRemote(remoteCatalog) }
            } catch (error: Exception) {
                post {
                    callback.onError(
                        message = error.message ?: "Network error",
                        hasCache = cachedCatalog != null,
                    )
                }
            }
        }
    }

    private fun getCachedCatalog(): CatalogResponse? {
        val categories = dao.getCategories().toCategories()
        val products = dao.getProducts().toProducts()

        if (categories.isEmpty() || products.isEmpty()) {
            return null
        }

        return CatalogResponse(
            categories = categories,
            items = products,
        )
    }

    private fun fetchCatalog(): CatalogResponse {
        val connection = URL(CATALOG_URL).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS
        connection.setRequestProperty("Authorization", "Bearer $TOKEN")

        try {
            if (connection.responseCode !in 200..299) {
                throw IOException("API error: ${connection.responseCode}")
            }

            val json = connection.inputStream.bufferedReader().use { it.readText() }
            return gson.fromJson(json, CatalogResponse::class.java)
        } finally {
            connection.disconnect()
        }
    }

    private fun post(action: () -> Unit) {
        mainHandler.post(action)
    }

    interface CatalogCallback {
        fun onCache(catalog: CatalogResponse)

        fun onRemote(catalog: CatalogResponse)

        fun onError(
            message: String,
            hasCache: Boolean,
        )
    }

    companion object {
        private const val CATALOG_URL = "https://fefu2026spring.deploy.feip.dev/catalog"
        private const val TOKEN = "Cmt7wdwFgDIi1_SRX8hlJIExs0jJKPr4axflLpExAxM"
        private const val TIMEOUT_MS = 10_000
    }
}
