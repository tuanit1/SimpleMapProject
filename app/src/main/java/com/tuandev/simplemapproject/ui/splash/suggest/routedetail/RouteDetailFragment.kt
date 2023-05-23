package com.tuandev.simplemapproject.ui.splash.suggest.routedetail

import androidx.fragment.app.viewModels
import com.tuandev.simplemapproject.base.BaseFragment
import com.tuandev.simplemapproject.base.ViewState
import com.tuandev.simplemapproject.databinding.FragmentRouteDetailBinding

class RouteDetailFragment :
    BaseFragment<FragmentRouteDetailBinding, RouteDetailViewModel, ViewState>(
        FragmentRouteDetailBinding::inflate
    ) {

    companion object {
        fun newInstance() = RouteDetailFragment()
    }

    override val viewModel: RouteDetailViewModel by viewModels()
    override val viewStateObserver: (viewState: ViewState) -> Unit = {}

    override fun initListener() {
        binding?.run {
            ivBack.setOnClickListener {
                parentActivity?.invokeBackPress()
            }
        }
    }


}