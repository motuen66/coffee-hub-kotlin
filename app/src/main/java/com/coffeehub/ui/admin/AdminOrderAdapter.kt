package com.coffeehub.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coffeehub.R
import com.coffeehub.databinding.ItemAdminOrderBinding
import com.coffeehub.domain.model.Order
import com.coffeehub.domain.model.OrderStatus
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminOrderAdapter(
    private val onNextStatus: (Order) -> Unit,
    private val onViewDetails: (Order) -> Unit
) : ListAdapter<Order, AdminOrderAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemAdminOrderBinding.inflate(
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
        private val binding: ItemAdminOrderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            binding.apply {
                // Customer info
                tvCustomerName.text = order.customerName
                tvOrderId.text = "Order #${order.id.take(8).uppercase()}"
                
                // Format timestamp
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                tvOrderTime.text = dateFormat.format(Date(order.timestamp))

                // Build items list
                layoutOrderItems.removeAllViews()
                order.items.forEach { item ->
                    val itemView = LayoutInflater.from(itemView.context)
                        .inflate(R.layout.item_order_product_simple, layoutOrderItems, false)
                    
                    itemView.findViewById<TextView>(R.id.tvItemName).text = 
                        "☕ ${item.productName} - ${item.size}"
                    itemView.findViewById<TextView>(R.id.tvItemQuantity).text = 
                        "× ${item.quantity}"
                    itemView.findViewById<TextView>(R.id.tvItemPrice).text = 
                        String.format("%,.0fđ", item.price * item.quantity)
                    
                    layoutOrderItems.addView(itemView)
                }

                // Total price
                tvTotalPrice.text = String.format("%,.0fđ", order.total)

                // Payment method
                tvPaymentMethod.text = order.paymentMethod

                // Notes
                if (!order.notes.isNullOrBlank()) {
                    tvNotes.visibility = View.VISIBLE
                    tvNotes.text = "Note: ${order.notes}"
                } else {
                    tvNotes.visibility = View.GONE
                }

                // Status chip
                setupStatusChip(chipStatus, order.status)

                // Next status button
                setupNextStatusButton(order)

                // Details button
                btnViewDetails.setOnClickListener {
                    onViewDetails(order)
                }
            }
        }

        private fun setupStatusChip(chip: Chip, status: OrderStatus) {
            chip.text = status.name
            
            val (bgColor, textColor) = when (status) {
                OrderStatus.PENDING -> R.color.orange to android.R.color.white
                OrderStatus.PREPARING -> R.color.blue to android.R.color.white
                OrderStatus.READY -> R.color.green to android.R.color.white
                OrderStatus.COMPLETED -> R.color.gray to android.R.color.white
                OrderStatus.CANCELLED -> R.color.red to android.R.color.white
            }
            
            chip.chipBackgroundColor = ContextCompat.getColorStateList(chip.context, bgColor)
            chip.setTextColor(ContextCompat.getColor(chip.context, textColor))
        }

        private fun setupNextStatusButton(order: Order) {
            binding.btnNextStatus.apply {
                when (order.status) {
                    OrderStatus.PENDING -> {
                        isEnabled = true
                        text = "Complete Order"
                        alpha = 1.0f
                        setOnClickListener { onNextStatus(order) }
                    }
                    OrderStatus.PREPARING,
                    OrderStatus.READY,
                    OrderStatus.COMPLETED -> {
                        isEnabled = false
                        text = "Completed"
                        alpha = 0.5f
                        setOnClickListener(null)
                    }
                    OrderStatus.CANCELLED -> {
                        isEnabled = false
                        text = "Cancelled"
                        alpha = 0.5f
                        setOnClickListener(null)
                    }
                }
            }
        }
    }

    private class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }
}
