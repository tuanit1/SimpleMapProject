package com.tuandev.simplemapproject.widget.placeInfoDialog

import android.view.LayoutInflater
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.base.BaseListAdapter
import com.tuandev.simplemapproject.base.BaseViewHolder
import com.tuandev.simplemapproject.data.models.ImageData
import com.tuandev.simplemapproject.databinding.LayoutItemImageBinding

class ImageInfoAdapter :
    BaseListAdapter<ImageData, ImageInfoAdapter.ImageInfoViewHolder>() {
    inner class ImageInfoViewHolder(private val binding: LayoutItemImageBinding) :
        BaseViewHolder<ImageData>(binding.root) {
        override fun bind(item: ImageData) {
            binding.run {
                Picasso.get()
                    .load(item.url)
                    .placeholder(R.drawable.img_place_holder)
                    .into(ivPhoto)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageInfoViewHolder {
        val binding =
            LayoutItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageInfoViewHolder(binding)
    }
}