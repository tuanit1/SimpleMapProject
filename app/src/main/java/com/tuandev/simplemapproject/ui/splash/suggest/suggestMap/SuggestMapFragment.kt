package com.tuandev.simplemapproject.ui.splash.suggest.suggestMap

import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.base.BaseFragment
import com.tuandev.simplemapproject.base.ViewState
import com.tuandev.simplemapproject.base.map.BaseMapFragment
import com.tuandev.simplemapproject.data.models.ActionItem
import com.tuandev.simplemapproject.data.models.RouteItem
import com.tuandev.simplemapproject.databinding.FragmentSuggestMapBinding
import com.tuandev.simplemapproject.extension.log
import com.tuandev.simplemapproject.extension.openFragment
import com.tuandev.simplemapproject.extension.show
import com.tuandev.simplemapproject.extension.showIf
import com.tuandev.simplemapproject.ui.splash.suggest.SuggestFragment
import com.tuandev.simplemapproject.widget.placeInfoDialog.PlaceInfoDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SuggestMapFragment :
    BaseFragment<FragmentSuggestMapBinding, SuggestMapViewModel, ViewState>(
        FragmentSuggestMapBinding::inflate
    ) {

    companion object {
        fun newInstance() = SuggestMapFragment()
    }

    private var mapFragment: BaseMapFragment? = null
    private var placeInfoBottomDialog: PlaceInfoDialog? = null
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
            tvDestName.setOnClickListener {
                parentActivity?.checkLocationPermission {
                    (parentFragment as? SuggestFragment)?.showRouteDetailFragment()
                }
            }

            ivNextPlace.setOnClickListener {
                (parentFragment as? SuggestFragment)?.run {
                    handleSelectedPositionUpdate(true)
                }
            }

            ivPrevPlace.setOnClickListener {
                (parentFragment as? SuggestFragment)?.run {
                    handleSelectedPositionUpdate(false)
                }
            }

            parentActivity?.onActivityBackPressListener = {
                llDest.show()
            }

            mapFragment?.run {
                onNodesLinesLoaded = {
                    handleSuggestRouteUpdated()
                    drawSelectedGuildPath()
                    updateRouteDestinationView()
                }

                onMarkerClick = { node ->
                    viewModel.getPlaceById(node.placeId)?.let { place ->
                        openPlaceInfoBottomDialog(place.id)
                    }
                }
            }

            (parentFragment as? SuggestFragment)?.run {
                invokeSuggestRouteUpdate = {
                    viewModel.viewModelScope.launch {
                        val updateRouteJob = launch {
                            if (isSuggestRouteChanged) {
                                isSuggestRouteChanged = false
                                handleSuggestRouteUpdated()
                            }
                        }

                        val updateSelectedJob = launch {
                            if (isSelectedPlaceChanged) {
                                isSelectedPlaceChanged = false
                                drawSelectedGuildPath()
                            }
                        }

                        joinAll(updateRouteJob, updateSelectedJob)
                        updateRouteDestinationView()

                    }
                }

                onLocationUpdate = { location ->
                    mapFragment?.updateCurrentLocation(location)
                    drawSelectedGuildPath()
                }
            }
        }
    }

    private fun openPlaceInfoBottomDialog(placeId: Int) {
        placeInfoBottomDialog?.run {
            updatePlace(placeId)
            dialog?.show()
        } ?: run {

            val tempList = listOf(
                ActionItem("1") {
                    log("1")
                },
                ActionItem("2") {
                    log("2")
                },
                ActionItem("3") {
                    log("3")
                },
                ActionItem("4") {
                    log("4")
                }
            )

            placeInfoBottomDialog = PlaceInfoDialog.newInstance(placeId, tempList)
            placeInfoBottomDialog?.show(childFragmentManager, null)
        }
    }

    private fun drawSelectedGuildPath() {
        (parentFragment as? SuggestFragment)?.run {
            getSaveSuggestList().find { it.itemState == RouteItem.SELECTED }?.let { selectedPlace ->
                getCurrentLocation()?.let { currentLocation ->
                    mapFragment?.drawSelectedGuildPath(selectedPlace, currentLocation)
                }
            }
        }
    }

    private fun updateRouteDestinationView() {
        binding?.run {
            (parentFragment as? SuggestFragment)?.run {
                getSaveSuggestList().find { it.itemState == RouteItem.SELECTED }
                    ?.let { selectedPlace ->
                        tvDestName.text = getString(
                            R.string.place_name_with_index,
                            selectedPlace.itemIndex,
                            selectedPlace.place.run {
                                game?.name ?: name
                            })
                        log(tvDestName.text.toString())

                        ivNextPlace.showIf(selectedPlace.itemIndex != getSaveSuggestList().lastIndex + 1)
                        ivPrevPlace.showIf(selectedPlace.itemIndex != 1)
                    }
            }
        }
    }

    private fun handleSuggestRouteUpdated() {
        (parentFragment as? SuggestFragment)?.getSaveSuggestList()?.let { suggestRoute ->
            mapFragment?.handleSuggestRouteUpdated(suggestRoute)
        }
    }
}