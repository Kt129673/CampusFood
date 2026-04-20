package com.example.campusfood.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    /**
     * Base URL for the Spring Boot backend.
     *
     * Emulator → Local PC:   "http://10.0.2.2:8080/api/"
     * Physical Device (WiFi): "http://<YOUR_PC_IP>:8080/api/"
     * Production:             "https://api.campusfood.com/api/"
     */
    private const val BASE_URL = "http://Campusfood-backend-env.eba-nwhwij87.eu-north-1.elasticbeanstalk.com/api/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

