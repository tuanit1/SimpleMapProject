package com.tuandev.simplemapproject.ui.splash.suggest

import androidx.fragment.app.viewModels
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.base.BaseFragment
import com.tuandev.simplemapproject.base.ViewState
import com.tuandev.simplemapproject.data.models.UserFeature
import com.tuandev.simplemapproject.databinding.FragmentSuggestBinding
import com.tuandev.simplemapproject.extension.openFragment
import com.tuandev.simplemapproject.ui.splash.suggest.routeDetail.RouteDetailFragment
import com.tuandev.simplemapproject.ui.splash.suggest.routeDetail.featureQuestion.FeatureQuestionFragment
import com.tuandev.simplemapproject.ui.splash.suggest.suggestMap.SuggestMapFragment

class SuggestFragment :
    BaseFragment<FragmentSuggestBinding, SuggestViewModel, ViewState>(FragmentSuggestBinding::inflate) {

    companion object {
        fun newInstance() = SuggestFragment()
    }

    override val viewModel: SuggestViewModel by viewModels()
    override val viewStateObserver: (viewState: ViewState) -> Unit = {}


    var onUserFeatureUpdatedListener: (UserFeature) -> Unit = {}
    override fun initView() {
        openSuggestMapFragment()
    }

    override fun initListener() {
        listenOnLiveData()
    }

    private fun listenOnLiveData() {
        viewModel.run {
            mUserFeature.observe(viewLifecycleOwner) { userFeature ->
                onUserFeatureUpdatedListener(userFeature)
            }
        }
    }

    private fun getContainerId() = R.id.container_suggest

    private fun openSuggestMapFragment() {
        openFragment(
            containerId = getContainerId(),
            fragment = SuggestMapFragment.newInstance()
        )
    }

    fun showRouteDetailFragment() {
        openFragment(
            containerId = getContainerId(),
            fragment = RouteDetailFragment.newInstance()
        )
    }

    fun showFeatureQuestionFragment() {
        openFragment(
            containerId = getContainerId(),
            fragment = FeatureQuestionFragment.newInstance()
        )
    }

    fun updateUserFeature(userFeature: UserFeature) {
        viewModel.updateUserFeature(userFeature)
    }
}