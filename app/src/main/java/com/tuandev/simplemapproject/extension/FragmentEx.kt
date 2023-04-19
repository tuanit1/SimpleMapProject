package com.tuandev.simplemapproject.extension

import androidx.fragment.app.Fragment
import androidx.fragment.app.commit

fun Fragment.addFragment(
    containerId: Int,
    fragment: Fragment,
    addToBackStack: Boolean = false,
    enterAnim: Int = 0,
    exitAnim: Int = 0,
    popEnter: Int = 0,
    popExit: Int = 0,
) {
    if (parentFragmentManager.findFragmentByTag(fragment.tag) == null) {
        parentFragmentManager.commit {
            setCustomAnimations(enterAnim, exitAnim, popEnter, popExit)
            add(containerId, fragment, fragment.tag)
            if (addToBackStack) {
                addToBackStack(fragment.tag)
            }
        }
    }
}