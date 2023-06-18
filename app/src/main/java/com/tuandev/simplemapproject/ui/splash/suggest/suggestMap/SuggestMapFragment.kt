package com.tuandev.simplemapproject.ui.splash.suggest.suggestMap

import android.location.Location
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.base.BaseFragment
import com.tuandev.simplemapproject.base.ViewState
import com.tuandev.simplemapproject.base.map.BaseMapFragment
import com.tuandev.simplemapproject.data.models.ActionItem
import com.tuandev.simplemapproject.data.models.Place
import com.tuandev.simplemapproject.data.models.RouteItem
import com.tuandev.simplemapproject.databinding.FragmentSuggestMapBinding
import com.tuandev.simplemapproject.extension.*
import com.tuandev.simplemapproject.ui.splash.suggest.SuggestFragment
import com.tuandev.simplemapproject.widget.MessageDialog
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

    private var mCurrentLocation: Location? = null
    private var isArrivedDestination: Boolean = false
    private var mapFragment: BaseMapFragment? = null
    private var placeInfoBottomDialog: PlaceInfoDialog? = null
    private var isSelectingPlace: Boolean = false
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
                    handleSelectNextPLace()
                }
            }

            ivPrevPlace.setOnClickListener {
                (parentFragment as? SuggestFragment)?.run {
                    handleSelectPreviousPLace()
                }
            }

            btnBackToRouteDetail.setOnClickListener {
                (parentFragment as? SuggestFragment)?.run {
                    disableSelectingPlace()
                    showRouteDetailFragment()
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
                        (this@SuggestMapFragment.parentFragment as? SuggestFragment)?.run {
                            if (!getSaveSuggestList().any { it.place.id == place.id } || !isSelectingPlace) {
                                openPlaceInfoBottomDialog(place.id)
                            }
                        }
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
                                isArrivedDestination = true
                                isSelectedPlaceChanged = false
                                checkIfArrivedDestination()
                                drawSelectedGuildPath()
                            }
                        }

                        joinAll(updateRouteJob, updateSelectedJob)
                        updateRouteDestinationView()

                    }
                }

                onLocationUpdate = { location ->
                    mCurrentLocation = location
                    mapFragment?.updateCurrentLocation(location)
                    checkIfArrivedDestination()
                    drawSelectedGuildPath()
                }
            }
        }
    }

    private fun checkIfArrivedDestination() {
        mCurrentLocation?.let { location ->
            (parentFragment as? SuggestFragment)?.getSaveSuggestList()
                ?.find { it.itemState == RouteItem.SELECTED }?.place?.let { place ->
                    mapFragment?.run {
                        getNodeByPlaceId(place.id)?.let { destNode ->
                            getDistance(
                                p1 = LatLng(destNode.latitude, destNode.longitude),
                                p2 = LatLng(location.latitude, location.longitude)
                            )?.let { distanceToDest ->
                                log("About ${distanceToDest}m to ${getPlaceName(place)}")
                                if (distanceToDest <= 8f) {
                                    isArrivedDestination = false
                                    MessageDialog(
                                        title = "Message",
                                        message = "You have arrived ${getPlaceName(place)}"
                                    ).show(childFragmentManager, null)
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun getPlaceName(place: Place?) = place?.game?.name ?: place?.name

    private fun openPlaceInfoBottomDialog(placeId: Int) {
        val actionItem = ActionItem(if (!isSelectingPlace) "Set to destination" else "Select") {
            if (isSelectingPlace) {
                handleSelectingPlaceResult(placeId)
            } else {
                handleUpdateSelectedPlaceResult(placeId)
            }
        }

        placeInfoBottomDialog?.run {
            updatePlace(placeId)
            updateActionItem(actionItem)
            dialog?.show()
        } ?: run {
            placeInfoBottomDialog = PlaceInfoDialog.newInstance(placeId, actionItem)
            placeInfoBottomDialog?.show(childFragmentManager, null)
        }
    }

    private fun handleUpdateSelectedPlaceResult(placeId: Int) {
        (parentFragment as? SuggestFragment)?.run {
            getSaveSuggestList().find { it.place.id == placeId }?.let { routeItem ->
                if (routeItem.itemState != RouteItem.SELECTED) {
                    context?.showToast(
                        "Your destination has been updated to ${
                            getPlaceName(
                                routeItem.place
                            )
                        }"
                    )
                    handleSelectedPositionUpdate(routeItem.itemIndex - 1)
                } else {
                    context?.showToast("This is already your destination")
                }
            }
        }
    }

    private fun handleSelectingPlaceResult(placeId: Int) {
        (parentFragment as? SuggestFragment)?.run {
            disableSelectingPlace()
            showRouteDetailFragment()
            getRouteDetailFragment()?.handleSelectingPlaceResult(placeId)
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

    fun handleDisplaySelectableNode(listPlace: List<Place>) {
        isSelectingPlace = true
        binding?.run {
            llController.gone()
            llSelectPlace.show()
        }
        mapFragment?.handleDisplaySelectableNode(listPlace)
    }

    fun disableSelectingPlace() {
        isSelectingPlace = false
        binding?.run {
            llController.show()
            llSelectPlace.gone()
        }
        mapFragment?.clearDisplayedSelectableNode()
    }
}