package com.tupiekozirki.megakazino

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.PatternsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: CatalogViewModel
    private lateinit var cartRepository: CartRepository
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var tabLayout: TabLayout
    private lateinit var catalogRecyclerView: RecyclerView
    private lateinit var cartContainer: View
    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var cartEmptyText: TextView
    private lateinit var orderForm: View
    private lateinit var customerNameInput: EditText
    private lateinit var customerEmailInput: EditText
    private lateinit var orderCommentInput: EditText
    private lateinit var cartTotalText: TextView
    private lateinit var checkoutButton: Button
    private var hasCartItems = false

    private val adapter by lazy {
        ProductAdapter { product ->
            showProductDetails(product)
        }
    }
    private val cartAdapter by lazy {
        CartAdapter(
            onIncrease = { item ->
                cartRepository.increase(item.product.id, item.size.id) {
                    refreshCart()
                }
            },
            onDecrease = { item ->
                cartRepository.decrease(item.product.id, item.size.id) {
                    refreshCart()
                }
            },
            onRemove = { item ->
                cartRepository.remove(item.product.id, item.size.id) {
                    refreshCart()
                }
            },
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[CatalogViewModel::class.java]
        cartRepository = CartRepository(this)

        catalogRecyclerView = findViewById(R.id.recyclerView)
        tabLayout = findViewById(R.id.tabLayout)
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        val errorLayout: LinearLayout = findViewById(R.id.errorLayout)
        val errorText: TextView = findViewById(R.id.errorText)
        val btnRetry: Button = findViewById(R.id.btnRetry)
        bottomNav = findViewById(R.id.bottomNavigation)
        cartContainer = findViewById(R.id.cartContainer)
        cartRecyclerView = findViewById(R.id.cartRecyclerView)
        cartEmptyText = findViewById(R.id.cartEmptyText)
        orderForm = findViewById(R.id.orderForm)
        customerNameInput = findViewById(R.id.customerNameInput)
        customerEmailInput = findViewById(R.id.customerEmailInput)
        orderCommentInput = findViewById(R.id.orderCommentInput)
        cartTotalText = findViewById(R.id.cartTotalText)
        checkoutButton = findViewById(R.id.checkoutButton)
        val clearCartButton: ImageButton = findViewById(R.id.clearCartButton)

        catalogRecyclerView.adapter = adapter
        cartRecyclerView.adapter = cartAdapter

        btnRetry.setOnClickListener {
            viewModel.loadData()
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_catalog -> {
                    showCatalogScreen()
                    true
                }
                R.id.nav_cart -> {
                    showCartScreen()
                    true
                }
                else -> false
            }
        }

        clearCartButton.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Очистить корзину?")
                .setMessage("Все добавленные товары будут удалены.")
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Очистить") { _, _ ->
                    cartRepository.clear {
                        refreshCart()
                    }
                }
                .show()
        }

        checkoutButton.setOnClickListener {
            cartRepository.clear {
                refreshCart()
                MaterialAlertDialogBuilder(this)
                    .setTitle("Заказ успешно оформлен")
                    .setMessage("Подтверждение и чек отправили на вашу почту")
                    .setPositiveButton("Вернуться на главную") { _, _ ->
                        customerNameInput.text.clear()
                        customerEmailInput.text.clear()
                        orderCommentInput.text.clear()
                        bottomNav.selectedItemId = R.id.nav_catalog
                    }
                    .show()
            }
        }

        val formWatcher =
            object : TextWatcher {
                override fun beforeTextChanged(
                    text: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) = Unit

                override fun onTextChanged(
                    text: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {
                    updateCheckoutState()
                }

                override fun afterTextChanged(text: Editable?) = Unit
            }
        customerNameInput.addTextChangedListener(formWatcher)
        customerEmailInput.addTextChangedListener(formWatcher)

        tabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val categoryId = tab?.tag as? String
                    if (categoryId != null) {
                        viewModel.filterByCategory(categoryId)
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) = Unit

                override fun onTabReselected(tab: TabLayout.Tab?) = Unit
            },
        )

        viewModel.state.observe(this) { state ->
            when (state) {
                is CatalogState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    catalogRecyclerView.visibility = View.GONE
                    errorLayout.visibility = View.GONE
                }
                is CatalogState.Error -> {
                    progressBar.visibility = View.GONE
                    catalogRecyclerView.visibility = View.GONE
                    errorLayout.visibility = View.VISIBLE
                    errorText.text = state.message
                }
                is CatalogState.Content -> {
                    progressBar.visibility = if (state.isRefreshing) View.VISIBLE else View.GONE
                    errorLayout.visibility = View.GONE
                    catalogRecyclerView.visibility = if (cartContainer.visibility == View.VISIBLE) View.GONE else View.VISIBLE

                    adapter.submitList(state.products)
                    renderTabs(state)

                    if (state.isOffline) {
                        Snackbar.make(
                            catalogRecyclerView,
                            "Нет сети. Показан сохраненный каталог.",
                            Snackbar.LENGTH_LONG,
                        ).setAction("Повторить") {
                            viewModel.loadData()
                        }.show()
                    }
                }
            }
        }

        refreshCart()
    }

    private fun renderTabs(state: CatalogState.Content) {
        if (tabLayout.tabCount != 0) {
            return
        }

        state.categories.forEach { category ->
            val tab = tabLayout.newTab().setText(category.name)
            tab.tag = category.id
            tabLayout.addTab(tab)
        }

        val savedId = viewModel.getSelectedCategoryId()
        for (i in 0 until tabLayout.tabCount) {
            if (tabLayout.getTabAt(i)?.tag == savedId) {
                tabLayout.getTabAt(i)?.select()
                break
            }
        }

        val tabStrip = tabLayout.getChildAt(0) as ViewGroup
        for (i in 0 until tabStrip.childCount) {
            val tabView = tabStrip.getChildAt(i)
            val params = tabView.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(10, 0, 10, 0)
            tabView.requestLayout()
        }
    }

    private fun showProductDetails(product: Product) {
        val sheet =
            ProductDetailsSheet(product) { selectedProduct, selectedSize ->
                cartRepository.addToCart(selectedProduct.id, selectedSize.id) {
                    refreshCart()
                }
            }
        sheet.show(supportFragmentManager, "ProductDetails")
    }

    private fun showCatalogScreen() {
        cartContainer.visibility = View.GONE
        tabLayout.visibility = View.VISIBLE
        catalogRecyclerView.visibility = View.VISIBLE
    }

    private fun showCartScreen() {
        tabLayout.visibility = View.GONE
        catalogRecyclerView.visibility = View.GONE
        cartContainer.visibility = View.VISIBLE
        refreshCart()
    }

    private fun refreshCart() {
        cartRepository.loadCart { items, quantity ->
            cartAdapter.submitList(items)
            hasCartItems = items.isNotEmpty()
            cartEmptyText.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            cartRecyclerView.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
            orderForm.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
            cartTotalText.text = items.sumOf { it.totalPriceInKopecks }.toRubles()
            updateCheckoutState()
            updateCartBadge(quantity)
        }
    }

    private fun updateCartBadge(quantity: Int) {
        val badge = bottomNav.getOrCreateBadge(R.id.nav_cart)
        badge.isVisible = quantity > 0
        badge.number = quantity
    }

    private fun updateCheckoutState() {
        val hasName = customerNameInput.text.toString().trim().isNotEmpty()
        val hasEmail = PatternsCompat.EMAIL_ADDRESS.matcher(customerEmailInput.text.toString().trim()).matches()
        checkoutButton.isEnabled = hasName && hasEmail && hasCartItems
    }
}
