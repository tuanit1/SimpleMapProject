package com.tuandev.simplemapproject.base.map

import androidx.fragment.app.viewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.base.BaseFragment
import com.tuandev.simplemapproject.databinding.FragmentBaseMapBinding
import com.tuandev.simplemapproject.extension.addFragment
import com.tuandev.simplemapproject.util.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BaseMapFragment :
    BaseFragment<FragmentBaseMapBinding, BaseMapViewModel, BaseMapViewState>(FragmentBaseMapBinding::inflate) {

    companion object {
        object TouchEvent {
            const val DRAW_MARKER = "draw_marker"
            const val DRAW_LINE_STEP_1 = "draw_line_step_1"
            const val DRAW_LINE_STEP_2 = "draw_line_step_2"
            const val OFF = "off"
        }

        fun newInstance() = BaseMapFragment()
    }

    private var supportMapFragment: SupportMapFragment? = null
    private var mMap: GoogleMap? = null
    var onMarkerClick: (Marker) -> Unit = {}
    var onMarkerDrawn: (Marker) -> Unit = {}
    var onLineDrawn: (Polyline) -> Unit = {}
    private var saveNodeOne: LatLng? = null

    override val viewModel: BaseMapViewModel by viewModels()

    override val viewStateObserver: (BaseMapViewState) -> Unit = { viewState ->
        when (viewState) {
            else -> {}
        }
    }

    override fun initView() {
        initMap()
        handleMap()
    }

    private fun initMap() {

        setCurrentTouchEvent(TouchEvent.OFF)

        supportMapFragment = SupportMapFragment.newInstance(
            GoogleMapOptions()
                .mapType(GoogleMap.MAP_TYPE_NORMAL)
                .rotateGesturesEnabled(false)
                .compassEnabled(false)
                .minZoomPreference(16f)
        )

        supportMapFragment?.let { fragment ->
            addFragment(
                containerId = getContainerId(),
                fragment = fragment,
                addToBackStack = true
            )
        }
    }

    private fun getContainerId() = R.id.fragment_map_container

    private fun handleMap() {
        supportMapFragment?.getMapAsync { map ->
            mMap = map
            setMapStyle()
            setMapListener()
            drawBorderLine()
        }
    }

    private fun setMapListener() {
        mMap?.run {
            setOnMarkerClickListener {
                onMarkerClick(it)

                when (viewModel.currentTouchEvent) {
                    TouchEvent.DRAW_LINE_STEP_1 -> {
                        saveNodeOne = it.position
                        setCurrentTouchEvent(TouchEvent.DRAW_LINE_STEP_2)
                    }
                    TouchEvent.DRAW_LINE_STEP_2 -> {
                        saveNodeOne?.let { node1 ->
                            drawLine(node1, it.position)
                            setCurrentTouchEvent(TouchEvent.DRAW_LINE_STEP_1)
                        }
                    }
                }

                return@setOnMarkerClickListener true
            }

            setOnMapClickListener {
                TouchEvent.run {
                    when (viewModel.currentTouchEvent) {
                        DRAW_MARKER -> {
                            drawMarker(it)
                        }
                    }
                }

            }
        }
    }

    private fun setMapStyle() {
        mMap?.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                requireContext(),
                R.raw.style_json
            )
        )
    }

    private fun drawBorderLine() {
        mMap?.run {
            Constants.AsiaParkMap.run {
                setLatLngBoundsForCameraTarget(bound)
                val rectOptions = PolygonOptions().add(nwLatLng, neLatLng, seLatLng, swLatLng)
                addPolygon(rectOptions)

                moveCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.builder()
                            .target(swLatLng)
                            .bearing(-7f)
                            .build()
                    )
                )
            }
        }
    }

    private fun drawMarker(latLng: LatLng) {
        mMap?.addMarker(
            MarkerOptions()
                .position(latLng)
        )?.let { onMarkerDrawn(it) }
    }

    private fun drawLine(node1: LatLng, node2: LatLng) {
        val polylineOptions = PolylineOptions()
            .add(node1)
            .add(node2)

        mMap?.addPolyline(polylineOptions)?.let { onLineDrawn(it) }
    }

    fun startDrawLine() {
        setCurrentTouchEvent(TouchEvent.DRAW_LINE_STEP_1)
    }

    fun setCurrentTouchEvent(event: String) {
        viewModel.currentTouchEvent = event
    }


}