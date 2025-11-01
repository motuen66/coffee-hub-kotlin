package com.coffeehub.domain.model

import com.google.firebase.firestore.PropertyName

data class User(
    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",
    
    @get:PropertyName("email")
    @set:PropertyName("email")
    var email: String = "",
    
    @get:PropertyName("name")
    @set:PropertyName("name")
    var name: String = "",
    
    @get:PropertyName("isAdmin")
    @set:PropertyName("isAdmin")
    var isAdmin: Boolean = false,
    
    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    var createdAt: Long = System.currentTimeMillis()
) {
    // No-argument constructor required by Firestore
    constructor() : this("", "", "", false, 0L)
}
