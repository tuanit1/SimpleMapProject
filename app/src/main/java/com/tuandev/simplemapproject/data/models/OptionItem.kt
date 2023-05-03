package com.tuandev.simplemapproject.data.models

data class OptionItem(
    val key: String,
    val title: String
) {
    fun areContentsTheSame(item: OptionItem) = this == item

    fun areItemsTheSame(item: OptionItem) = this.key == item.key
}