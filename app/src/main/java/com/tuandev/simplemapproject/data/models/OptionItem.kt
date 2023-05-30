package com.tuandev.simplemapproject.data.models

data class OptionItem(
    val key: String,
    val title: String
) {
    companion object {
        const val KEY_DELETE_MAP_ITEM = "key_delete_map_item"
        const val KEY_EDIT_MAP_ITEM = "key_edit_map_item"
        const val KEY_REPLACE_PLACE = "key_replace_place"
        const val KEY_REMOVE_PLACE = "key_remove_place"
        const val KEY_OPEN_PLACE_DETAIL = "key_open_place_detail"
        const val KEY_UPDATE_CURRENT_PLACE = "key_update_current_place"

    }

    fun areContentsTheSame(item: OptionItem) = this == item
    fun areItemsTheSame(item: OptionItem) = this.key == item.key
}