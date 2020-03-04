package com.example.hsexercise.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

abstract class BaseActivity<VM : ViewModel> : AppCompatActivity() {
    protected abstract val viewModelClass: Class<VM>
    protected lateinit var viewModel: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBeforeViewLoad(savedInstanceState)

        setLayout()
        viewModel = ViewModelProvider(this, provideViewModelFactory()).get(viewModelClass)
        onViewLoad(savedInstanceState)
    }

    abstract fun provideViewModelFactory(): ViewModelProvider.Factory

    abstract fun setLayout()

    open fun onBeforeViewLoad(savedInstanceState: Bundle?) {
        // Intentionally empty so that subclasses can override if necessary
    }

    open fun onViewLoad(savedInstanceState: Bundle?) {
        // Intentionally empty so that subclasses can override if necessary
    }
}
