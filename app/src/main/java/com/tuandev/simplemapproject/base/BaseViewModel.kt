package com.tuandev.simplemapproject.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseViewModel<VS : ViewState> : ViewModel() {
    val viewState: MutableLiveData<VS> by lazy { MutableLiveData<VS>() }
    var loadingProgress: MutableLiveData<Boolean> = MutableLiveData()
    protected fun updateViewState(vs: VS) = viewState.postValue(vs)

    protected fun <T> fetchFromFireStore(
        task: Task<T>,
        onSuccess: (T) -> Unit,
        onError: () -> Unit = {},
        isShowLoading: Boolean = false
    ) {
        if(isShowLoading){
            loadingProgress.value = true
        }

        task.run {
            addOnSuccessListener { result ->
                onSuccess(result)
                loadingProgress.value = false
            }
            addOnFailureListener {
                onError()
                loadingProgress.value = false
            }
        }
    }

}