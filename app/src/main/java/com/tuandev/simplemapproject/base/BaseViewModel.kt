package com.tuandev.simplemapproject.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage

abstract class BaseViewModel<VS : ViewState> : ViewModel() {
    val viewState: MutableLiveData<VS> by lazy { MutableLiveData<VS>() }
    var loadingProgress: MutableLiveData<Boolean> = MutableLiveData()
    var showErrorPopup: (errorMessage: String) -> Unit = {}
    protected fun updateViewState(vs: VS) = viewState.postValue(vs)

    protected fun <T> callApiFromFireStore(
        task: Task<T>,
        onSuccess: (T) -> Unit,
        onError: () -> Unit = {},
        isShowLoading: Boolean = false
    ) {
        if (isShowLoading) {
            loadingProgress.value = true
        }

        task.run {
            addOnSuccessListener { result ->
                onSuccess(result)
                loadingProgress.value = false
            }
            addOnFailureListener {
                onError()
                showErrorPopup(it.message.toString())
                loadingProgress.value = false
            }
        }
    }

    protected fun uploadImage(
        image: ByteArray,
        onSuccess: (String) -> Unit
    ) {
        val path = "image/place/IMG_${System.currentTimeMillis()}.jpg"
        val imageRef = FirebaseStorage.getInstance().reference.child(path)

        imageRef.putBytes(image)
            .addOnSuccessListener {
                imageRef.downloadUrl
                    .addOnCompleteListener {
                        val downloadUrl = it.result.toString()
                        onSuccess(downloadUrl)
                    }.addOnFailureListener {
                        showErrorPopup(it.message.toString())
                    }
            }.addOnFailureListener {
                showErrorPopup(it.message.toString())
            }
    }
}