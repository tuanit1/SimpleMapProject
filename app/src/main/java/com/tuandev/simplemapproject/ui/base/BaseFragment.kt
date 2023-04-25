package com.tuandev.simplemapproject.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import dagger.hilt.android.AndroidEntryPoint

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

@AndroidEntryPoint
abstract class BaseFragment<VB : ViewBinding, VM: BaseViewModel>(
    private val inflate: Inflate<VB>
) : Fragment() {
    private var _binding: VB? = null
    val viewModel: VM by viewModels()
    val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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
        initView()
        initListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    open fun initView() {}
    open fun initListener() {}

}