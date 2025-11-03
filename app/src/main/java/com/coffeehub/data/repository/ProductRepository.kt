package com.coffeehub.data.repository

import android.net.Uri
import com.coffeehub.domain.model.Product
import com.coffeehub.util.ImageStorageHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val imageStorageHelper: ImageStorageHelper
) {
    private val productsCollection = firestore.collection("products")

    fun getProducts(): Flow<List<Product>> = callbackFlow {
        val listener = productsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val products = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(products)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getProductById(productId: String): Product? {
        return try {
            val doc = productsCollection.document(productId).get().await()
            doc.toObject(Product::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addProduct(product: Product): Result<String> {
        return try {
            val docRef = productsCollection.add(product).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(productId: String, product: Product): Result<Unit> {
        return try {
            productsCollection.document(productId).set(product).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            productsCollection.document(productId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun searchProducts(query: String): Flow<List<Product>> = callbackFlow {
        val listener = productsCollection
            .orderBy("name")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val products = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(products)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Save product image to internal storage
     * @return local file path
     */
    suspend fun saveProductImage(imageUri: Uri, productId: String): Result<String> {
        return try {
            val imagePath = imageStorageHelper.saveProductImage(imageUri, productId)
            if (imagePath.isNotEmpty()) {
                Result.success(imagePath)
            } else {
                Result.failure(Exception("Failed to save image to internal storage"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete product image from internal storage
     */
    suspend fun deleteProductImage(imagePath: String): Result<Unit> {
        return try {
            imageStorageHelper.deleteProductImage(imagePath)
            Result.success(Unit)
        } catch (e: Exception) {
            // Image deletion failure shouldn't block product deletion
            Result.success(Unit)
        }
    }
}
