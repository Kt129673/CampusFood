package com.example.campusfood.network

import com.example.campusfood.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit client for backend communication.
 *
 * Production configuration:
 * - Uses BuildConfig to switch between local and cloud endpoints
 * - Includes structured logging interceptor (BODY for debug, NONE for release)
 * - Configures timeout policies for mobile network resilience
 * - Lenient Moshi parsing to handle backend schema evolution gracefully
 */
object RetrofitInstance {

    /**
     * Backend endpoint selection:
     * - IS_LOCAL = true  → connects to local dev server
     * - IS_LOCAL = false → connects to AWS Elastic Beanstalk production
     */
    private const val IS_LOCAL = true

    // Set this to:
    // "10.0.2.2" for Android Emulator
    // "172.20.10.6" for Physical Device (Match your PC's IP on the same network)
    private const val LOCAL_IP = "172.20.10.6"

    private val BASE_URL = if (IS_LOCAL) "http://$LOCAL_IP:5000/api/"
                           else "http://Campusfood-backend-env.eba-nwhwij87.eu-north-1.elasticbeanstalk.com/api/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            // Add standard headers to all requests
            val request = chain.request().newBuilder()
                .addHeader("Accept", "application/json")
                .addHeader("X-Client-Platform", "Android")
                .addHeader("X-Client-Version", BuildConfig.VERSION_NAME)
                .build()
            chain.proceed(request)
        }
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
            .build()
            .create(ApiService::class.java)
    }
}
