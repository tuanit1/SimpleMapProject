package com.tuandev.simplemapproject.widget

import com.tuandev.simplemapproject.base.BaseDialogFragment
import com.tuandev.simplemapproject.databinding.DialogErrorMessageBinding

class ErrorMessageDialog(
    val message: String
) : BaseDialogFragment<DialogErrorMessageBinding>(DialogErrorMessageBinding::inflate) {
    override fun initView() {
        binding?.run {
            tvMessage.text = message
            rlButton.setOnClickListener {
                dismiss()
            }
        }
    }
}
