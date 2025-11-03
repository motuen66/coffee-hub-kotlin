package com.coffeehub.ui.admin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.coffeehub.R
import com.coffeehub.databinding.DialogAddEditProductBinding
import com.coffeehub.databinding.FragmentManageProductsBinding
import com.coffeehub.domain.model.Product
import com.coffeehub.viewmodel.AdminProductUiState
import com.coffeehub.viewmodel.AdminProductViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class ManageProductsFragment : Fragment() {

    private var _binding: FragmentManageProductsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminProductViewModel by viewModels()

    private lateinit var productAdapter: AdminProductAdapter

    private var selectedImageUri: Uri? = null
    private var currentEditingProduct: Product? = null

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            currentDialogBinding?.let { dialogBinding ->
                Glide.with(this)
                    .load(selectedImageUri)
                    .into(dialogBinding.ivPreview)
            }
        }
    }

    private var currentDialogBinding: DialogAddEditProductBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        productAdapter = AdminProductAdapter(
            onEdit = { product -> showAddEditDialog(product) },
            onDelete = { product -> showDeleteConfirmation(product) }
        )

        binding.rvAdminProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productAdapter
        }
    }

    private fun setupListeners() {
        // Add product buttons
        binding.btnAddProduct.setOnClickListener {
            showAddEditDialog(null)
        }

        binding.fabAddProduct.setOnClickListener {
            showAddEditDialog(null)
        }

        // Search button
        binding.btnSearch.setOnClickListener {
            toggleSearchView()
        }

        // Search view
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.searchProducts(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { viewModel.searchProducts(it) }
                return true
            }
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is AdminProductUiState.Loading -> {
                        binding.progressIndicator.visibility = View.VISIBLE
                        binding.rvAdminProducts.visibility = View.GONE
                        binding.layoutEmptyState.visibility = View.GONE
                    }
                    is AdminProductUiState.Success -> {
                        binding.progressIndicator.visibility = View.GONE
                        
                        if (state.products.isEmpty()) {
                            binding.rvAdminProducts.visibility = View.GONE
                            binding.layoutEmptyState.visibility = View.VISIBLE
                        } else {
                            binding.rvAdminProducts.visibility = View.VISIBLE
                            binding.layoutEmptyState.visibility = View.GONE
                            productAdapter.submitList(state.products)
                        }
                    }
                    is AdminProductUiState.Error -> {
                        binding.progressIndicator.visibility = View.GONE
                        binding.rvAdminProducts.visibility = View.GONE
                        binding.layoutEmptyState.visibility = View.VISIBLE
                        
                        Toast.makeText(
                            requireContext(),
                            "Error: ${state.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun toggleSearchView() {
        if (binding.searchView.visibility == View.VISIBLE) {
            binding.searchView.visibility = View.GONE
            binding.searchView.setQuery("", false)
            viewModel.loadProducts()
        } else {
            binding.searchView.visibility = View.VISIBLE
            binding.searchView.requestFocus()
        }
    }

    private fun showAddEditDialog(product: Product?) {
        val dialogBinding = DialogAddEditProductBinding.inflate(layoutInflater)
        currentDialogBinding = dialogBinding
        currentEditingProduct = product
        selectedImageUri = null

        // Set dialog title
        dialogBinding.tvDialogTitle.text = if (product == null) "Add Product" else "Edit Product"

        // Pre-fill data if editing
        product?.let {
            dialogBinding.etName.setText(it.name)
            dialogBinding.etPrice.setText(it.price.toString())
            dialogBinding.etCategory.setText(it.category)
            dialogBinding.etStock.setText(it.stock.toString())
            dialogBinding.etDescription.setText(it.description)

            // Load existing image - handle both local file and URL
            if (it.imageUrl.isNotEmpty()) {
                val imageSource = when {
                    it.imageUrl.startsWith("/") || it.imageUrl.startsWith("file://") -> {
                        // Local file path
                        File(it.imageUrl)
                    }
                    it.imageUrl.startsWith("http") -> {
                        // URL from Firebase Storage or external
                        it.imageUrl
                    }
                    else -> null
                }
                
                if (imageSource != null) {
                    Glide.with(this)
                        .load(imageSource)
                        .into(dialogBinding.ivPreview)
                }
            }
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        // Choose image button
        dialogBinding.btnChooseImage.setOnClickListener {
            openImagePicker()
        }

        // Cancel button
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Save button
        dialogBinding.btnSaveProduct.setOnClickListener {
            saveProduct(dialogBinding, product, dialog)
        }

        dialog.show()
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        imagePickerLauncher.launch(intent)
    }

    private fun saveProduct(
        dialogBinding: DialogAddEditProductBinding,
        product: Product?,
        dialog: androidx.appcompat.app.AlertDialog
    ) {
        val name = dialogBinding.etName.text.toString().trim()
        val priceStr = dialogBinding.etPrice.text.toString().trim()
        val category = dialogBinding.etCategory.text.toString().trim()
        val stockStr = dialogBinding.etStock.text.toString().trim()
        val description = dialogBinding.etDescription.text.toString().trim()

        // Validation
        when {
            name.isEmpty() -> {
                dialogBinding.etName.error = "Product name is required"
                return
            }
            priceStr.isEmpty() -> {
                dialogBinding.etPrice.error = "Price is required"
                return
            }
            stockStr.isEmpty() -> {
                dialogBinding.etStock.error = "Stock is required"
                return
            }
        }

        val price = priceStr.toDoubleOrNull() ?: 0.0
        val stock = stockStr.toIntOrNull() ?: 0

        // Show loading
        dialogBinding.progressIndicator.visibility = View.VISIBLE
        dialogBinding.btnSaveProduct.isEnabled = false

        if (product == null) {
            // Add new product
            viewModel.addProduct(
                name = name,
                price = price,
                category = category,
                description = description,
                stock = stock,
                imageUri = selectedImageUri
            ) { success, message ->
                dialogBinding.progressIndicator.visibility = View.GONE
                dialogBinding.btnSaveProduct.isEnabled = true

                if (success) {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    currentDialogBinding = null
                } else {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                }
            }
        } else {
            // Update existing product
            viewModel.updateProduct(
                productId = product.id,
                name = name,
                price = price,
                category = category,
                description = description,
                stock = stock,
                imageUri = selectedImageUri,
                currentImageUrl = product.imageUrl
            ) { success, message ->
                dialogBinding.progressIndicator.visibility = View.GONE
                dialogBinding.btnSaveProduct.isEnabled = true

                if (success) {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    currentDialogBinding = null
                } else {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showDeleteConfirmation(product: Product) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete \"${product.name}\"? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteProduct(product)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteProduct(product: Product) {
        viewModel.deleteProduct(product) { success, message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentDialogBinding = null
        _binding = null
    }
}
