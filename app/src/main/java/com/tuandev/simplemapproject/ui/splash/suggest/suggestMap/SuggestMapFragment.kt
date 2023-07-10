package com.tuandev.simplemapproject.ui.splash.suggest.suggestMap

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import androidx.core.widget.doAfterTextChanged
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
import com.tuandev.simplemapproject.util.Constants
import com.tuandev.simplemapproject.widget.MessageDialog
import com.tuandev.simplemapproject.widget.placeInfoDialog.PlaceInfoDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.math.PI

@AndroidEntryPoint
class SuggestMapFragment :
    BaseFragment<FragmentSuggestMapBinding, SuggestMapViewModel, ViewState>(
        FragmentSuggestMapBinding::inflate
    ), SensorEventListener {

    companion object {
        const val SUGGEST_ROUTE = "suggest_route"
        const val SELECT_PLACE = "select_place"
        const val GUILD_PLACE = "guild_place"

        fun newInstance() = SuggestMapFragment()
    }

    private var mCurrentLocation: Location? = null
    private var isArrivedDestination: Boolean = false
    private var mapFragment: BaseMapFragment? = null
    private var placeInfoBottomDialog: PlaceInfoDialog? = null
    private var selectedServicePlaceId: Int? = null
    private var currentViewMode: String = SUGGEST_ROUTE
    private var isInMapBound: Boolean = false
    override val viewModel: SuggestMapViewModel by viewModels()
    override val viewStateObserver: (viewState: ViewState) -> Unit = {}
    private var sensorManager: SensorManager? = null
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)


    override fun initView() {
        mapFragment = BaseMapFragment.newInstance(BaseMapFragment.Companion.MapMode.SUGGEST_ROUTE)
        mapFragment?.let {
            openFragment(
                containerId = getContainerId(),
                fragment = it
            )
        }
        sensorManager = parentActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private fun getContainerId() = R.id.container_suggest_route

    override fun initListener() {

        binding?.run {

            btnBackToSuggestMap.setOnClickListener {
                updateDestinationView(SUGGEST_ROUTE)
                drawSelectedGuildPath()
            }

            tvDestName.setOnClickListener {
                parentActivity?.checkLocationPermission {
                    if (isInMapBound) {
                        (parentFragment as? SuggestFragment)?.showRouteDetailFragment()
                    } else {
                        viewModel.showMessagePopup("Make sure you are in Asia Park area to continue")
                    }
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

            edtSearch.doAfterTextChanged {
                mapFragment?.showPlaceByFilter(it?.toString() ?: "")
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
                            openPlaceInfoBottomDialog(place.id)
                        }
                    }
                }
            }

            (parentFragment as? SuggestFragment)?.run {
                invokeSuggestRouteUpdate = {
                    viewModel.viewModelScope.launch {
                        val updateRouteJob = launch {
                            if (isSuggestRouteChanged) {
                                edtSearch.setText("")
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
                    checkIfInMapBound()

                    when (currentViewMode) {
                        SUGGEST_ROUTE -> {
                            drawSelectedGuildPath()
                        }
                        GUILD_PLACE -> {
                            handleDrawPathToServicePlace()
                        }
                    }
                }
            }
        }
    }

    private fun checkIfInMapBound() {
        isInMapBound = mCurrentLocation?.run {
            Constants.AsiaParkMap.bound.contains(LatLng(latitude, longitude))
        } ?: false
        binding?.rlNotInBound?.showIf(!isInMapBound)
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
        val actionItem =
            ActionItem(if (currentViewMode != SELECT_PLACE) "Set to destination" else "Select") {
                if (currentViewMode == SELECT_PLACE) {
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
                updateDestinationView(SUGGEST_ROUTE)
            } ?: run {
                selectedServicePlaceId = placeId
                handleDrawPathToServicePlace()
            }
        }
    }

    private fun handleDrawPathToServicePlace() {
        selectedServicePlaceId?.let { placeId ->
            binding?.run {
                tvGuildServicePlace.text =
                    "You are heading to ${viewModel.getPlaceById(placeId)?.name}"
                updateDestinationView(GUILD_PLACE)
            }
            (parentFragment as? SuggestFragment)?.run {
                getCurrentLocation()?.let { currentLocation ->
                    selectedServicePlaceId?.let { }
                    mapFragment?.drawSelectedGuildPath(placeId, currentLocation)
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
                    mapFragment?.drawSelectedGuildPath(selectedPlace.place.id, currentLocation)
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
        updateDestinationView(SELECT_PLACE)
        mapFragment?.handleDisplaySelectableNode(listPlace)
    }

    fun disableSelectingPlace() {
        updateDestinationView(SUGGEST_ROUTE)
        mapFragment?.clearDisplayedSelectableNode()
    }

    private fun updateDestinationView(state: String) {
        binding?.run {
            currentViewMode = state
            llController.showIf(state == SUGGEST_ROUTE)
            llSelectPlace.showIf(state == SELECT_PLACE)
            llGuildServicePlace.showIf(state == GUILD_PLACE)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }
        updateOrientationAngles()
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    private fun updateOrientationAngles() {
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        val degree = orientationAngles[0] * (180 / PI).toFloat()
        mapFragment?.updateCurrentBearing(degree.toFloat())
    }

    override fun onResume() {
        super.onResume()
        sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager?.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager?.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }
}