package com.tuandev.simplemapproject.data.models

data class RouteItem(
    var isStart: Boolean = false,
    val place: Place,
    var itemState: String = NOT_VISITED,
    var itemIndex: Int = 0,
) {
    companion object {
        const val VISITED = "visited"
        const val NOT_VISITED = "not_visited"
        const val SELECTED = "selected"
    }

    fun areContentsTheSame(item: RouteItem) = this == item
    fun areItemsTheSame(item: RouteItem) = this.place.id == item.place.id
}