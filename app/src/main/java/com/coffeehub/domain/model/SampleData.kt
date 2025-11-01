package com.coffeehub.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Data classes for parsing database.json sample data
 */
data class SampleDatabase(
    @SerializedName("Banner")
    val banner: List<BannerItem>? = null,
    
    @SerializedName("Category")
    val category: List<CategoryItem>? = null,
    
    @SerializedName("Popular")
    val popular: List<ProductItem>? = null,
    
    @SerializedName("Special")
    val special: List<ProductItem>? = null,
    
    @SerializedName("Items")
    val items: List<ProductItem>? = null
)

data class BannerItem(
    @SerializedName("url")
    val url: String? = null
)

data class CategoryItem(
    @SerializedName("id")
    val id: Int? = null,
    
    @SerializedName("title")
    val title: String? = null
)

data class ProductItem(
    @SerializedName("categoryId")
    val categoryId: String? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("extra")
    val extra: String? = null,
    
    @SerializedName("picUrl")
    val picUrl: List<String>? = null,
    
    @SerializedName("price")
    val price: Double? = null,
    
    @SerializedName("rating")
    val rating: Double? = null,
    
    @SerializedName("title")
    val title: String? = null
)
