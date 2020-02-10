package com.example.wemovebookings.api

class CustomNetworkException(message: String?, throwable: Throwable?) :
    Exception(message, throwable) {


    constructor(message: String?) : this(message, null)

}