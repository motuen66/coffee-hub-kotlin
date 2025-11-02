package com.coffeehub.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coffeehub.databinding.BottomSheetFilterBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.NumberFormat
import java.util.Locale

/**
 * Filter Bottom Sheet for sorting and filtering products
 */
class FilterBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFilterBinding? = null
    private val binding get() = _binding!!
    
    private var onFilterApplied: ((FilterOptions) -> Unit)? = null
    
    data class FilterOptions(
        val sortBy: SortBy = SortBy.NONE,
        val minPrice: Float = 0f,
        val maxPrice: Float = 150000f,
        val availableOnly: Boolean = false
    )
    
    enum class SortBy {
        NONE,
        PRICE_LOW_TO_HIGH,
        PRICE_HIGH_TO_LOW,
        NAME_A_TO_Z,
        NAME_Z_TO_A
    }
    
    companion object {
        fun newInstance(onFilterApplied: (FilterOptions) -> Unit): FilterBottomSheetFragment {
            return FilterBottomSheetFragment().apply {
                this.onFilterApplied = onFilterApplied
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupPriceRangeSlider()
        setupClickListeners()
    }
    
    private fun setupPriceRangeSlider() {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        formatter.maximumFractionDigits = 0
        
        // Update price range text when slider changes
        binding.sliderPriceRange.addOnChangeListener { slider, _, _ ->
            val values = slider.values
            val minPrice = values[0].toInt()
            val maxPrice = values[1].toInt()
            
            binding.tvPriceRange.text = "${formatPrice(minPrice)} - ${formatPrice(maxPrice)}"
        }
        
        // Set initial text
        val initialValues = binding.sliderPriceRange.values
        binding.tvPriceRange.text = "${formatPrice(initialValues[0].toInt())} - ${formatPrice(initialValues[1].toInt())}"
    }
    
    private fun formatPrice(price: Int): String {
        val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
        return "₫${formatter.format(price)}"
    }
    
    private fun setupClickListeners() {
        binding.btnClose.setOnClickListener {
            dismiss()
        }
        
        binding.btnReset.setOnClickListener {
            resetFilters()
        }
        
        binding.btnApply.setOnClickListener {
            applyFilters()
        }
    }
    
    private fun resetFilters() {
        // Reset sort chips
        binding.chipGroupSort.clearCheck()
        
        // Reset price range
        binding.sliderPriceRange.values = listOf(20000f, 100000f)
        
        // Reset availability switch
        binding.switchAvailableOnly.isChecked = false
        
        // Update price range text
        binding.tvPriceRange.text = "₫20,000 - ₫100,000"
    }
    
    private fun applyFilters() {
        val sortBy = when (binding.chipGroupSort.checkedChipId) {
            binding.chipSortPriceLow.id -> SortBy.PRICE_LOW_TO_HIGH
            binding.chipSortPriceHigh.id -> SortBy.PRICE_HIGH_TO_LOW
            binding.chipSortNameAZ.id -> SortBy.NAME_A_TO_Z
            binding.chipSortNameZA.id -> SortBy.NAME_Z_TO_A
            else -> SortBy.NONE
        }
        
        val priceRange = binding.sliderPriceRange.values
        val minPrice = priceRange[0]
        val maxPrice = priceRange[1]
        
        val availableOnly = binding.switchAvailableOnly.isChecked
        
        val filterOptions = FilterOptions(
            sortBy = sortBy,
            minPrice = minPrice,
            maxPrice = maxPrice,
            availableOnly = availableOnly
        )
        
        onFilterApplied?.invoke(filterOptions)
        dismiss()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
