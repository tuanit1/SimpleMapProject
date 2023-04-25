package com.tuandev.simplemapproject.base.map

import com.tuandev.simplemapproject.base.BaseViewModel
import com.tuandev.simplemapproject.base.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class BaseMapViewState: ViewState(){
    object Tuna : BaseMapViewState()
    data class ShiBa(val text: String): BaseMapViewState()
}

@HiltViewModel
class BaseMapViewModel @Inject constructor() : BaseViewModel<BaseMapViewState>() {
    fun showOut() {
        updateViewState(BaseMapViewState.Tuna)
    }

    fun showOut2(text: String){
        updateViewState(BaseMapViewState.ShiBa(text))
    }
}