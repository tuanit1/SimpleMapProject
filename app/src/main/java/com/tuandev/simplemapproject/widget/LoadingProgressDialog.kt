package com.tuandev.simplemapproject.widget

import com.tuandev.simplemapproject.base.BaseDialogFragment
import com.tuandev.simplemapproject.databinding.DialogLoadingProgressBinding
import com.tuandev.simplemapproject.extension.getHeightScreen
import com.tuandev.simplemapproject.extension.getWidthScreen

class LoadingProgressDialog :
    BaseDialogFragment<DialogLoadingProgressBinding>(DialogLoadingProgressBinding::inflate) {
    override fun onStart() {
        super.onStart()
        context?.run {
            dialog?.window?.run {
                attributes = attributes.apply {
                    width = getWidthScreen()
                    height = getHeightScreen()
                }
            }
        }
    }
}