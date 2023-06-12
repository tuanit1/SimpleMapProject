package com.tuandev.simplemapproject.widget.placeInfoDialog

import androidx.lifecycle.MutableLiveData
import com.tuandev.simplemapproject.base.BaseViewModel
import com.tuandev.simplemapproject.base.ViewState
import com.tuandev.simplemapproject.data.models.ImageData
import com.tuandev.simplemapproject.data.models.Place
import com.tuandev.simplemapproject.data.repositories.local.LocalRepository
import com.tuandev.simplemapproject.data.repositories.remote.FireStoreRepository
import com.tuandev.simplemapproject.extension.mapToImageData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlaceInfoDialogViewModel @Inject constructor(
    private val localRepository: LocalRepository,
    private val fireStoreRepository: FireStoreRepository
) : BaseViewModel<ViewState>() {

    var mPlace: MutableLiveData<Place> = MutableLiveData()
    var mImageList: MutableLiveData<List<ImageData>> = MutableLiveData()

    fun updatePlace(placeId: Int) {
        localRepository.listPlace.find { it.id == placeId }?.let { place ->
            mPlace.value = place
        }
    }

    fun loadPlaceImages() {
        mPlace.value?.run {
            callApiFromFireStore(
                task = fireStoreRepository.getPlaceImageList(id),
                onSuccess = { querySnapshot ->
                    val imageList = querySnapshot.map { it.mapToImageData() }
                    mImageList.value = imageList
                },
                isShowLoading = true
            )
        }
    }
}