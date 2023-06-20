package com.tuandev.simplemapproject.ui.splash.suggest.routeDetail

import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
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
import com.tuandev.simplemapproject.widget.markerSelectedDialog.OptionItemDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class RouteDetailFragment :
    BaseFragment<FragmentRouteDetailBinding, RouteDetailViewModel, RouteDetailViewState>(
        FragmentRouteDetailBinding::inflate
    ) {

    companion object {
        const val ADD_SELECTED_PLACE = "add_selected_place"
        const val REPLACE_SELECTED_PLACE = "replace_selected_place"
        fun newInstance() = RouteDetailFragment()
    }

    private var routeItemAdapter: RouteItemAdapter? = null
    private var saveReplaceIndex: Int? = null
    private var currentActionSelectPlace: String = ""

    override val viewModel: RouteDetailViewModel by viewModels()
    override val viewStateObserver: (viewState: ViewState) -> Unit = { viewState ->
        when (viewState) {
            is RouteDetailViewState.OnSuggestListUpdated -> {
                viewModel.mUserFeature?.run {
                    if (viewState.estimatedTime > availableTime && !viewModel.isFirstLoad) {
                        showDialogReachOutTime(viewState)
                    } else {
                        viewModel.isFirstLoad = false
                        handleUpdateUserFeatureView(viewState)
                    }
                }
            }
            is RouteDetailViewState.OnUpdateCurrentPlace -> {
                routeItemAdapter?.notifyItemRangeChanged(0, viewState.suggestList.size)
                (parentFragment as? SuggestFragment)?.run {
                    isUpdateRouteByBackPress = true
                    isSelectedPlaceChanged = true
                    updateSuggestRouteList(viewState.suggestList.toList())
                }
            }
            is RouteDetailViewState.OnFetchNodeLineDataSuccess -> {
                handleLoadLocalData()
            }
        }
    }

    private fun handleUpdateUserFeatureView(viewState: RouteDetailViewState.OnSuggestListUpdated) {
        binding?.run {
            viewModel.run {
                mUserFeature?.run {
                    tvEstimatedTime.setTextColor(
                        if (viewState.estimatedTime > availableTime)
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.maximumRed
                            )
                        else
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

                    if (!viewState.isUpdateViewOnly) {
                        (parentFragment as? SuggestFragment)?.run {
                            isUpdateRouteByBackPress = true
                            isSelectedPlaceChanged = true
                            isSuggestRouteChanged = true
                            updateSuggestRouteList(viewState.suggestList.toList())
                        }
                    }
                }
            }
        }
    }

    private fun showDialogReachOutTime(viewState: RouteDetailViewState.OnSuggestListUpdated) {
        ConfirmMessageDialog(
            title = "Message",
            message = "The suggest route has exceeded your available time. Are you sure to continue?"
        ).apply {
            positiveAction = {
                handleUpdateUserFeatureView(viewState)
            }
            negativeAction = {
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
        viewModel.run {
            fetchAllNodesAndLines()
        }
    }

    private fun handleLoadLocalData() {
        (parentFragment as? SuggestFragment)?.run {
            viewModel.run {
                val currentLocation = getCurrentLocation()
                if (currentLocation != null) {
                    viewModelScope.launch {
                        mUserFeature = getUserFeature()
                        if (mUserFeature != null) {
                            withContext(Dispatchers.IO) {
                                setCurrentUserNode(currentLocation)
                            }
                            updateFeatureView()
                            getSaveSuggestList().let { saveSuggestList ->
                                if (saveSuggestList.isNotEmpty()) {
                                    updateSuggestList(saveSuggestList)
                                } else {
                                    suggestRoute()
                                }
                            }
                        } else {
                            showFeatureQuestionFragment(false)
                        }
                    }
                } else {
                    showErrorPopup("Error when retrieving user's location. Please try again!")
                    parentActivity?.invokeBackPress()
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
                ConfirmMessageDialog(
                    title = "Regenerate route",
                    message = "You will lose your current route data. Do you want to proceed?"
                ).apply {
                    positiveAction = {
                        (this@RouteDetailFragment.parentFragment as? SuggestFragment)?.showFeatureQuestionFragment(
                            false
                        )
                    }
                }.show(childFragmentManager, null)
            }

            btnAddPlace.setOnClickListener {
                currentActionSelectPlace = ADD_SELECTED_PLACE
                handleSelectedPlaceFromMap()
            }

            btnSort.setOnClickListener {
                ConfirmMessageDialog(
                    title = "Auto re-arrange",
                    message = "Your route will be re-arranged to our best recommend order, exclude VISITED places"
                ).apply {
                    positiveAction = {
                        viewModel.handleUpdateSuggestNode()
                    }
                }.show(childFragmentManager, null)
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
                                    currentActionSelectPlace = REPLACE_SELECTED_PLACE
                                    saveReplaceIndex = position
                                    handleSelectedPlaceFromMap()
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

    private fun handleSelectedPlaceFromMap() {
        (parentFragment as SuggestFragment).run {
            showSuggestMapFragment()
            getSuggestMapFragment()?.handleDisplaySelectableNode(viewModel.getAddablePlace())
        }
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

    fun handleSelectingPlaceResult(placeId: Int) {
        when (currentActionSelectPlace) {
            ADD_SELECTED_PLACE -> viewModel.handleAddRouteItem(placeId)
            REPLACE_SELECTED_PLACE -> {
                saveReplaceIndex?.let { replaceIndex ->
                    viewModel.handleReplaceItem(placeId, replaceIndex)
                    saveReplaceIndex = null
                }
            }
        }
        currentActionSelectPlace = ""
    }
}