package com.tuandev.simplemapproject.ui.maptool

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapToolViewModel @Inject constructor() : ViewModel() {
    fun showOut(action: () -> Unit) {
        action.invoke()
    }
}