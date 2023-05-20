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

fun Any.toIntOrNull(): Int? {
    return try {
        this.toString().toInt()
    } catch (e: Exception) {
        null
    }
}

fun Float.toRoundedFloat(roundNumber: Int): Float {
    return try {
        String.format("%.${roundNumber}f", this).toFloat()
    } catch (e: Exception) {
        this
    }
}

