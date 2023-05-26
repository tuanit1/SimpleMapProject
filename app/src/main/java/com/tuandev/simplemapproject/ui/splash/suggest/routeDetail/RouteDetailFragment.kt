package com.tuandev.simplemapproject.ui.splash.suggest.routeDetail

import androidx.fragment.app.viewModels
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.base.BaseFragment
import com.tuandev.simplemapproject.base.ViewState
import com.tuandev.simplemapproject.data.models.UserFeature
import com.tuandev.simplemapproject.databinding.FragmentRouteDetailBinding
import com.tuandev.simplemapproject.ui.splash.suggest.SuggestFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RouteDetailFragment :
    BaseFragment<FragmentRouteDetailBinding, RouteDetailViewModel, ViewState>(
        FragmentRouteDetailBinding::inflate
    ) {

    companion object {
        fun newInstance() = RouteDetailFragment()
    }

    override val viewModel: RouteDetailViewModel by viewModels()
    override val viewStateObserver: (viewState: ViewState) -> Unit = {}

    override fun initView() {
        viewModel.fetchAllNodesAndLines()
    }

    override fun initListener() {

        val suggestFragment = (parentFragment as SuggestFragment)

        binding?.run {
            ivBack.setOnClickListener {
                parentActivity?.invokeBackPress()
            }

            btnRegenerateRoute.setOnClickListener {
                (parentFragment as? SuggestFragment)?.showFeatureQuestionFragment()
            }

            suggestFragment.run {
                onUserFeatureUpdatedListener = { userFeature ->
                    updateFeatureView(userFeature)
                    suggestRoute(userFeature)
                }
            }
        }
    }

    private fun suggestRoute(userFeature: UserFeature) {
        viewModel.suggestGame(userFeature)
    }

    private fun updateFeatureView(userFeature: UserFeature) {
        binding?.run {
            userFeature.run {
                tvMaximumThrill.text =
                    context?.getString(R.string.maximum_thrill_level, maxThrill?.name)
                val hour = availableTime.toInt()
                val min = ((availableTime - hour) * 60).toInt()
                tvAvailableTime.text = context?.getString(R.string.available_time, hour, min)
            }
        }
    }

}