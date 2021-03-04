package com.antoinegrandin.bankaccount

import retrofit2.Call
import retrofit2.http.GET

interface JsonPlaceHolderApi {
    @get:GET("accounts")
    val accounts: Call<List<Account>>
}