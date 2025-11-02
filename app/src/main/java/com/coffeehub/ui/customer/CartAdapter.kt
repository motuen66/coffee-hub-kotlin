package com.coffeehub.ui.customer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coffeehub.R
import com.coffeehub.databinding.ItemCartProductBinding
import com.coffeehub.domain.model.CartItem

class CartAdapter(
    private val onQuantityChange: (String, Int) -> Unit,
    private val onRemove: (String) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(CartDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartViewHolder(
        private val binding: ItemCartProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            binding.apply {
                // Load product image
                Glide.with(ivCartProduct.context)
                    .load(item.productImage)
                    .placeholder(R.drawable.ic_coffee_placeholder)
                    .error(R.drawable.ic_coffee_placeholder)
                    .into(ivCartProduct)

                // Set product info
                tvCartName.text = item.productName
                tvCartSize.text = item.size
                tvCartPrice.text = formatPrice(item.totalPrice) // Use totalPrice instead of price
                tvQuantity.text = item.quantity.toString()

                // Quantity controls
                btnDecrease.setOnClickListener {
                    val newQty = item.quantity - 1
                    if (newQty >= 1) {
                        onQuantityChange(item.productId, newQty)
                    }
                }

                btnIncrease.setOnClickListener {
                    val newQty = item.quantity + 1
                    onQuantityChange(item.productId, newQty)
                }

                // Remove button
                btnRemove.setOnClickListener {
                    onRemove(item.productId)
                }
            }
        }

        private fun formatPrice(price: Double): String {
            return String.format("%,.0fđ", price)
        }
    }

    class CartDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.productId == newItem.productId && oldItem.size == newItem.size
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }
    }
}
