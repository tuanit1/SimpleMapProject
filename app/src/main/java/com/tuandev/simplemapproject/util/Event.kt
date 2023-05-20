package com.tuandev.simplemapproject.util

object Event {
    var onDeleteImageListener: (String, () -> Unit) -> Unit = { _, _ -> }
}