package com.swipeassignment.utilities

sealed class ApiResponce<T>(val data : T ?= null, val message : String ?= null) {
    class Loading<T> : ApiResponce<T>()
    class Successful<T>(data: T? = null) : ApiResponce<T>(data = data)
    class Error<T>(errorMessage: String?= null) : ApiResponce<T>(message = errorMessage)
}