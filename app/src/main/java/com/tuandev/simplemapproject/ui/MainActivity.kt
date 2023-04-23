package com.tuandev.simplemapproject.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.extension.addFragment
import com.tuandev.simplemapproject.ui.maptool.MapToolFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addFragment(
            containerId = getContainerId(),
                        fragment = MapToolFragment.newInstance(),
            addToBackStack = true
        )
    }

    private fun getContainerId() = R.id.main_activity_fragment_container
}