package com.irfan.nanamyuk.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ConfigApi {
    companion object {

        var BASE_URL: String = "http://13.215.156.224:5000/"
        var BASE_ML: String = "http://129.150.57.39/flask/"
        var BASE_OWM: String = "https://api.openweathermap.org/data/2.5/"

        fun getApiService(url: String): ServiceApi {
            val loggingInterceptor =
                HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()
            val retrofit =
                Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()
            return retrofit.create(ServiceApi::class.java)
        }
    }
}