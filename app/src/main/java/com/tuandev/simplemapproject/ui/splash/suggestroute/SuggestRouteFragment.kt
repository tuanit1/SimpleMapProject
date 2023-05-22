package com.tuandev.simplemapproject.ui.splash.suggestroute

import androidx.fragment.app.viewModels
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.base.BaseFragment
import com.tuandev.simplemapproject.base.ViewState
import com.tuandev.simplemapproject.base.map.BaseMapFragment
import com.tuandev.simplemapproject.databinding.FragmentSuggestRouteBinding
import com.tuandev.simplemapproject.extension.openFragment


class SuggestRouteFragment :
    BaseFragment<FragmentSuggestRouteBinding, SuggestRouteViewModel, ViewState>(
        FragmentSuggestRouteBinding::inflate
    ) {

    companion object {
        fun newInstance() = SuggestRouteFragment()
    }

    private var mapFragment: BaseMapFragment? = null
    override val viewModel: SuggestRouteViewModel by viewModels()
    override val viewStateObserver: (viewState: ViewState) -> Unit = {}

    override fun initView() {
        mapFragment = BaseMapFragment.newInstance(BaseMapFragment.Companion.MapMode.SUGGEST_ROUTE)
        mapFragment?.let {
            openFragment(
                containerId = getContainerId(),
                fragment = it
            )

            parentActivity?.openRouteDetailFragment()
        }
    }

    private fun getContainerId() = R.id.container_suggest_route

    override fun initListener() {
    }
}