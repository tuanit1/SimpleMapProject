package com.tuandev.simplemapproject.data.models

data class OptionItem(
    val key: String,
    val title: String
) {
    companion object {
        const val KEY_DELETE_MAP_ITEM = "key_delete_map_item"
        const val KEY_EDIT_MAP_ITEM = "key_edit_map_item"
    }

    fun areContentsTheSame(item: OptionItem) = this == item
    fun areItemsTheSame(item: OptionItem) = this.key == item.key
}