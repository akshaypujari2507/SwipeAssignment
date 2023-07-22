package com.swipeassignment.utilities

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object Constants {
    const val BASE_URL = "https://app.getswipe.in/api/public/"
    fun isNetworkAvailable(context : Context) : Boolean{
       val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
       val network = connectivityManager.activeNetwork
       val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
       return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
    }
}