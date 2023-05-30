package com.tuandev.simplemapproject.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.databinding.LayoutCustomButtonBinding

class CustomButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {
    private var binding: LayoutCustomButtonBinding? = null

    var showProgress: (Boolean) -> Unit = {}

    init {
        initView(attrs)
    }

    private fun initView(attrs: AttributeSet?) {
        binding = LayoutCustomButtonBinding.inflate(LayoutInflater.from(context), this, true)
        val styleAttrs = context.obtainStyledAttributes(attrs, R.styleable.CustomButton)
        try {
            binding?.apply {
                tvTitle.text = styleAttrs.getString(R.styleable.CustomButton_text)

                showProgress = {
                    if (it) {
                        progressBar.visibility = VISIBLE
                        tvTitle.visibility = GONE
                    } else {
                        progressBar.visibility = GONE
                        tvTitle.visibility = VISIBLE
                    }
                }
            }
        } finally {
            styleAttrs.recycle()
        }
    }
}