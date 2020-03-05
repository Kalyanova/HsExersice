package com.example.hsexercise.feature

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.example.hsexercise.R
import com.example.hsexercise.common.BaseActivity
import com.example.hsexercise.common.observeNonNull
import com.example.hsexercise.databinding.ActivityFeatureBinding

class FeatureActivity : BaseActivity<FeatureViewModel>() {

    private val connectivityManager: ConnectivityManager
        get() = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

    private val connectivityCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            with(viewModel) {
                val isLoadingStarted = loading.value ?: false
                val isErrorOccurred = error.value ?: false
                if (!isLoadingStarted && isErrorOccurred) {
                    Log.d(TAG, "Network is available. Let's try to reload pictures.")
                    loadPictures()
                    clearErrorState()
                }
            }
        }
    }

    private lateinit var binding: ActivityFeatureBinding

    override val viewModelClass = FeatureViewModel::class.java

    override fun provideViewModelFactory() = FeatureViewModel.Factory(application)

    override fun setLayout() {
        binding = ActivityFeatureBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onViewLoad(savedInstanceState: Bundle?) {
        connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), connectivityCallback)
        val picturesList = binding.picturesList
        val previousPageBtn = binding.previousPageBtn
        val nextPageBtn =  binding.nextPageBtn

        if (savedInstanceState == null) {
            viewModel.loadPictures()
        }

        previousPageBtn.isEnabled = false
        previousPageBtn.setOnClickListener {
            picturesList.isInvisible = true
            viewModel.showPreviousPage()
        }
        nextPageBtn.setOnClickListener {
            picturesList.isInvisible = true
            viewModel.showNextPage()
        }

        with(viewModel) {
            loading.observeNonNull(this@FeatureActivity) {
                binding.progressBar.isVisible = it
                hideErrorStatePlaceholder()
            }
            response.observeNonNull(this@FeatureActivity) { pictures ->
                picturesList.isVisible = true
                picturesList.adapter = PicturesAdapter(pictures)
                hideErrorStatePlaceholder()
                previousPageBtn.isVisible = true
                nextPageBtn.isVisible = true
            }
            previousPageBtnEnabled.observeNonNull(this@FeatureActivity) {
                previousPageBtn.isEnabled = it
            }
            nextPageBtnEnabled.observeNonNull(this@FeatureActivity) {
                nextPageBtn.isEnabled = it
            }
            error.observeNonNull(this@FeatureActivity) {
                if (it) handleErrorState(R.drawable.network_error, R.string.error_state)
                previousPageBtn.isEnabled = if (it) !it else (previousPageBtnEnabled.value ?: false)
                nextPageBtn.isEnabled = if (it) !it else (nextPageBtnEnabled.value ?: true)
            }
            emptyList.observe(this@FeatureActivity, Observer {
                handleErrorState(R.drawable.no_data_found, R.string.empty_state)
                nextPageBtn.isEnabled = false
            })
        }
    }

    override fun onDestroy() {
        connectivityManager.unregisterNetworkCallback(connectivityCallback)
        super.onDestroy()
    }

    private fun hideErrorStatePlaceholder() {
        binding.errorStatePlaceholder.root.isVisible = false
    }

    private fun handleErrorState(@DrawableRes imageId: Int, @StringRes messageId: Int) {
        with(binding.errorStatePlaceholder) {
            root.isVisible = true
            errorStateImage.setImageDrawable(getDrawable(imageId))
            errorStateText.text = getString(messageId)
        }
    }

    private companion object {
        private const val TAG = "FeatureActivity"
    }
}
