package com.tuandev.simplemapproject.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.tuandev.simplemapproject.extension.getWidthScreen

abstract class BaseDialogFragment<VB : ViewBinding>(
    private val inflate: Inflate<VB>
): DialogFragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflate.invoke(inflater, container, false)
        return binding?.root
    }

    override fun onStart() {
        super.onStart()
        val screenWidth = requireContext().getWidthScreen()
        dialog?.window?.run {
            attributes = attributes.apply {
                width = (screenWidth * 0.8).toInt()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()
        initListener()
    }

    open fun initView() {}
    open fun initListener() {}
}