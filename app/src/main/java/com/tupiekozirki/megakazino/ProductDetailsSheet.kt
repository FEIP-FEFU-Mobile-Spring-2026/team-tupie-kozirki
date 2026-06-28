package com.tupiekozirki.megakazino

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProductDetailsSheet(
    private val product: Product,
    private val onAddToCart: (Product, Size) -> Unit,
) : BottomSheetDialogFragment() {
    private var selectedSize: Size? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.dialog_product_details, container, false)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageView>(R.id.detailsImage).load(product.imageUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_product_placeholder)
            error(R.drawable.ic_product_placeholder)
        }
        view.findViewById<TextView>(R.id.detailsName).text = product.name
        view.findViewById<TextView>(R.id.detailsDescription).text = product.longDescription

        val tagGroup = view.findViewById<ChipGroup>(R.id.tagGroup)
        tagGroup.removeAllViews()
        product.tags.forEach { tagName ->
            val chip =
                Chip(requireContext()).apply {
                    text = tagName
                    chipMinHeight = 24f
                    textSize = 10f
                    chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.light_beige)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.brand_brown))
                    chipStrokeWidth = 0f
                    isClickable = false
                }
            tagGroup.addView(chip)
        }

        val sizeGroup = view.findViewById<ChipGroup>(R.id.sizeGroup)
        sizeGroup.removeAllViews()
        product.sizes.forEach { size ->
            val chip =
                Chip(requireContext()).apply {
                    text = size.name
                    isCheckable = true
                    isCheckedIconVisible = false
                    chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.size_chip_background)
                    setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.size_text_selector))
                    chipStrokeWidth = 0f
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            selectedSize = size
                        }
                    }
                }
            sizeGroup.addView(chip)
        }

        view.findViewById<ImageButton>(R.id.btnInfo).setOnClickListener {
            val info =
                """
                Материал: ${product.material}
                Вес: ${product.weight}
                Сезон: ${product.season}
                Страна производства: ${product.countryOfOrigin}
                """.trimIndent()

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Характеристики")
                .setMessage(info)
                .setPositiveButton("Ок", null)
                .show()
        }

        val btnAdd = view.findViewById<Button>(R.id.btnAddToCart)
        btnAdd.text = "В корзину · ${product.priceInKopecks.toRubles()}"

        btnAdd.setOnClickListener {
            val size = selectedSize
            if (size == null) {
                Toast.makeText(requireContext(), "Выберите размер", Toast.LENGTH_SHORT).show()
            } else {
                onAddToCart(product, size)
                Toast.makeText(requireContext(), "Добавлено в корзину", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }
    }
}
