package com.tuandev.simplemapproject.data.models

sealed class PopBackStackOption {
    object PopOne : PopBackStackOption()
    object PopAll : PopBackStackOption()
    class PopCount(val count: Int) : PopBackStackOption()
}