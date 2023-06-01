package com.tuandev.simplemapproject.ui.splash.suggest.routeDetail

import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.base.BaseFragment
import com.tuandev.simplemapproject.base.ViewState
import com.tuandev.simplemapproject.data.models.OptionItem
import com.tuandev.simplemapproject.data.models.RouteItem
import com.tuandev.simplemapproject.databinding.FragmentRouteDetailBinding
import com.tuandev.simplemapproject.extension.handleHighlightSpannable
import com.tuandev.simplemapproject.extension.show
import com.tuandev.simplemapproject.extension.showIf
import com.tuandev.simplemapproject.ui.splash.suggest.SuggestFragment
import com.tuandev.simplemapproject.ui.splash.suggest.routeDetail.adapter.RouteItemAdapter
import com.tuandev.simplemapproject.widget.ConfirmMessageDialog
import com.tuandev.simplemapproject.widget.markerselecteddialog.OptionItemDialog
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
            is RouteDetailViewState.OnSuggestListUpdated -> {
                viewModel.mUserFeature?.run {
                    binding?.run {
                        if (viewState.estimatedTime > availableTime) {
                            showDialogReachOutTime(viewState)
                        } else {
                            tvEstimatedTime.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.blackTextColor
                                )
                            )
                            tvEstimatedTime.text = context?.getString(
                                R.string.estimated_time,
                                getFormattedTimeString(viewState.estimatedTime)
                            )?.handleHighlightSpannable(
                                listOf("Estimated time:")
                            )
                            routeItemAdapter?.submitList(viewState.suggestList.toList())
                            (parentFragment as? SuggestFragment)?.updateSuggestRouteList(viewState.suggestList.toList())
                        }
                    }
                }
            }
            is RouteDetailViewState.OnUpdateCurrentPlace -> {
                routeItemAdapter?.notifyItemRangeChanged(0, viewState.suggestList.size)
            }
            is RouteDetailViewState.OnFetchNodeLineDataSuccess -> {
                handleLoadLocalData()
            }
        }
    }

    private fun showDialogReachOutTime(viewState: RouteDetailViewState.OnSuggestListUpdated) {
        ConfirmMessageDialog(
            title = "Message",
            message = "This update will reach out your available time. Are you sure to continue?"
        ).apply {
            successAction = {
                binding?.run {
                    routeItemAdapter?.submitList(viewState.suggestList.toList())
                    tvEstimatedTime.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.maximumRed
                        )
                    )
                    tvEstimatedTime.text = context?.getString(
                        R.string.estimated_time,
                        getFormattedTimeString(viewState.estimatedTime)
                    )?.handleHighlightSpannable(
                        listOf("Estimated time:")
                    )
                }
            }
            cancelAction = {
                viewModel.restoreSavedSuggestList()
            }
        }.show(childFragmentManager, null)
    }

    override fun initView() {
        routeItemAdapter = RouteItemAdapter(requireContext())
        binding?.run {
            rvRecommendPlace.run {
                adapter = routeItemAdapter
                itemAnimator = DefaultItemAnimator()
                layoutManager = LinearLayoutManager(context)
            }
        }

        viewModel.fetchAllNodesAndLines()
    }

    private fun handleLoadLocalData() {
        (parentFragment as? SuggestFragment)?.run {
            viewModel.run {
                mUserFeature = getUserFeature()

                if (mUserFeature != null) {
                    updateFeatureView()
                    getSaveSuggestList().let { saveSuggestList ->
                        if (saveSuggestList.isNotEmpty()) {
                            viewModel.updateSuggestList(saveSuggestList)
                        } else {
                            suggestRoute()
                        }
                    }
                } else {
                    showFeatureQuestionFragment(false)
                }
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
                (parentFragment as? SuggestFragment)?.showFeatureQuestionFragment(false)
            }

            btnAddPlace.setOnClickListener {
                showChoosePlaceDialog { placeId ->
                    viewModel.handleAddRouteItem(placeId.toInt())
                }
            }

            suggestFragment.run {
                onUserFeatureUpdatedListener = { userFeature ->
                    viewModel.run {
                        mUserFeature = userFeature
                        updateFeatureView()
                        suggestRoute()
                    }

                }
            }

            routeItemAdapter?.onItemClick = { position ->
                OptionItemDialog(
                    title = "Choose your action",
                    optionList = listOf(
                        OptionItem(OptionItem.KEY_OPEN_PLACE_DETAIL, "View place's detail"),
                        OptionItem(
                            OptionItem.KEY_UPDATE_CURRENT_PLACE,
                            "Update your current to this place"
                        ),
                        OptionItem(OptionItem.KEY_REPLACE_PLACE, "Replace this place"),
                        OptionItem(OptionItem.KEY_REMOVE_PLACE, "Remove this place")
                    )
                ).apply {
                    onItemClick = { key ->
                        when (key) {
                            OptionItem.KEY_REPLACE_PLACE -> {
                                if (viewModel.getSuggestList()[position].itemState == RouteItem.VISITED) {
                                    viewModel.showMessagePopup("Replacing a VISITED place is not allowed")
                                } else {
                                    showChoosePlaceDialog { placeId ->
                                        viewModel.handleReplaceItem(
                                            placeId = placeId.toInt(),
                                            replaceIndex = position
                                        )
                                    }
                                }
                            }
                            OptionItem.KEY_REMOVE_PLACE -> {
                                viewModel.handleDeleteSuggestNode(position)
                            }
                            OptionItem.KEY_UPDATE_CURRENT_PLACE -> {
                                viewModel.updateCurrentPlace(position)
                            }
                        }
                    }
                }.show(childFragmentManager, null)
            }
        }
    }

    private fun showChoosePlaceDialog(onItemSelected: (String) -> Unit) {
        val placeOptionList = viewModel.getAddablePlace().map { place ->
            if (place.game != null) {
                OptionItem(place.id.toString(), "Game: ${place.game.name}")
            } else {
                OptionItem(place.id.toString(), "Place: ${place.name}")
            }
        }

        OptionItemDialog(
            title = "Choose a place",
            optionList = placeOptionList,
            isSearchEnable = true
        ).apply {
            onItemClick = { placeId ->
                onItemSelected(placeId)

            }
        }.show(childFragmentManager, null)
    }

    private fun updateFeatureView() {
        binding?.run {
            viewModel.mUserFeature?.run {
                llButton.show()
                llUserFeature.show()
                val gameType = when {
                    isFamilyOnly -> context?.getString(R.string.game_type, "Family")
                    isThrillOnly -> context?.getString(R.string.game_type, "Thrill")
                    else -> context?.getString(R.string.game_type, "Thrill & Family")
                }
                tvGameType.text = gameType?.handleHighlightSpannable(listOf("Typical of games:"))
                tvMaximumThrill.showIf(maxThrill != null)
                tvMaximumThrill.text =
                    context?.getString(R.string.maximum_thrill_level, maxThrill?.name)
                        ?.handleHighlightSpannable(
                            listOf("Maximum thrill level:")
                        )
                tvAvailableTime.text =
                    context?.getString(
                        R.string.available_time,
                        getFormattedTimeString(availableTime)
                    )?.handleHighlightSpannable(
                        listOf("Available time:")
                    )
            }
        }
    }

    private fun getFormattedTimeString(availableTime: Float): String? {
        val hour = availableTime.toInt()
        val min = ((availableTime - hour) * 60).toInt()
        return context?.getString(R.string.formatted_time, hour, min)
    }

}