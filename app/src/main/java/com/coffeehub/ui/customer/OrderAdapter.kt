package com.coffeehub.ui.customer

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coffeehub.databinding.ItemOrderBinding
import com.coffeehub.domain.model.Order
import com.coffeehub.domain.model.OrderStatus
import java.text.SimpleDateFormat
import java.util.*

class OrderAdapter(
    private val onViewDetails: (Order) -> Unit
) : ListAdapter<Order, OrderAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderViewHolder(
        private val binding: ItemOrderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            binding.apply {
                // Order ID (show last 6 characters)
                tvOrderId.text = "Order #${order.id.takeLast(6)}"

                // Status chip
                setupStatusChip(order.status)

                // Date & Time
                tvOrderDate.text = formatDate(order.timestamp)

                // Items summary
                val itemCount = order.items.sumOf { it.quantity }
                val itemsPreview = order.items.take(2).joinToString(", ") { 
                    "${it.productName} x${it.quantity}" 
                }
                tvItemsSummary.text = if (order.items.size > 2) {
                    "$itemCount items • $itemsPreview..."
                } else {
                    "$itemCount items • $itemsPreview"
                }

                // Payment method
                tvPaymentMethod.text = order.paymentMethod

                // Total
                tvOrderTotal.text = formatPrice(order.total)

                // View details button
                btnViewDetails.setOnClickListener {
                    onViewDetails(order)
                }
            }
        }

        private fun setupStatusChip(status: OrderStatus) {
            binding.chipStatus.apply {
                text = status.name
                
                // Set color based on status
                val (bgColor, textColor) = when (status) {
                    OrderStatus.PENDING -> Pair("#FF9800", "#FFFFFF") // Orange
                    OrderStatus.PREPARING -> Pair("#2196F3", "#FFFFFF") // Blue
                    OrderStatus.READY -> Pair("#4CAF50", "#FFFFFF") // Green
                    OrderStatus.COMPLETED -> Pair("#757575", "#FFFFFF") // Gray
                    OrderStatus.CANCELLED -> Pair("#F44336", "#FFFFFF") // Red
                }
                
                chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                    Color.parseColor(bgColor)
                )
                setTextColor(Color.parseColor(textColor))
            }
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

        private fun formatPrice(price: Double): String {
            return String.format("%,.0fđ", price)
        }
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }
}
