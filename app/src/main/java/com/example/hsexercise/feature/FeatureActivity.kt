package com.example.hsexercise.feature

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
                val data = response.value
                if (data.isNullOrEmpty()) {
                    loadPictures()
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

        viewModel.loadPictures()

        with(viewModel) {
            loading.observeNonNull(this@FeatureActivity) {
                binding.progressBar.isVisible = it
                hideErrorStatePlaceholder()
            }
            response.observeNonNull(this@FeatureActivity) { pictures ->
                binding.picturesList.adapter = PicturesAdapter(pictures)
                hideErrorStatePlaceholder()
            }
            emptyList.observe(this@FeatureActivity, Observer {
                handleErrorState(R.drawable.no_data_found, R.string.empty_state)
            })
            error.observe(this@FeatureActivity, Observer {
                handleErrorState(R.drawable.network_error, R.string.error_state)
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
}
