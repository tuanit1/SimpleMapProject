package com.tuandev.simplemapproject.widget

import com.tuandev.simplemapproject.base.BaseDialogFragment
import com.tuandev.simplemapproject.databinding.DialogConfirmMessageBinding

class ConfirmMessageDialog(
    private val title: String,
    private val message: String,
    private val positiveTitle: String? = null,
    private val negativeTitle: String? = null
) : BaseDialogFragment<DialogConfirmMessageBinding>(DialogConfirmMessageBinding::inflate) {
    var successAction: () -> Unit = {}
    var cancelAction: () -> Unit = {}
    override fun initView() {
        binding?.run {
            tvTitle.text = title
            tvMessage.text = message

            tvPositive.text = positiveTitle ?: "Okay"
            tvNegative.text = negativeTitle ?: "Cancel"

            btnOK.setOnClickListener {
                successAction()
                dismiss()
            }
            btnCancel.setOnClickListener {
                cancelAction()
                dismiss()
            }
        }
    }
}
