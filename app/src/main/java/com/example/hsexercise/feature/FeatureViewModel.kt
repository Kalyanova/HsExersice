package com.example.hsexercise.feature

import android.app.Application
import android.util.Log
import androidx.annotation.UiThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.hsexercise.common.NetworkProvider
import com.example.hsexercise.feature.database.FeatureModel
import com.example.hsexercise.feature.database.FeatureRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FeatureViewModel(application: Application) : AndroidViewModel(application) {

    // LiveData for "Loading" State (progress bar should be shown while api call is taking place)
    private val _loading = MutableLiveData<Boolean>()

    private val _response = MutableLiveData<List<FeatureModel>>()
    private val _error = MutableLiveData<Nothing>()
    private val _emptyList = MutableLiveData<Nothing>()
    private val retrofitService = NetworkProvider.retrofitService
    private val featureDao = FeatureRoomDatabase.getDatabase(application).featureTableDao()

    private var getPicturesJobFromDB = Job()
    private var getPicturesJobFromNetwork = Job()

    private val networkCoroutineScope = CoroutineScope(getPicturesJobFromNetwork + Dispatchers.Main)
    private val dbCoroutineScope = CoroutineScope(getPicturesJobFromDB + Dispatchers.IO)

    val loading: LiveData<Boolean> get() = _loading
    val response: LiveData<List<FeatureModel>> get() = _response
    val emptyList: LiveData<Nothing> get() = _emptyList
    val error: LiveData<Nothing> get() = _error

    fun loadPictures() {
        Log.d(TAG, "loadPictures")
        _loading.postValue(true)

        // We're still executing code on the main thread (because Retrofit does all its work on a background thread),
        // but now we're letting coroutines manage concurrency.
        networkCoroutineScope.launch {
            val getPicturesDeferred = retrofitService.getPicturesAsync(PAGE_NUMBER)
            try {
                val response: List<FeatureModel> = getPicturesDeferred.await()
                _loading.postValue(false)
                Log.d(TAG, "Response: $response")

                if (!response.isNullOrEmpty()) {
                    // Content State (there is data to display)
                    _response.postValue(response)
                    // TODO: saveIntoDatabaseIfNecessary(response)
                } else {
                    // Empty State (no data)
                    _emptyList.postValue(null)
                }
            } catch (e: Exception) {
                // Error State (api call failed)
                _error.postValue(null)
                Log.e(TAG, "Failure: ${e.message}")
            }
        }
    }

    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) = FeatureViewModel(application) as T
    }

    private companion object {
        private const val TAG = "FeatureViewModel"
        private const val PAGE_NUMBER = "1"
    }
}
