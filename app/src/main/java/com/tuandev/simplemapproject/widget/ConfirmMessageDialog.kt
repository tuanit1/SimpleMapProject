package com.tuandev.simplemapproject.widget

import com.tuandev.simplemapproject.base.BaseDialogFragment
import com.tuandev.simplemapproject.databinding.DialogConfirmMessageBinding
import com.tuandev.simplemapproject.databinding.DialogErrorMessageBinding

class ConfirmMessageDialog(
    private val title: String,
    private val message: String,
) : BaseDialogFragment<DialogConfirmMessageBinding>(DialogConfirmMessageBinding::inflate) {
    var successAction: () -> Unit = {}
    override fun initView() {
        binding?.run {
            tvTitle.text = title
            tvMessage.text = message
            btnOK.setOnClickListener {
                successAction()
                dismiss()
            }
            btnCancel.setOnClickListener {
                dismiss()
            }
        }
    }
}
