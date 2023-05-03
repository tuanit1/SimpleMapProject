package com.tuandev.simplemapproject.base

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.tuandev.simplemapproject.data.models.OptionItem


abstract class BaseListAdapter<T : Any, VH : BaseViewHolder<T>> :
    ListAdapter<T, VH>(ItemDiffCallback<T>()) {
    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}

abstract class BaseViewHolder<T>(
    view: View
) : ViewHolder(view) {
    abstract fun bind(item: T)
}


class ItemDiffCallback<T : Any> : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return when (oldItem) {
            is OptionItem -> {
                oldItem.areItemsTheSame(newItem as OptionItem)
            }

            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return when (oldItem) {
            is OptionItem -> {
                oldItem.areContentsTheSame(newItem as OptionItem)
            }

            else -> false
        }
    }
}





