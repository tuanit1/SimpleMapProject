package com.tuandev.simplemapproject.extension

import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.tuandev.simplemapproject.data.models.PopBackStackOption

fun Fragment.openFragment(
    containerId: Int,
    fragment: Fragment,
    isReplace: Boolean = false,
    popBackStackOption: PopBackStackOption? = null,
    enterAnim: Int = 0,
    exitAnim: Int = 0,
    popEnter: Int = 0,
    popExit: Int = 0,
    tag: String? = null
) {
    childFragmentManager.run {
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

                val fragmentTag = tag ?: fragment.tag

                if (isReplace) {
                    replace(containerId, fragment, fragmentTag)
                } else {
                    add(containerId, fragment, fragmentTag)
                }
                addToBackStack(tag ?: fragmentTag)
            }
        }
    }
}