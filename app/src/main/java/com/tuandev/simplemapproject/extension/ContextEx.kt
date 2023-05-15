package com.tuandev.simplemapproject.extension

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.Toast

fun Context.getWidthScreen(): Int{
    val dimension = DisplayMetrics()
    val wm = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    wm?.defaultDisplay?.getMetrics(dimension)
    return dimension.widthPixels
}

fun Context.getHeightScreen(): Int{
    val dimension = DisplayMetrics()
    val wm = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    wm?.defaultDisplay?.getMetrics(dimension)
    return dimension.heightPixels
}

fun Context.showToast(message: String?){
    Toast.makeText(this, "$message", Toast.LENGTH_SHORT).show()
}