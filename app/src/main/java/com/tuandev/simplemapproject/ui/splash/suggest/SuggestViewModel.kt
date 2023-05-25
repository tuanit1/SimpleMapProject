package com.tuandev.simplemapproject.ui.splash.suggest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tuandev.simplemapproject.base.BaseViewModel
import com.tuandev.simplemapproject.base.ViewState
import com.tuandev.simplemapproject.data.models.UserFeature

sealed class SuggestViewState: ViewState(){
}

class SuggestViewModel: BaseViewModel<ViewState>() {
    var mUserFeature: MutableLiveData<UserFeature> = MutableLiveData()

    fun updateUserFeature(userFeature: UserFeature){
        mUserFeature.value = userFeature
    }





}