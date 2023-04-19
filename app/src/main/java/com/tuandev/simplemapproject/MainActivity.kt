package com.tuandev.simplemapproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.SupportMapFragment
import com.tuandev.simplemapproject.extension.addFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addFragment(
            containerId = getContainerId(),
            fragment = MapFragment.newInstance(),
            addToBackStack = true
        )
    }

    private fun getContainerId() = R.id.main_activity_fragment_container
}