package com.tuandev.simplemapproject.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.databinding.LayoutCustomCircleImageBinding

class CustomCircleImage @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {
    private var binding: LayoutCustomCircleImageBinding? = null

    init {
        initView(attrs)
    }

    private fun initView(attrs: AttributeSet?) {
        binding = LayoutCustomCircleImageBinding.inflate(LayoutInflater.from(context), this, true)
        val styleAttrs = context.obtainStyledAttributes(attrs, R.styleable.CustomCircleImage)
        try {
            binding?.apply {
                val size = styleAttrs.getDimensionPixelSize(R.styleable.CustomCircleImage_size, 0)
                val resId = styleAttrs.getResourceId(R.styleable.CustomCircleImage_android_src, 0)
                iv.run {
                    layoutParams.width = size
                    layoutParams.height = size
                    setImageResource(resId)
                    requestLayout()
                }
                rl.run {
                    layoutParams.width = size
                    layoutParams.height = size
                    requestLayout()
                }
            }
        } finally {
            styleAttrs.recycle()
        }
    }

    fun setRes(res: Int) {
        binding?.iv?.setImageResource(res)
    }
}