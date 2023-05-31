package com.tuandev.simplemapproject.ui.splash.suggest

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tuandev.simplemapproject.base.BaseViewModel
import com.tuandev.simplemapproject.base.ViewState
import com.tuandev.simplemapproject.data.database.daos.SuggestRouteDao
import com.tuandev.simplemapproject.data.database.entities.SaveSuggestRoute
import com.tuandev.simplemapproject.data.models.RouteItem
import com.tuandev.simplemapproject.data.models.UserFeature
import com.tuandev.simplemapproject.data.repositories.local.LocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SuggestViewModel @Inject constructor(
    val suggestRouteDao: SuggestRouteDao,
    private val localRepository: LocalRepository
) : BaseViewModel<ViewState>() {

    var mUserFeature: MutableLiveData<UserFeature> = MutableLiveData()
    var mSuggestList: MutableList<SaveSuggestRoute> = mutableListOf()

    fun updateUserFeature(userFeature: UserFeature) {
        mUserFeature.value = userFeature
    }

    fun updateSuggestList(suggestList: List<RouteItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            suggestRouteDao.run {
                deleteAll()
                insertAll(suggestList.map { saveSuggestRoute ->
                    saveSuggestRoute.run {
                        SaveSuggestRoute(
                            placeId = place.id,
                            isStart = isStart,
                            itemState = itemState,
                            itemIndex = itemIndex
                        )
                    }
                })
            }
        }
    }

    fun getSuggestList() = mSuggestList.mapNotNull { saveSuggestRoute ->
        localRepository.listPlace.find { it.id == saveSuggestRoute.placeId }?.let { place ->
            RouteItem(
                place = place,
                isStart = saveSuggestRoute.isStart,
                itemIndex = saveSuggestRoute.itemIndex,
                itemState = saveSuggestRoute.itemState
            )
        }
    }.sortedBy { it.itemIndex }
}