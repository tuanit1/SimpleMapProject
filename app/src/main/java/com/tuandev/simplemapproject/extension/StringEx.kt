package com.tuandev.simplemapproject.extension

import android.util.Log
import com.tuandev.simplemapproject.util.Constants
import java.util.Objects


fun Any.toIntOrNull(): Int?{
    return try {
        this.toString().toInt()
    } catch (e: Exception){
        log(e.message)
        null
    }
}