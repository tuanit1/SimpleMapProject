package com.tuandev.simplemapproject.base.map

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
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

    override val viewStateObserver: (BaseMapViewState) -> Unit = { viewState ->
        when (viewState) {
            is BaseMapViewState.AddNodeSuccess -> {
                onNodeAdded(viewState.newNode)
            }

            is BaseMapViewState.AddLineSuccess -> {
                onLineAdded(viewState.newLine)
            }

            is BaseMapViewState.GetNodesSuccess -> {
                loadAllNodeToMap()
            }

            is BaseMapViewState.GetLinesSuccess -> {
                loadAllLineToMap()
            }

            is BaseMapViewState.RemoveNodeSuccess -> {}
        }
    }

    override fun initView() {
        initMap()
        handleMap()
    }

    override fun initListener() {
        viewModel.currentTouchEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                TouchEvent.OFF -> {
                    lastSelectedMarker?.setIcon((getNodeImage()))
                }
            }
        }
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

    private fun fetchData() {
        viewModel.getAllNodesAndLines()
    }

    private fun getContainerId() = R.id.fragment_map_container

    private fun handleMap() {
        supportMapFragment?.getMapAsync { map ->
            mMap = map
            setMapStyle()
            setMapListener()
            drawBorderLine()
            fetchData()
        }
    }

    private fun setMapListener() {
        mMap?.run {

            setOnPolylineClickListener { polyline ->
                if (viewModel.currentTouchEvent.value == TouchEvent.OFF) {
                    onPolylineClick(polyline)
                }
            }

            setOnMarkerClickListener { marker ->
                log("${marker.position}")
                when (viewModel.currentTouchEvent.value) {
                    TouchEvent.DRAW_LINE_STEP_1 -> {
                        handleDrawLineStep1(marker)
                    }

                    TouchEvent.DRAW_LINE_STEP_2 -> {
                        handleDrawLineStep2(marker)
                    }

                    TouchEvent.OFF -> {
                        onMarkerClick(marker)
                    }
                }

                return@setOnMarkerClickListener true
            }

            setOnMapClickListener { latLng ->
                TouchEvent.run {
                    when (viewModel.currentTouchEvent.value) {
                        DRAW_MARKER -> {
                            drawNodeMarker(latLng)?.let { marker ->
                                viewModel.addNode(marker)
                            }
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
                mMap?.addPolygon(
                    PolygonOptions()
                        .addAll(borderList)
                        .strokePattern(listOf(Dot()))
                        .strokeColor(ContextCompat.getColor(requireContext(), R.color.guidePathColor))
                )

                moveCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.builder()
                            .target(bound.center)
                            .bearing(-7f)
                            .build()
                    )
                )
            }
        }
    }

    private fun drawMarker(
        latLng: LatLng,
        nodeId: String? = null,
        bitmapDescriptor: BitmapDescriptor
    ) =
        mMap?.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("")
                .anchor(0.5f, 0.5f)
                .icon(bitmapDescriptor)
        )?.apply {
            tag = nodeId
        }

    private fun drawNodeMarker(latLng: LatLng, nodeId: String? = null) =
        drawMarker(latLng, nodeId, getNodeImage())

    private fun drawBorderMarker(latLng: LatLng, nodeId: String? = null) =
        drawMarker(latLng, nodeId, getBorderImage())


    private fun drawLine(node1: Marker, node2: Marker, id: String? = null): Polyline? =
        mMap?.addPolyline(
            PolylineOptions()
                .color(ContextCompat.getColor(requireContext(), R.color.guidePathColor))
                .add(node1.position, node2.position)
                .clickable(true)
                .width(12f)
        )?.apply {
            tag = id
        }

    fun startDrawLine() {
        setCurrentTouchEvent(TouchEvent.DRAW_LINE_STEP_1)
    }

    fun setCurrentTouchEvent(event: String) {
        viewModel.currentTouchEvent.value = event
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

    private fun getBorderImage() =
        BitmapDescriptorFactory.fromBitmap(
            resizeMapIcons(
                resId = R.drawable.ic_cross_cancel,
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
        lastSelectedMarker = marker
        marker.setIcon(getSelectedNodeImage())
        setCurrentTouchEvent(TouchEvent.DRAW_LINE_STEP_2)
    }

    private fun handleDrawLineStep2(marker: Marker) {
        lastSelectedMarker?.let { lastMarker ->

            val firstNodeId = lastMarker.tag?.toString()
            val secondNodeId = marker.tag?.toString()

            if (lastSelectedMarker != marker) {
                if (viewModel.checkIfLineNotExist(firstNodeId, secondNodeId)) {
                    if (firstNodeId != null && secondNodeId != null) {
                        drawLine(lastMarker, marker)?.let { polyline ->
                            viewModel.addLine(
                                firstNodeId = firstNodeId,
                                secondNodeId = secondNodeId,
                                polyline = polyline
                            )
                        }
                    }

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

    private fun loadAllNodeToMap() {
        viewModel.listNode.forEach { node ->
            node.run {
                marker = drawNodeMarker(latLng = LatLng(latitude, longitude), nodeId = id)
            }
        }
    }

    private fun loadAllLineToMap() {
        viewModel.listLine.forEach { line ->
            line.run {
                viewModel.getNodeById(line.firstNodeId)?.marker?.let { firstMarker ->
                    viewModel.getNodeById(line.secondNodeId)?.marker?.let { secondMarker ->
                        polyline = drawLine(firstMarker, secondMarker, id)
                    }
                }
            }
        }
    }

    fun removeNode(nodeId: String) = viewModel.removeNode(nodeId)
    fun removeLine(lineId: String) = viewModel.removeLine(lineId)

}