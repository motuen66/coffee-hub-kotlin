package com.coffeehub.util

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageStorageHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val productImagesDir: File
        get() = File(context.filesDir, "product_images").apply {
            if (!exists()) mkdirs()
        }

    /**
     * Save image from URI to internal storage
     * @param imageUri - URI from image picker
     * @param productId - unique identifier for the product
     * @return local file path or empty string if failed
     */
    fun saveProductImage(imageUri: Uri, productId: String): String {
        return try {
            val fileName = "${productId}_${System.currentTimeMillis()}.jpg"
            val destinationFile = File(productImagesDir, fileName)

            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            destinationFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * Delete product image from internal storage
     * @param imagePath - absolute path to the image file
     */
    fun deleteProductImage(imagePath: String): Boolean {
        return try {
            if (imagePath.isNotEmpty() && imagePath.startsWith(context.filesDir.absolutePath)) {
                val file = File(imagePath)
                if (file.exists()) {
                    file.delete()
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Get File object from image path for Glide loading
     */
    fun getImageFile(imagePath: String): File? {
        return try {
            if (imagePath.isNotEmpty()) {
                val file = File(imagePath)
                if (file.exists()) file else null
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Clear all product images (useful for testing/debugging)
     */
    fun clearAllProductImages(): Boolean {
        return try {
            productImagesDir.deleteRecursively()
            productImagesDir.mkdirs()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
