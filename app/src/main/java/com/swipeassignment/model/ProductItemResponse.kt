package com.swipeassignment.model

data class ProductItemResponse(
    val message: String,
    val product_details: ProductDetail,
    val product_id: Int,
    val success: Boolean
)