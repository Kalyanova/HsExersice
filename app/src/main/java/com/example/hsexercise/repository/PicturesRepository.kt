package com.example.hsexercise.repository

import com.example.hsexercise.feature.database.FeatureModel
import com.example.hsexercise.feature.database.FeatureTableDao
import com.example.hsexercise.network.ApiService
import kotlinx.coroutines.Deferred

/**
 * Gets and save pictures from/to database and fetches them from network.
 * Room executes all queries on a separate thread.
 */
class PicturesRepository (
    private val featureTableDao: FeatureTableDao,
    private val retrofitService: ApiService
) {

    fun getPicturesFromNetworkAsync(pageNumber: Int): Deferred<List<FeatureModel>> {
        return retrofitService.getPicturesAsync(pageNumber)
    }

    fun getPicturesForPageFromDB(pageNumber: Int): List<FeatureModel> {
        return featureTableDao.getAllForPage(pageNumber)
    }

    fun insertAllPictures(pictures: List<FeatureModel>) {
        featureTableDao.insertAll(pictures)
    }
}