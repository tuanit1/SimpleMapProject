package com.tuandev.simplemapproject.data.models

data class Game(
    val id: Int,
    val name: String,
    val thrillLevel: ThrillLevel,
    val duration: Int = 120,
    val isAvailable: Boolean = true
)