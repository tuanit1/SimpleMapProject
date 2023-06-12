package com.tuandev.simplemapproject.widget.imageListDialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso
import com.tuandev.simplemapproject.data.models.ImageData
import com.tuandev.simplemapproject.databinding.FragmentImageItemBinding

class ImageItemFragment(
    private val imageData: ImageData,
    private var onDeleteItem: (Long) -> Unit = {}
) : Fragment() {

    private var binding: FragmentImageItemBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImageItemBinding.inflate(inflater, container, false)

        binding?.run {

            Picasso.get()
                .load(imageData.url)
                .into(ivPhoto)

            ivDelete.setOnClickListener {
                onDeleteItem(imageData.getId())
            }
        }

        return binding?.root
    }
}