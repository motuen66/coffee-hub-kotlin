package com.coffeehub.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coffeehub.R
import com.coffeehub.databinding.ItemAdminProductBinding
import com.coffeehub.domain.model.Product
import java.io.File

class AdminProductAdapter(
    private val onEdit: (Product) -> Unit,
    private val onDelete: (Product) -> Unit
) : ListAdapter<Product, AdminProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemAdminProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(
        private val binding: ItemAdminProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.apply {
                // Product name
                tvProductName.text = product.name

                // Price
                tvProductPrice.text = formatPrice(product.price)

                // Category
                tvProductCategory.text = product.category ?: "No category"

                // Stock
                tvProductStock.text = "Stock: ${product.stock}"

                // Load image - handle both local file path and URL
                val imageSource = when {
                    product.imageUrl.isEmpty() -> R.drawable.ic_coffee_placeholder
                    product.imageUrl.startsWith("/") || product.imageUrl.startsWith("file://") -> {
                        // Local file path
                        File(product.imageUrl)
                    }
                    product.imageUrl.startsWith("http") -> {
                        // URL (old products from Firebase Storage or external URLs)
                        product.imageUrl
                    }
                    else -> R.drawable.ic_coffee_placeholder
                }
                
                Glide.with(ivProduct.context)
                    .load(imageSource)
                    .placeholder(R.drawable.ic_coffee_placeholder)
                    .error(R.drawable.ic_coffee_placeholder)
                    .into(ivProduct)

                // Edit button
                btnEdit.setOnClickListener {
                    onEdit(product)
                }

                // Delete button
                btnDelete.setOnClickListener {
                    onDelete(product)
                }
            }
        }

        private fun formatPrice(price: Double): String {
            return String.format("%,.0fđ", price)
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}
