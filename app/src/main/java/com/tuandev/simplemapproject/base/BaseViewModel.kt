package com.tuandev.simplemapproject.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class BaseViewModel<VS : ViewState> : ViewModel() {
    val viewState: MutableLiveData<VS> by lazy { MutableLiveData<VS>() }
    protected fun updateViewState(vs: VS) = viewState.postValue(vs)
}