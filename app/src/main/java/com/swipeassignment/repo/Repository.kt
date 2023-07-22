package com.swipeassignment.repo

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.swipeassignment.model.ProductDetails
import com.swipeassignment.model.ProductItemResponse
import com.swipeassignment.network.ApiServices
import com.swipeassignment.utilities.ApiResponce
import com.swipeassignment.utilities.Constants
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

class Repository(private val apiServices: ApiServices, private val context: Context) {
    private val mutableLiveData : MutableLiveData<ApiResponce<ProductDetails>> = MutableLiveData()
    val liveData: LiveData<ApiResponce<ProductDetails>> = mutableLiveData

    suspend fun getLiveData(){
        if (Constants.isNetworkAvailable(context)){
            try {
                mutableLiveData.postValue(ApiResponce.Loading())
                val data = apiServices.getProductList().body()
                if (data != null){
                    mutableLiveData.postValue(ApiResponce.Successful(data))
                }else{
                    mutableLiveData.postValue(ApiResponce.Error("Some error occurs"))
                }
            }catch (e : Exception){
                mutableLiveData.postValue(ApiResponce.Error("Somethings went to wrong"))
            }
        }else{
            mutableLiveData.postValue(ApiResponce.Error("No internet connection"))
        }
    }


    suspend fun addProductData(productName : String, productType: String, price : Double, tax : Double, image: File?) : Response<ProductItemResponse>{
        return apiServices.addProductDetails(name = productName.toRequestBody(),
            type = productType.toRequestBody(),
            price = price.toString().toRequestBody(),
            tax = tax.toString().toRequestBody(),
            image = if (image != null){
                MultipartBody.Part.createFormData(
                    "files[]",
                    image.name,
                    RequestBody.create("image/*".toMediaTypeOrNull(), image)
                )
            } else { null })
    }
}