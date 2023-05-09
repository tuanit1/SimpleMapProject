package com.tuandev.simplemapproject.ui.splash

import androidx.fragment.app.viewModels
import com.tuandev.simplemapproject.base.BaseFragment
import com.tuandev.simplemapproject.base.ViewState
import com.tuandev.simplemapproject.databinding.FragmentSplashBinding

class SplashFragment : BaseFragment<FragmentSplashBinding, SplashViewModel, ViewState>(FragmentSplashBinding::inflate) {

    override val viewModel: SplashViewModel by viewModels()
    override val viewStateObserver: (viewState: ViewState) -> Unit = {}

    companion object {
        @JvmStatic
        fun newInstance() = SplashFragment()
    }

    override fun initListener() {
        binding?.run {
            tv.setOnClickListener {
                parentActivity?.openLoginFragment()
            }
        }
    }



}