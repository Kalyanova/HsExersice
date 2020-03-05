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
    private val _previousPageBtnEnabled = MutableLiveData<Boolean>()
    private val _nextPageBtnEnabled = MutableLiveData<Boolean>()
    private val _response = MutableLiveData<List<FeatureModel>>()
    private val _error = MutableLiveData<Boolean>()
    private val _emptyList = MutableLiveData<Nothing>()
    private val retrofitService = NetworkProvider.retrofitService

    private val featureDao = FeatureRoomDatabase.getDatabase(application).featureTableDao()

    private var getPicturesJobFromDB = Job()
    private var getPicturesJobFromNetwork = Job()

    private val networkCoroutineScope = CoroutineScope(getPicturesJobFromNetwork + Dispatchers.Main)
    private val dbCoroutineScope = CoroutineScope(getPicturesJobFromDB + Dispatchers.IO)

    private var page = 1
    private var pressedButton: Button = Button.NONE

    val loading: LiveData<Boolean> get() = _loading
    val previousPageBtnEnabled: LiveData<Boolean> get() = _previousPageBtnEnabled
    val nextPageBtnEnabled: LiveData<Boolean> get() = _nextPageBtnEnabled
    val response: LiveData<List<FeatureModel>> get() = _response
    val emptyList: LiveData<Nothing> get() = _emptyList
    val error: LiveData<Boolean> get() = _error

    fun showNextPage() {
        pressedButton = Button.NEXT_PAGE
        page++
        loadPictures()
    }

    fun showPreviousPage() {
        if (page == START_PAGE_NUMBER) return
        pressedButton = Button.PREVIOUS_PAGE
        page--
        loadPictures()
    }

    fun loadPictures(pageNumber: Int = page) {
        Log.d(TAG, "Loading pictures with page number = $pageNumber")
        _loading.postValue(true)

        // We're still executing code on the main thread (because Retrofit does all its work on a background thread),
        // but now we're letting coroutines manage concurrency.
        networkCoroutineScope.launch {
            val getPicturesDeferred = retrofitService.getPicturesAsync(pageNumber)
            try {
                val response: List<FeatureModel> = getPicturesDeferred.await()
                _loading.postValue(false)

                when (pressedButton) {
                    Button.PREVIOUS_PAGE -> {
                        _nextPageBtnEnabled.postValue(true)
                        if (pageNumber == START_PAGE_NUMBER) {
                            _previousPageBtnEnabled.postValue(false)
                        }
                    }
                    Button.NEXT_PAGE -> { _previousPageBtnEnabled.postValue(true) }
                    Button.NONE -> {}
                }

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
                _loading.postValue(false)
                _error.postValue(true)
                Log.e(TAG, "Failure: ${e.message}")
            }
        }
    }

    fun clearErrorState() {
        _error.postValue(false)
    }

    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) = FeatureViewModel(application) as T
    }

    enum class Button {
        NONE, PREVIOUS_PAGE, NEXT_PAGE
    }

    private companion object {
        private const val TAG = "FeatureViewModel"
        private const val START_PAGE_NUMBER = 1
    }
}
