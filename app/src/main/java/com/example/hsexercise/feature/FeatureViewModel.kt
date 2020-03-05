package com.example.hsexercise.feature

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.hsexercise.common.NetworkProvider
import com.example.hsexercise.feature.database.FeatureModel
import com.example.hsexercise.common.FeatureRoomDatabase
import com.example.hsexercise.repository.PicturesRepository
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
    // The ViewModel maintains a reference to the repository to get data
    private val repository: PicturesRepository

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

    init {
        val featureDao = FeatureRoomDatabase.getDatabase(application).featureTableDao()
        val retrofitService = NetworkProvider.retrofitService
        repository = PicturesRepository(featureDao, retrofitService)
    }

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
        _loading.postValue(true)
        clearErrorState()
        getPicturesFromDatabase(pageNumber)
    }

    fun clearErrorState() {
        _error.postValue(false)
    }

    private fun loadPicturesFromNetwork(pageNumber: Int = page) {
        Log.d(TAG, "Loading pictures with page number = $pageNumber")

        // We're still executing code on the main thread (because Retrofit does all its work on a background thread),
        // but now we're letting coroutines manage concurrency.
        networkCoroutineScope.launch {
            val getPicturesDeferred = repository.getPicturesFromNetworkAsync(pageNumber)
            try {
                val response: List<FeatureModel> = getPicturesDeferred.await()
                onLoaded(response, isLoadedFromNetwork = true)
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private fun onLoaded(pictures: List<FeatureModel>, isLoadedFromNetwork: Boolean = false) {
        _loading.postValue(false)
        changeButtonsState()

        Log.d(TAG, "Response: $pictures")

        if (!pictures.isNullOrEmpty()) {
            // Content State (there is data to display)
            _response.postValue(pictures)
            if (isLoadedFromNetwork) {
                saveIntoDatabase(pictures, page)
            }
        } else {
            // Empty State (no data)
            _emptyList.postValue(null)
        }
    }

    private fun changeButtonsState() {
        when (pressedButton) {
            Button.PREVIOUS_PAGE -> {
                _nextPageBtnEnabled.postValue(true)
                if (page == START_PAGE_NUMBER) {
                    _previousPageBtnEnabled.postValue(false)
                }
            }
            Button.NEXT_PAGE -> { _previousPageBtnEnabled.postValue(true) }
            Button.NONE -> {}
        }
    }

    private fun saveIntoDatabase(pictures: List<FeatureModel>, pageNumber: Int) {
        Log.d(TAG, "saveIntoDatabase")
        val picturesWithPageNumber: List<FeatureModel> = pictures.map {
            FeatureModel(
                it.id,
                it.author,
                it.width,
                it.height,
                it.url,
                page = pageNumber
            )
        }
        dbCoroutineScope.launch {
            repository.insertAllPictures(picturesWithPageNumber)
        }
    }

    private fun getPicturesFromDatabase(pageNumber: Int) = dbCoroutineScope.launch {
        Log.d(TAG, "getPicturesFromDatabase")
        try {
            val pictures = repository.getPicturesForPageFromDB(pageNumber)
            // Checks to see if that page exists in the database,
            // if it does then display those products otherwise make a call to the endpoint
            if (pictures.isNullOrEmpty()) {
                loadPicturesFromNetwork(page)
            } else {
                onLoaded(pictures, isLoadedFromNetwork = false)
            }
        } catch (e: Exception) {
            handleError(e)
        }
    }

    private fun handleError(e: Exception) {
        // Error State (api call failed)
        _loading.postValue(false)
        _error.postValue(true)
        Log.e(TAG, "Failure: ${e.message}")
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
