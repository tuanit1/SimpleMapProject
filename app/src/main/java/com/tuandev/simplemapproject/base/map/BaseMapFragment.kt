package com.tuandev.simplemapproject.base.map

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
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
    private var lastSelectedMarker: Marker? = null

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

                when (viewModel.currentTouchEvent) {
                    TouchEvent.DRAW_LINE_STEP_1 -> {
                        it.setIcon(getSelectedNodeImage())
                        setCurrentTouchEvent(TouchEvent.DRAW_LINE_STEP_2)
                    }

                    TouchEvent.DRAW_LINE_STEP_2 -> {
                        lastSelectedMarker?.let { lastMarker ->
                            if (lastSelectedMarker != it) {
                                drawLine(lastMarker.position, it.position)
                            }
                            lastMarker.setIcon((getNodeImage()))
                            setCurrentTouchEvent(TouchEvent.DRAW_LINE_STEP_1)
                        }
                    }

                    TouchEvent.OFF -> {
                        onMarkerClick(it)
                    }
                }

                lastSelectedMarker = it

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
                val rectOptions = PolygonOptions()
                    .add(nwLatLng, neLatLng, seLatLng, swLatLng)
                    .strokePattern(listOf(Dot()))
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
                .title("")
                .anchor(0.5f, 0.5f)
                .icon(getNodeImage())
        )?.let { onMarkerDrawn(it) }
    }

    private fun drawLine(node1: LatLng, node2: LatLng) {
        val polylineOptions = PolylineOptions()
            .color(ContextCompat.getColor(requireContext(), R.color.guidePathColor))
            .add(node1, node2)
            .width(12f)

        mMap?.addPolyline(polylineOptions)?.let { onLineDrawn(it) }
    }

    fun startDrawLine() {
        setCurrentTouchEvent(TouchEvent.DRAW_LINE_STEP_1)
    }

    fun setCurrentTouchEvent(event: String) {
        viewModel.currentTouchEvent = event
    }

    private fun resizeMapIcons(
        resId: Int,
        height: Int,
        width: Int,
    ): Bitmap = BitmapFactory.decodeResource(resources, resId).let {
        Bitmap.createScaledBitmap(it, height, width, false)
    }

    private fun getNodeImage() =
        BitmapDescriptorFactory.fromBitmap(
            resizeMapIcons(
                resId = R.drawable.ic_node,
                height = 70,
                width = 70
            )
        )

    private fun getSelectedNodeImage() =
        BitmapDescriptorFactory.fromBitmap(
            resizeMapIcons(
                resId = R.drawable.ic_node_selected,
                height = 90,
                width = 90
            )
        )

}