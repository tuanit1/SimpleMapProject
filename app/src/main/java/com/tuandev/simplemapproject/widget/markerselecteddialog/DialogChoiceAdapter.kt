package com.tuandev.simplemapproject.widget.markerselecteddialog

import android.view.LayoutInflater
import android.view.ViewGroup
import com.tuandev.simplemapproject.base.BaseListAdapter
import com.tuandev.simplemapproject.base.BaseViewHolder
import com.tuandev.simplemapproject.data.models.OptionItem
import com.tuandev.simplemapproject.databinding.OptionChoiceItemBinding

class DialogChoiceAdapter:
    BaseListAdapter<OptionItem, DialogChoiceAdapter.DialogChoiceViewHolder>() {

    var onItemClick: (OptionItem) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogChoiceViewHolder {
        val binding = OptionChoiceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DialogChoiceViewHolder(binding)
    }

    inner class DialogChoiceViewHolder(
        private val binding: OptionChoiceItemBinding
    ): BaseViewHolder<OptionItem>(binding.root) {
        override fun bind(item: OptionItem) {
            binding.run {
                tvItem.text = item.title
                root.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    }
}