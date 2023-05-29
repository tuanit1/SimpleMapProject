package com.tuandev.simplemapproject.ui.splash.suggest.routeDetail

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.base.BaseFragment
import com.tuandev.simplemapproject.base.ViewState
import com.tuandev.simplemapproject.data.models.UserFeature
import com.tuandev.simplemapproject.databinding.FragmentRouteDetailBinding
import com.tuandev.simplemapproject.extension.show
import com.tuandev.simplemapproject.extension.showIf
import com.tuandev.simplemapproject.ui.splash.suggest.SuggestFragment
import com.tuandev.simplemapproject.ui.splash.suggest.routeDetail.adapter.RouteItemAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RouteDetailFragment :
    BaseFragment<FragmentRouteDetailBinding, RouteDetailViewModel, ViewState>(
        FragmentRouteDetailBinding::inflate
    ) {

    companion object {
        fun newInstance() = RouteDetailFragment()
    }

    private var routeItemAdapter: RouteItemAdapter? = null

    override val viewModel: RouteDetailViewModel by viewModels()
    override val viewStateObserver: (viewState: ViewState) -> Unit = { viewState ->
        when (viewState) {
            is RouteDetailViewState.OnSuggestRouteFinish -> {
                binding?.run {
                    tvEstimatedTime.show()
                    tvEstimatedTime.text = "Estimated time ${getFormattedTimeString(viewState.estimatedTime)}"
                    routeItemAdapter?.submitList(viewState.suggestList.toList())
                }
            }
        }
    }

    override fun initView() {
        viewModel.fetchAllNodesAndLines()

        routeItemAdapter = RouteItemAdapter(requireContext())
        binding?.run {
            rvRecommendPlace.run {
                adapter = routeItemAdapter
                itemAnimator = DefaultItemAnimator()
                layoutManager = LinearLayoutManager(context)
            }
        }
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
                tvGameType.show()
                tvGameType.text = when {
                    isFamilyOnly -> context?.getString(R.string.game_type, "Family")
                    isThrillOnly -> context?.getString(R.string.game_type, "Thrill")
                    else -> context?.getString(R.string.game_type, "Thrill & Family")
                }
                tvMaximumThrill.showIf(maxThrill != null)
                tvMaximumThrill.text =
                    context?.getString(R.string.maximum_thrill_level, maxThrill?.name)
                tvAvailableTime.show()
                tvAvailableTime.text = "Available time: ${getFormattedTimeString(availableTime)}"
            }
        }
    }

    private fun getFormattedTimeString(availableTime: Float): String? {
        val hour = availableTime.toInt()
        val min = ((availableTime - hour) * 60).toInt()
        return context?.getString(R.string.available_time, hour, min)
    }

}