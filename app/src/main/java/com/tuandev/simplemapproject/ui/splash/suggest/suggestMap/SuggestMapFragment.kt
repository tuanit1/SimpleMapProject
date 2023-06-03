package com.tuandev.simplemapproject.ui.splash.suggest.suggestMap

import androidx.fragment.app.viewModels
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.base.BaseFragment
import com.tuandev.simplemapproject.base.ViewState
import com.tuandev.simplemapproject.base.map.BaseMapFragment
import com.tuandev.simplemapproject.data.models.RouteItem
import com.tuandev.simplemapproject.databinding.FragmentSuggestMapBinding
import com.tuandev.simplemapproject.extension.openFragment
import com.tuandev.simplemapproject.extension.show
import com.tuandev.simplemapproject.ui.splash.suggest.SuggestFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SuggestMapFragment :
    BaseFragment<FragmentSuggestMapBinding, SuggestMapViewModel, ViewState>(
        FragmentSuggestMapBinding::inflate
    ) {

    companion object {
        fun newInstance() = SuggestMapFragment()
    }

    private var mapFragment: BaseMapFragment? = null
    override val viewModel: SuggestMapViewModel by viewModels()
    override val viewStateObserver: (viewState: ViewState) -> Unit = {}

    override fun initView() {
        mapFragment = BaseMapFragment.newInstance(BaseMapFragment.Companion.MapMode.SUGGEST_ROUTE)
        mapFragment?.let {
            openFragment(
                containerId = getContainerId(),
                fragment = it
            )
        }
    }

    private fun getContainerId() = R.id.container_suggest_route

    override fun initListener() {

        binding?.run {
            llDest.setOnClickListener {
                (parentFragment as? SuggestFragment)?.showRouteDetailFragment()
            }

            parentActivity?.onActivityBackPressListener = {
                llDest.show()
            }

            (parentFragment as? SuggestFragment)?.run {
                onSuggestRouteUpdatedListener = { suggestRoute ->
                    handleSuggestRouteUpdated(suggestRoute)
                }
            }

            mapFragment?.onNodesLinesLoaded = {
                (parentFragment as? SuggestFragment)?.getSaveSuggestList()?.let { suggestRoute ->
                    handleSuggestRouteUpdated(suggestRoute)
                }
            }
        }
    }

    private fun handleSuggestRouteUpdated(suggestRoute: List<RouteItem>) {
        mapFragment?.handleSuggestRouteUpdated(suggestRoute)
    }
}