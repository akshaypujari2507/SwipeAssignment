package com.swipeassignment.network

import com.swipeassignment.model.ProductDetails
import com.swipeassignment.model.ProductItemResponse
import com.swipeassignment.utilities.Constants
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiServices {

    @GET("get")
    suspend fun getProductList() : Response<ProductDetails>

    @Multipart
    @POST("add")
    suspend fun addProductDetails(
        @Part("product_name") name: RequestBody,
        @Part("product_type") type: RequestBody,
        @Part("price") price: RequestBody,
        @Part("tax") tax: RequestBody,
        @Part image: MultipartBody.Part?,
    ) : Response<ProductItemResponse>
}