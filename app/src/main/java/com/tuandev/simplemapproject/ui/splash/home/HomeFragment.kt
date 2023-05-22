package com.tuandev.simplemapproject.ui.splash.home

import androidx.fragment.app.viewModels
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.base.BaseFragment
import com.tuandev.simplemapproject.base.ViewState
import com.tuandev.simplemapproject.databinding.FragmentHomeBinding


class HomeFragment :
    BaseFragment<FragmentHomeBinding, HomeViewModel, ViewState>(FragmentHomeBinding::inflate) {

    companion object {
        fun newInstance() = HomeFragment()
    }

    override val viewModel: HomeViewModel by viewModels()
    override val viewStateObserver: (viewState: ViewState) -> Unit = {}

    override fun initView() {

    }

    private fun getContainerId() = R.id.container_home_fragment
}