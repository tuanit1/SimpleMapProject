package com.tuandev.simplemapproject.widget.imagelistdialog

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tuandev.simplemapproject.data.models.ImageData
import com.tuandev.simplemapproject.util.Event

class ImagePagerAdapter(
    fragment: Fragment,
    private val listImage: MutableList<ImageData>
) : FragmentStateAdapter(fragment) {

    private var handleDeleteImage: (Long) -> Unit = {}

    init {
        handleDeleteImage = { id ->
            val index = listImage.indexOfFirst { it.getId() == id }
            val imagePath = listImage[index].name

            Event.onDeleteImageListener(imagePath) {
                listImage.removeAt(index)
                notifyItemRemoved(index)
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return listImage[position].getId()
    }

    override fun containsItem(itemId: Long) = listImage.any {
        it.getId() == itemId
    }

    override fun getItemCount() = listImage.size

    override fun createFragment(position: Int): Fragment {
        return ImageItemFragment(
            imageData = listImage[position],
            onDeleteItem = handleDeleteImage
        )
    }
}