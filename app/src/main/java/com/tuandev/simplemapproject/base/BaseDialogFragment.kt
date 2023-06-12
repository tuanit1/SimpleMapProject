package com.tuandev.simplemapproject.base

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tuandev.simplemapproject.extension.getHeightScreen
import com.tuandev.simplemapproject.extension.getWidthScreen

abstract class BaseDialogFragment<VB : ViewBinding>(
    private val inflate: Inflate<VB>,
): DialogFragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding
    private var isReachedMaxSize = false

    companion object {
        const val WIDTH_RATIO = 0.8f
        const val HEIGHT_RATIO = 0.8f
    }

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

        context?.run {
            dialog?.window?.run {
                attributes = attributes.apply {
                    width = (getWidthScreen() * WIDTH_RATIO).toInt()
                }
            }
        }
    }

    fun checkReachedMaxSize(measuredHeight: Int) {
        val heightScreen = context?.getHeightScreen() ?: 0
        if (measuredHeight > heightScreen * HEIGHT_RATIO){
            if(!isReachedMaxSize){
                dialog?.window?.run {
                    attributes = attributes.apply {
                        isReachedMaxSize = true
                        height = (heightScreen * HEIGHT_RATIO).toInt()
                    }
                }
            }
        }else {
            dialog?.window?.run {
                if(isReachedMaxSize){
                    attributes = attributes.apply {
                        isReachedMaxSize = false
                        height = measuredHeight
                    }
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogDescription = Dialog(requireContext())
        dialogDescription.window?.run {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            requestFeature(Window.FEATURE_NO_TITLE)
            isCancelable = true
        }
        return dialogDescription
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()
        initListener()
    }

    open fun initView() {}
    open fun initListener() {}
}