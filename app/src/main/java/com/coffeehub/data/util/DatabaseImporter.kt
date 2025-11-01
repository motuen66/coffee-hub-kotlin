package com.coffeehub.data.util

import android.content.Context
import com.coffeehub.domain.model.Product
import com.coffeehub.domain.model.SampleDatabase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class to import sample data from assets/database.json to Firestore
 */
@Singleton
class DatabaseImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val gson: Gson
) {
    
    /**
     * Import all products from database.json to Firestore products collection
     * @return Pair of (successCount, failureCount)
     */
    suspend fun importProducts(): Pair<Int, Int> {
        var successCount = 0
        var failureCount = 0
        
        try {
            // Read and parse JSON file
            val sampleData = readSampleDatabase() ?: return Pair(0, 1)
            
            // Combine all product items (Popular + Special + Items)
            val allProducts = mutableListOf<Pair<String, com.coffeehub.domain.model.ProductItem>>()
            
            // Add popular products with "Popular" category
            sampleData.popular?.forEach { item ->
                allProducts.add("Popular" to item)
            }
            
            // Add special products with "Special" category
            sampleData.special?.forEach { item ->
                allProducts.add("Special" to item)
            }
            
            // Add items with their categoryId mapped to category name
            val categoryMap = sampleData.category?.associate { it.id.toString() to (it.title ?: "Other") } ?: emptyMap()
            sampleData.items?.forEach { item ->
                val categoryName = categoryMap[item.categoryId] ?: "Other"
                allProducts.add(categoryName to item)
            }
            
            // Import each product to Firestore
            allProducts.forEach { (category, item) ->
                try {
                    val product = convertToProduct(item, category)
                    firestore.collection("products")
                        .add(product)
                        .await()
                    successCount++
                } catch (e: Exception) {
                    e.printStackTrace()
                    failureCount++
                }
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            failureCount++
        }
        
        return Pair(successCount, failureCount)
    }
    
    /**
     * Check if products collection already has data
     */
    suspend fun hasExistingProducts(): Boolean {
        return try {
            val snapshot = firestore.collection("products")
                .limit(1)
                .get()
                .await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Clear all products from Firestore (use with caution!)
     */
    suspend fun clearAllProducts(): Int {
        var deletedCount = 0
        try {
            val snapshot = firestore.collection("products")
                .get()
                .await()
            
            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
                deletedCount++
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return deletedCount
    }
    
    /**
     * Read and parse database.json from assets folder
     */
    private fun readSampleDatabase(): SampleDatabase? {
        return try {
            val inputStream = context.assets.open("database.json")
            val reader = InputStreamReader(inputStream)
            gson.fromJson(reader, SampleDatabase::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Convert ProductItem from JSON to Product domain model
     */
    private fun convertToProduct(item: com.coffeehub.domain.model.ProductItem, category: String): Product {
        // Convert USD price to VND (approximate: 1 USD = 24,000 VND)
        val priceInVnd = (item.price ?: 0.0) * 24000
        
        return Product(
            id = "", // Firestore will auto-generate
            name = item.title ?: "Unknown Product",
            description = item.description ?: "",
            price = priceInVnd,
            imageUrl = item.picUrl?.firstOrNull() ?: "",
            category = category,
            stock = 100, // Default stock
            isAvailable = true,
            createdAt = Timestamp.now(),
            extra = item.extra,
            rating = item.rating ?: 0.0
        )
    }
}
