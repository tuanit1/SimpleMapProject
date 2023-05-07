package com.tuandev.simplemapproject.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.extension.addFragment
import com.tuandev.simplemapproject.ui.HomeFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addFragment(
            containerId = getContainerId(),
            fragment = HomeFragment.newInstance(),
            addToBackStack = true
        )
    }

    private fun getContainerId() = R.id.main_activity_fragment_container
}