package com.tuandev.simplemapproject.extension

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import com.tuandev.simplemapproject.util.Constants

fun FragmentActivity.addFragment(
    containerId: Int,
    fragment: Fragment,
    addToBackStack: Boolean = false,
    enterAnim: Int = 0,
    exitAnim: Int = 0,
    popEnter: Int = 0,
    popExit: Int = 0,
) {
    if (supportFragmentManager.findFragmentByTag(fragment.tag) == null) {
        supportFragmentManager.commit {
            setCustomAnimations(enterAnim, exitAnim, popEnter, popExit)
            add(containerId, fragment, fragment.tag)
            if (addToBackStack) {
                addToBackStack(fragment.tag)
            }
        }
    }
}

fun log(message: String?){
    Log.e(Constants.LOG_TAG, "$message")
}

fun Context.showToast(message: String?){
    Toast.makeText(this, "$message", Toast.LENGTH_SHORT).show()
}