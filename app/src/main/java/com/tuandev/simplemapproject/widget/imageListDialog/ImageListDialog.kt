package com.tuandev.simplemapproject.widget.imageListDialog

import com.tuandev.simplemapproject.base.BaseDialogFragment
import com.tuandev.simplemapproject.data.models.ImageData
import com.tuandev.simplemapproject.databinding.DialogImageListBinding
import com.tuandev.simplemapproject.extension.getHeightScreen
import com.tuandev.simplemapproject.extension.getWidthScreen

class ImageListDialog(
    private val imageList: MutableList<ImageData>
) : BaseDialogFragment<DialogImageListBinding>(DialogImageListBinding::inflate) {

    override fun onStart() {
        super.onStart()

        context?.run {
            dialog?.window?.run {
                attributes = attributes.apply {
                    width = (getWidthScreen() * WIDTH_RATIO).toInt()
                    height = (getHeightScreen() * HEIGHT_RATIO).toInt()
                }
            }
        }
    }

    override fun initListener() {
        binding?.run {
            imageViewPager.adapter = ImagePagerAdapter(this@ImageListDialog, imageList)
            ivClose.setOnClickListener {
                dismiss()
            }
        }
    }
}