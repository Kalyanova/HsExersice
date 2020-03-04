package com.example.hsexercise.network

import com.example.hsexercise.feature.database.FeatureModel
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("v2/list")
    fun getPicturesAsync(@Query("page") page: String): Deferred<List<FeatureModel>>
}