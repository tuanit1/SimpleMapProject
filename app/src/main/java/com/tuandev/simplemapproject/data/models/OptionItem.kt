package com.tuandev.simplemapproject.data.models

data class OptionItem(
    val key: String,
    val title: String
) {
    companion object {
        const val KEY_REMOVE_MAP_ITEM = "key_remove_map_item"
    }

    fun areContentsTheSame(item: OptionItem) = this == item

    fun areItemsTheSame(item: OptionItem) = this.key == item.key
}