package com.coffeehub.ui.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.coffeehub.data.util.DatabaseImporter
import com.coffeehub.databinding.FragmentAdminDashboardBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!
    
    @Inject
    lateinit var databaseImporter: DatabaseImporter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkAndImportData()
    }
    
    /**
     * Auto-import sample data if database is empty (one-time setup)
     * Uncomment the importData() call to run the import
     */
    private fun checkAndImportData() {
        lifecycleScope.launch {
            try {
                val hasData = databaseImporter.hasExistingProducts()
                if (!hasData) {
                    Log.d("AdminDashboard", "Database is empty. Auto-importing sample data...")
                    
                    // UNCOMMENT THIS LINE TO AUTO-IMPORT DATA:
//                     importData()
                    
                    Toast.makeText(
                        requireContext(),
                        "Database is empty. Uncomment importData() to load sample products.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Log.d("AdminDashboard", "Database already has products.")
                }
            } catch (e: Exception) {
                Log.e("AdminDashboard", "Error checking database", e)
            }
        }
    }
    
    /**
     * Manual import function - call this to import sample data
     * Can be triggered from code or button click
     */
    private fun importData() {
        lifecycleScope.launch {
            try {
                Toast.makeText(requireContext(), "Importing products...", Toast.LENGTH_SHORT).show()
                
                val (success, failure) = databaseImporter.importProducts()
                
                val message = "Import completed!\nSuccess: $success\nFailed: $failure"
                Log.d("AdminDashboard", message)
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                
            } catch (e: Exception) {
                val errorMsg = "Import failed: ${e.message}"
                Log.e("AdminDashboard", errorMsg, e)
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
