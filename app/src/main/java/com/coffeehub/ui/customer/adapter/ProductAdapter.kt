package com.coffeehub.ui.customer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.coffeehub.R
import com.coffeehub.databinding.ItemProductCardBinding
import com.coffeehub.domain.model.Product
import java.text.NumberFormat
import java.util.Locale

/**
 * RecyclerView adapter for displaying product cards in grid layout
 */
class ProductAdapter(
    private val onProductClick: (Product) -> Unit,
    private val onAddClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding, onProductClick, onAddClick)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ProductViewHolder(
        private val binding: ItemProductCardBinding,
        private val onProductClick: (Product) -> Unit,
        private val onAddClick: (Product) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.apply {
                // Set product name
                tvName.text = product.name

                // Format price in VND
                val priceFormatted = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                    .format(product.price)
                tvPrice.text = priceFormatted

                // Load product image with Glide
                Glide.with(ivProduct.context)
                    .load(product.imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .into(ivProduct)

                // Handle click events
                root.setOnClickListener {
                    onProductClick(product)
                }

                btnAdd.setOnClickListener {
                    onAddClick(product)
                }
            }
        }
    }

    /**
     * DiffUtil callback for efficient list updates
     */
    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}
