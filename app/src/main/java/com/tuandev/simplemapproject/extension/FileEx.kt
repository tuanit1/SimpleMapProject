package com.tuandev.simplemapproject.extension

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

fun compressBitmapFromUri(context: Context, uri: Uri): Bitmap {
    val bitmap = if (Build.VERSION.SDK_INT < 28) {
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    } else {
        val source: ImageDecoder.Source =
            ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source)
    }

    val newWidth = 1024
    val newHeight = (bitmap.height * (newWidth.toFloat()/ bitmap.width)).toInt()
    return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
}

fun compressBitmap(bitmap: Bitmap): Bitmap{
    val newWidth = 1024
    val newHeight = (bitmap.height * (newWidth.toFloat() / bitmap.width)).toInt()
    return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
}