package com.tupiekozirki.megakazino

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load

class CartAdapter(
    private val onIncrease: (CartProduct) -> Unit,
    private val onDecrease: (CartProduct) -> Unit,
    private val onRemove: (CartProduct) -> Unit,
) : ListAdapter<CartProduct, CartAdapter.CartViewHolder>(CartDiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: CartViewHolder,
        position: Int,
    ) {
        val item = getItem(position)
        holder.bind(item)
        holder.increaseButton.setOnClickListener { onIncrease(item) }
        holder.decreaseButton.setOnClickListener { onDecrease(item) }
        holder.removeButton.setOnClickListener { onRemove(item) }
    }

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val image: ImageView = view.findViewById(R.id.cartProductImage)
        private val name: TextView = view.findViewById(R.id.cartProductName)
        private val size: TextView = view.findViewById(R.id.cartProductSize)
        private val price: TextView = view.findViewById(R.id.cartProductPrice)
        private val quantity: TextView = view.findViewById(R.id.cartQuantityText)
        val increaseButton: ImageButton = view.findViewById(R.id.cartIncreaseButton)
        val decreaseButton: ImageButton = view.findViewById(R.id.cartDecreaseButton)
        val removeButton: ImageButton = view.findViewById(R.id.cartRemoveButton)

        fun bind(item: CartProduct) {
            image.load(item.product.imageUrl) {
                crossfade(true)
                placeholder(android.R.color.darker_gray)
                error(android.R.drawable.ic_menu_report_image)
            }
            name.text = item.product.name
            size.text = item.size.name
            quantity.text = item.quantity.toString()
            price.text = item.totalPriceInKopecks.toRubles()
        }
    }

    class CartDiffCallback : DiffUtil.ItemCallback<CartProduct>() {
        override fun areItemsTheSame(
            oldItem: CartProduct,
            newItem: CartProduct,
        ): Boolean {
            return oldItem.product.id == newItem.product.id && oldItem.size.id == newItem.size.id
        }

        override fun areContentsTheSame(
            oldItem: CartProduct,
            newItem: CartProduct,
        ): Boolean {
            return oldItem == newItem
        }
    }
}
