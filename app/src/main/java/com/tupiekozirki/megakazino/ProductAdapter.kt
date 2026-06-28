package com.tupiekozirki.megakazino

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class ProductAdapter(private val onItemClick: (Product) -> Unit) :
    ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ProductViewHolder,
        position: Int,
    ) {
        val product = getItem(position)
        holder.bind(product)
        holder.itemView.setOnClickListener { onItemClick(product) }
    }

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val productImage: ImageView = view.findViewById(R.id.productImage)
        private val productName: TextView = view.findViewById(R.id.productName)
        private val productDescription: TextView = view.findViewById(R.id.productDescription)
        private val productPrice: TextView = view.findViewById(R.id.productPrice)

        fun bind(product: Product) {
            productName.text = product.name
            productDescription.text = product.shortDescription

            productImage.load(product.imageUrl) {
                crossfade(true)
                placeholder(android.R.color.darker_gray)
                error(android.R.drawable.ic_menu_report_image)
            }
            val rubles = product.priceInKopecks / 100
            val symbols =
                DecimalFormatSymbols(Locale.getDefault()).apply {
                    groupingSeparator = ' '
                }
            val formatter = DecimalFormat("#,###", symbols)
            productPrice.text = "${formatter.format(rubles)} ₽"
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(
            oldItem: Product,
            newItem: Product,
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Product,
            newItem: Product,
        ): Boolean {
            return oldItem == newItem
        }
    }
}
