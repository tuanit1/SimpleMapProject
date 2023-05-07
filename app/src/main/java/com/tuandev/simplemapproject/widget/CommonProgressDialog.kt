package com.tuandev.simplemapproject.widget

import android.app.Dialog
import android.content.Context
import android.view.Window
import com.tuandev.simplemapproject.R

class CommonProgressDialog(context: Context): Dialog(context) {
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_progress)
        window?.run {
            setDimAmount(0f)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
        setCancelable(true)
    }
}