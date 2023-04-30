package com.tuandev.simplemapproject.base.map

import com.tuandev.simplemapproject.base.BaseViewModel
import com.tuandev.simplemapproject.base.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class BaseMapViewState: ViewState(){

}

@HiltViewModel
class BaseMapViewModel @Inject constructor() : BaseViewModel<BaseMapViewState>() {
    var currentTouchEvent: String = ""
}