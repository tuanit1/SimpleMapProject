package com.tuandev.simplemapproject.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage

abstract class BaseViewModel<VS : ViewState> : ViewModel() {
    val viewState: MutableLiveData<VS> by lazy { MutableLiveData<VS>() }
    var loadingProgressLiveData: MutableLiveData<Boolean> = MutableLiveData()
    var showErrorPopup: (errorMessage: String) -> Unit = {}
    var showMessagePopup: (message: String) -> Unit = {}
    protected fun updateViewState(vs: VS) = viewState.postValue(vs)

    protected fun <T> callApiFromFireStore(
        task: Task<T>,
        onSuccess: (T) -> Unit,
        onError: () -> Unit = {},
        isShowLoading: Boolean = false
    ) {
        if (isShowLoading) {
            loadingProgressLiveData.value = true
        }

        task.run {
            addOnSuccessListener { result ->
                onSuccess(result)
                if (isShowLoading) loadingProgressLiveData.value = false

            }
            addOnFailureListener {
                onError()
                showErrorPopup(it.message.toString())
                if (isShowLoading) loadingProgressLiveData.value = false
            }
        }
    }

    protected fun uploadImage(
        image: ByteArray,
        onSuccess: (String, String) -> Unit
    ) {
        val path = "image/place/IMG_${System.currentTimeMillis()}.jpg"
        val imageRef = FirebaseStorage.getInstance().reference.child(path)

        loadingProgressLiveData.value = true

        imageRef.putBytes(image)
            .addOnSuccessListener {
                imageRef.downloadUrl
                    .addOnCompleteListener {
                        val downloadUrl = it.result.toString()
                        onSuccess(path, downloadUrl)
                        showMessagePopup("Photo has been uploaded")
                    }.addOnFailureListener {
                        loadingProgressLiveData.value = false
                        showErrorPopup(it.message.toString())
                    }
            }.addOnFailureListener {
                loadingProgressLiveData.value = false
                showErrorPopup(it.message.toString())
            }
    }

    protected fun removeImage(
        imageName: String,
        onSuccess: () -> Unit
    ) {
        loadingProgressLiveData.value = true
        FirebaseStorage.getInstance().reference.child(imageName).delete()
            .addOnCompleteListener {
                loadingProgressLiveData.value = false
                showMessagePopup("Photo has been removed")
                onSuccess()
            }
            .addOnFailureListener {
                loadingProgressLiveData.value = false
                showErrorPopup(it.message.toString())
            }
    }
}