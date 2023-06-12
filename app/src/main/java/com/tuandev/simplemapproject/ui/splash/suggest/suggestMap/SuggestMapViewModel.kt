package com.tuandev.simplemapproject.ui.splash.suggest.suggestMap

import com.tuandev.simplemapproject.base.BaseViewModel
import com.tuandev.simplemapproject.base.ViewState
import com.tuandev.simplemapproject.data.repositories.local.LocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SuggestMapViewModel @Inject constructor(
    private val localRepository: LocalRepository
) : BaseViewModel<ViewState>() {
    fun getPlaceById(placeId: Int?) = localRepository.listPlace.find { it.id == placeId }
}