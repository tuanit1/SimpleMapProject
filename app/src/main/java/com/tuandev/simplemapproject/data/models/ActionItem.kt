package com.tuandev.simplemapproject.data.models

data class ActionItem(
    val title: String,
    val action: () -> Unit
)