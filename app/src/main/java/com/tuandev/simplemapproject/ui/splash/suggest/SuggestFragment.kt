package com.tuandev.simplemapproject.ui.splash.suggest

import androidx.fragment.app.viewModels
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.base.BaseFragment
import com.tuandev.simplemapproject.base.ViewState
import com.tuandev.simplemapproject.databinding.FragmentSuggestBinding
import com.tuandev.simplemapproject.extension.openFragment
import com.tuandev.simplemapproject.ui.splash.suggest.routedetail.RouteDetailFragment
import com.tuandev.simplemapproject.ui.splash.suggest.suggestmap.SuggestMapFragment

class SuggestFragment : BaseFragment<FragmentSuggestBinding, SuggestViewModel, ViewState>(FragmentSuggestBinding::inflate) {

    companion object {
        fun newInstance() = SuggestFragment()
    }

    override val viewModel: SuggestViewModel by viewModels()
    override val viewStateObserver: (viewState: ViewState) -> Unit = {}

    override fun initView() {
        openSuggestMapFragment()
    }

    private fun getContainerId() = R.id.container_suggest

    private fun openSuggestMapFragment(){
        openFragment(
            containerId = getContainerId(),
            fragment = SuggestMapFragment.newInstance()
        )
    }

    fun showRouteDetailFragment(){
        openFragment(
            containerId = getContainerId(),
            fragment = RouteDetailFragment.newInstance()
        )
    }
}