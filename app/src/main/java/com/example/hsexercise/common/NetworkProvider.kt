package com.example.hsexercise.common

import com.example.hsexercise.BuildConfig
import com.example.hsexercise.network.ApiService
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

object NetworkProvider {

    val retrofitService : ApiService by lazy {
        provideRestClient().createRetrofitAdapter().create(ApiService::class.java)
    }

    private fun provideRestClient() =
        RestClient(RestClientConfig(
            provideMoshiConverterFactory(),
            provideCoroutineCallAdapterFactory()
        ).apply {
            addInterceptor(provideHttpLoggingInterceptor())
        })

    private fun provideHttpLoggingInterceptor() =
        HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT)
            .apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.BASIC
                }
            }

    private fun provideMoshiConverterFactory(): MoshiConverterFactory = MoshiConverterFactory.create(moshi)

    private fun provideCoroutineCallAdapterFactory(): CoroutineCallAdapterFactory = CoroutineCallAdapterFactory()
}

class RestClient(private val restClientConfig: RestClientConfig) {
    fun createRetrofitAdapter(hostUrl: String = BASE_URL): Retrofit = Retrofit.Builder()
        .addCallAdapterFactory(restClientConfig.callAdapterFactory)
        .addConverterFactory(restClientConfig.converterFactory)
        .client(okHttpClient())
        .baseUrl(hostUrl)
        .build()

    private fun okHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .readTimeout(restClientConfig.readTimeOutValue, TimeUnit.SECONDS)
            .writeTimeout(restClientConfig.writeTimeOutValue, TimeUnit.SECONDS)
            .connectTimeout(restClientConfig.connectTimeOutValue, TimeUnit.SECONDS)

        restClientConfig.interceptors().forEach { builder.addInterceptor(it) }

        if (BuildConfig.DEBUG) builder.hostnameVerifier(HostnameVerifier { _, _ -> true })

        return builder.build()
    }
}

val API_TIME_OUT = if (BuildConfig.DEBUG) 60L else 20L
const val BASE_URL = "https://picsum.photos/"

data class RestClientConfig(
    val converterFactory: Converter.Factory,
    val callAdapterFactory: CallAdapter.Factory,
    val readTimeOutValue: Long = API_TIME_OUT,
    val writeTimeOutValue: Long = API_TIME_OUT,
    val connectTimeOutValue: Long = API_TIME_OUT) {
    private var interceptors: MutableList<Interceptor> = mutableListOf()

    fun addInterceptor(interceptor: Interceptor) = interceptors.add(interceptor)

    fun interceptors() = interceptors.asIterable()
}
