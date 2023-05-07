package com.tuandev.simplemapproject.ui

import androidx.fragment.app.viewModels
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.base.BaseFragment
import com.tuandev.simplemapproject.base.ViewState
import com.tuandev.simplemapproject.databinding.FragmentHomeBinding
import com.tuandev.simplemapproject.extension.addFragment
import com.tuandev.simplemapproject.ui.toolmap.ToolMapFragment


class HomeFragment :
    BaseFragment<FragmentHomeBinding, HomeViewModel, ViewState>(FragmentHomeBinding::inflate) {


    companion object {
        fun newInstance() = HomeFragment()
    }

    override val viewModel: HomeViewModel by viewModels()
    override val viewStateObserver: (viewState: ViewState) -> Unit = {}

    override fun initView() {
        addFragment(
            containerId = getContainerId(),
            fragment = ToolMapFragment.newInstance(),
            addToBackStack = true
        )
    }

    private fun getContainerId() = R.id.container_home_fragment
}