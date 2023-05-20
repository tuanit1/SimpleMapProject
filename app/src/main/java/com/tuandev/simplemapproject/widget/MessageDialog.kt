package com.tuandev.simplemapproject.widget

import com.tuandev.simplemapproject.base.BaseDialogFragment
import com.tuandev.simplemapproject.databinding.DialogErrorMessageBinding

class MessageDialog(
    val title: String,
    val message: String
) : BaseDialogFragment<DialogErrorMessageBinding>(DialogErrorMessageBinding::inflate) {
    override fun initView() {
        binding?.run {
            tvTitle.text = title
            tvMessage.text = message
            rlButton.setOnClickListener {
                dismiss()
            }
        }
    }
}
