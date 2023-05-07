package com.tuandev.simplemapproject.extension


fun Any.toFloatOrNull(): Float? {
    return try {
        this.toString().toFloat()
    } catch (e: Exception) {
        null
    }
}

fun Any.toDoubleOrNull(): Double? {
    return try {
        this.toString().toDouble()
    } catch (e: Exception) {
        null
    }
}

