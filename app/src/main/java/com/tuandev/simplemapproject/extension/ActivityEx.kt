package com.tuandev.simplemapproject.extension

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import com.tuandev.simplemapproject.data.models.PopBackStackOption
import com.tuandev.simplemapproject.util.Constants

fun FragmentActivity.openFragment(
    containerId: Int,
    fragment: Fragment,
    popBackStackOption: PopBackStackOption? = null,
    isReplace: Boolean = true,
    enterAnim: Int = 0,
    exitAnim: Int = 0,
    popEnter: Int = 0,
    popExit: Int = 0,
) {
    supportFragmentManager.run {
        if (findFragmentByTag(fragment.tag) == null) {
            popBackStackOption?.let {
                var count = backStackEntryCount
                when (it) {
                    is PopBackStackOption.PopOne -> {
                        if (count > 0) popBackStack()
                    }
                    is PopBackStackOption.PopAll -> {
                        while (count-- > 0) {
                            popBackStack()
                        }
                    }
                    is PopBackStackOption.PopCount -> {
                        for (i in it.count downTo 1) popBackStack()
                    }
                }
            }
            commit {
                setCustomAnimations(enterAnim, exitAnim, popEnter, popExit)
                if(isReplace){
                    replace(containerId, fragment, fragment.tag)
                }else{
                    add(containerId, fragment, fragment.tag)
                }
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