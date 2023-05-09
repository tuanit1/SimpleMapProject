package com.tuandev.simplemapproject.activity

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.data.models.PopBackStackOption
import com.tuandev.simplemapproject.extension.openFragment
import com.tuandev.simplemapproject.ui.splash.SplashFragment
import com.tuandev.simplemapproject.ui.splash.home.HomeFragment
import com.tuandev.simplemapproject.ui.splash.login.LoginFragment
import com.tuandev.simplemapproject.ui.splash.toolmap.ToolMapFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        initListener()
    }

    private fun initView() {
        openSplashFragment()
    }

    private fun initListener() {
        handleBackPress()
    }

    private fun handleBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val count = supportFragmentManager.backStackEntryCount
                if (count > 1) {
                    when (val currentFragment = supportFragmentManager.fragments.last()) {
                        else -> {
                            supportFragmentManager.popBackStack()
                        }
                    }
                } else {
                    finish()
                }
            }
        })
    }

    private fun Fragment.handleChildFragmentBackPress() {
        val count = childFragmentManager.backStackEntryCount
        if (count > 1) {
            childFragmentManager.popBackStack()
        } else {
            supportFragmentManager.popBackStack()
        }
    }

    private fun getContainerId() = R.id.main_activity_container

    fun openLoginFragment() {
        openFragment(
            containerId = getContainerId(),
            fragment = LoginFragment.newInstance(),
            popBackStackOption = PopBackStackOption.PopAll
        )
    }

    fun openHomeFragment(){
        openFragment(
            containerId = getContainerId(),
            fragment = HomeFragment.newInstance(),
            popBackStackOption = PopBackStackOption.PopAll
        )
    }

    fun openSplashFragment(){
        openFragment(
            containerId = getContainerId(),
            fragment = SplashFragment.newInstance()
        )
    }

    fun openToolMapFragment(){
        openFragment(
            containerId = getContainerId(),
            fragment = ToolMapFragment.newInstance(),
            popBackStackOption = PopBackStackOption.PopAll
        )
    }
}
