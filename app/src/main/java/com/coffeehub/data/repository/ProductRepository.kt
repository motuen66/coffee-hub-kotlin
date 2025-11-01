package com.coffeehub.data.repository

import com.coffeehub.domain.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
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
}
