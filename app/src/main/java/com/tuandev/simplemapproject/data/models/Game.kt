package com.tuandev.simplemapproject.data.models

data class Game(
    val id: Int,
    val name: String,
    val zoneId: String,
    val thrillLevelId: Int,
    val duration: Int = 120,
    val isAvailable: Boolean = true
)