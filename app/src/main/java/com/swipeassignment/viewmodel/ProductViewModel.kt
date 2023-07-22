package com.swipeassignment.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swipeassignment.model.ProductItemResponse
import com.swipeassignment.model.ProductDetails
import com.swipeassignment.repo.Repository
import com.swipeassignment.utilities.ApiResponce
import kotlinx.coroutines.launch
import java.io.File

class ProductViewModel(private val repository: Repository) : ViewModel() {

    val liveData: LiveData<ApiResponce<ProductDetails>> = repository.liveData
    private var selectedImage: File? = null
    fun getLiveData(){
        viewModelScope.launch {
            repository.getLiveData()
        }
    }

    // for adding product item
    private val addProductData : MutableLiveData<ApiResponce<ProductItemResponse>> = MutableLiveData()
    val productLiveData : LiveData<ApiResponce<ProductItemResponse>> = addProductData
    fun setImageFile(file: File?){
        selectedImage = file
    }

    fun addProductDetails(productName : String, productType: String, price : Double, tax : Double, image: File? = selectedImage){
        viewModelScope.launch {
            addProductData.postValue(ApiResponce.Loading())
            try {
                val responce = repository.addProductData(productName, productType, price, tax, image).body()
                print("responce $responce")
                if (responce?.success == true){
                    addProductData.postValue(ApiResponce.Successful(responce))
                }else{
                    addProductData.postValue(ApiResponce.Error("Not adding"))
                }
            }catch (e : Exception){
                print("responce Exception $e")
                addProductData.postValue(ApiResponce.Error(e.message))
            }
        }
    }
}