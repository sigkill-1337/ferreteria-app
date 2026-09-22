package com.example.ferreteria.data

import com.example.ferreteria.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit + Moshi + OkHttp, una sola instancia para toda la app.
 *
 * Dos detalles que importan:
 *
 * 1. Moshi necesita [KotlinJsonAdapterFactory] explícitamente. Con
 *    `MoshiConverterFactory.create()` a secas, un data class de Kotlin sin
 *    codegen truena en tiempo de ejecución al parsear.
 * 2. El log redacta X-API-Key. Sin `redactHeader` la key acabaría impresa en
 *    Logcat en cada request.
 */
object ApiClient {

    /** true cuando falta FERRETERIA_API_KEY en local.properties. */
    val faltaApiKey: Boolean = BuildConfig.FERRETERIA_API_KEY.isBlank()

    val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val interceptorApiKey = Interceptor { chain ->
        val peticion = chain.request().newBuilder()
            .addHeader("X-API-Key", BuildConfig.FERRETERIA_API_KEY)
            .addHeader("Accept", "application/json")
            .build()
        chain.proceed(peticion)
    }

    private val interceptorLog = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
        redactHeader("X-API-Key")
    }

    private val cliente: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(interceptorApiKey)
        .addInterceptor(interceptorLog)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    val api: FerreteriaApi = Retrofit.Builder()
        .baseUrl(BuildConfig.FERRETERIA_BASE_URL)
        .client(cliente)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(FerreteriaApi::class.java)
}
