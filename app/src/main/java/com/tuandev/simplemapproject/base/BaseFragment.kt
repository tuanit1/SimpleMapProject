package com.tuandev.simplemapproject.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.tuandev.simplemapproject.widget.CommonProgressDialog

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class BaseFragment<VB : ViewBinding, VM : BaseViewModel<VS>, VS : ViewState>(
    private val inflate: Inflate<VB>,
) : Fragment() {
    private var _binding: VB? = null
    protected val binding get() = _binding

    private val commonProgressBar by lazy { CommonProgressDialog(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflate.invoke(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.viewState.observe(viewLifecycleOwner) {
            viewStateObserver(it)
        }

        listenLiveData()
        initView()
        initListener()
    }

    private fun listenLiveData() {
        viewModel.loadingProgress.observe(viewLifecycleOwner) { isShow ->
            if (isShow) {
                if (!commonProgressBar.isShowing) {
                    commonProgressBar.show()
                }
            } else {
                commonProgressBar.dismiss()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    open fun initView() {}
    open fun initListener() {}

    protected abstract val viewModel: VM

    protected abstract val viewStateObserver: (viewState: VS) -> Unit
}