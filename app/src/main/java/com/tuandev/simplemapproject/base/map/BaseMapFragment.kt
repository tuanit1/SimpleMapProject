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
import com.tuandev.simplemapproject.data.models.Line
import com.tuandev.simplemapproject.data.models.Node
import com.tuandev.simplemapproject.databinding.FragmentBaseMapBinding
import com.tuandev.simplemapproject.extension.addFragment
import com.tuandev.simplemapproject.extension.log
import com.tuandev.simplemapproject.extension.showToast
import com.tuandev.simplemapproject.extension.toIntOrNull
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
    var onPolylineClick: (Polyline) -> Unit = {}
    var onNodeAdded: (Node) -> Unit = {}
    var onLineAdded: (Line) -> Unit = {}
    private var lastSelectedMarker: Marker? = null

    override val viewModel: BaseMapViewModel by viewModels()

    override val viewStateObserver: (BaseMapViewState) -> Unit = {
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

            setOnPolylineClickListener {
                if(viewModel.currentTouchEvent == TouchEvent.OFF){
                    onPolylineClick(it)
                }
            }

            setOnMarkerClickListener {
                when (viewModel.currentTouchEvent) {
                    TouchEvent.DRAW_LINE_STEP_1 -> {
                        handleDrawLineStep1(it)
                    }

                    TouchEvent.DRAW_LINE_STEP_2 -> {
                        handleDrawLineStep2(it)
                    }

                    TouchEvent.OFF -> {
                        lastSelectedMarker?.setIcon((getNodeImage()))
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
        )?.let {
            onNodeAdded(viewModel.addNode(it))
        }
    }

    private fun drawLine(node1: Marker, node2: Marker) {
        mMap?.addPolyline(
            PolylineOptions()
                .color(ContextCompat.getColor(requireContext(), R.color.guidePathColor))
                .add(node1.position, node2.position)
                .clickable(true)
                .width(12f)
        )?.let {
            onLineAdded(
                viewModel.addLine(
                    firstNodeId = node1.tag?.toIntOrNull() ?: -1,
                    secondNodeId = node2.tag?.toIntOrNull() ?: -1,
                    polyline = it
                )
            )
        }
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

    fun toggleSatellite() {
        mMap?.run {
            mapType = if (mapType == GoogleMap.MAP_TYPE_NORMAL) {
                GoogleMap.MAP_TYPE_HYBRID
            } else {
                GoogleMap.MAP_TYPE_NORMAL
            }
        }
    }

    private fun handleDrawLineStep1(marker: Marker) {
        marker.setIcon(getSelectedNodeImage())
        setCurrentTouchEvent(TouchEvent.DRAW_LINE_STEP_2)
    }

    private fun handleDrawLineStep2(marker: Marker) {
        lastSelectedMarker?.let { lastMarker ->

            val firstNodeId = lastMarker.tag?.toIntOrNull() ?: -1
            val secondNodeId = marker.tag?.toIntOrNull() ?: -1

            if (lastSelectedMarker != marker) {
                if (viewModel.checkIfLineNotExist(firstNodeId, secondNodeId)) {
                    drawLine(lastMarker, marker)
                    lastMarker.setIcon((getNodeImage()))
                    setCurrentTouchEvent(TouchEvent.DRAW_LINE_STEP_1)
                } else {
                    context?.showToast(Constants.ErrorMessage.CONFLICT_LINE)
                }
            } else {
                lastMarker.setIcon((getNodeImage()))
                setCurrentTouchEvent(TouchEvent.DRAW_LINE_STEP_1)
            }

        }
    }

    fun removeNode(nodeId: Int) = viewModel.removeNode(nodeId)
    fun removeLine(lineId: Int) = viewModel.removeLine(lineId)

}