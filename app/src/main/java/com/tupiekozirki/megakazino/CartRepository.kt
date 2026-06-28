package com.tupiekozirki.megakazino

import android.content.Context
import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

data class CartProduct(
    val product: Product,
    val size: Size,
    val quantity: Int,
) {
    val totalPriceInKopecks: Long = product.priceInKopecks * quantity
}

class CartRepository(context: Context) {
    private val dao = CatalogDatabase.getInstance(context).catalogDao()
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun addToCart(
        productId: String,
        sizeId: String,
        callback: () -> Unit,
    ) {
        executor.execute {
            dao.addCartItem(productId, sizeId)
            post(callback)
        }
    }

    fun increase(
        productId: String,
        sizeId: String,
        callback: () -> Unit,
    ) {
        executor.execute {
            dao.addCartItem(productId, sizeId)
            post(callback)
        }
    }

    fun decrease(
        productId: String,
        sizeId: String,
        callback: () -> Unit,
    ) {
        executor.execute {
            dao.decreaseCartItem(productId, sizeId)
            post(callback)
        }
    }

    fun remove(
        productId: String,
        sizeId: String,
        callback: () -> Unit,
    ) {
        executor.execute {
            dao.removeCartItem(productId, sizeId)
            post(callback)
        }
    }

    fun clear(callback: () -> Unit) {
        executor.execute {
            dao.clearCart()
            post(callback)
        }
    }

    fun loadCart(callback: (List<CartProduct>, Int) -> Unit) {
        executor.execute {
            val productsById = dao.getProducts().toProducts().associateBy { it.id }
            val cartItems = dao.getCartItems()
            val cartProducts =
                cartItems.mapNotNull { item ->
                    val product = productsById[item.productId] ?: return@mapNotNull null
                    val size = product.sizes.firstOrNull { it.id == item.sizeId } ?: return@mapNotNull null
                    CartProduct(
                        product = product,
                        size = size,
                        quantity = item.quantity,
                    )
                }

            post {
                callback(cartProducts, cartItems.sumOf { it.quantity })
            }
        }
    }

    fun loadQuantity(callback: (Int) -> Unit) {
        executor.execute {
            val quantity = dao.getCartQuantity()
            post {
                callback(quantity)
            }
        }
    }

    private fun post(action: () -> Unit) {
        mainHandler.post(action)
    }
}
