package com.tuandev.simplemapproject.base.map

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.base.BaseFragment
import com.tuandev.simplemapproject.data.models.Line
import com.tuandev.simplemapproject.data.models.Node
import com.tuandev.simplemapproject.data.models.RouteItem
import com.tuandev.simplemapproject.databinding.FragmentBaseMapBinding
import com.tuandev.simplemapproject.extension.log
import com.tuandev.simplemapproject.extension.openFragment
import com.tuandev.simplemapproject.extension.showToast
import com.tuandev.simplemapproject.extension.toRoundedFloat
import com.tuandev.simplemapproject.util.AStarSearch
import com.tuandev.simplemapproject.util.Constants
import com.tuandev.simplemapproject.util.Event
import com.tuandev.simplemapproject.widget.EditNodeDialog
import com.tuandev.simplemapproject.widget.imagelistdialog.ImageListDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class BaseMapFragment :
    BaseFragment<FragmentBaseMapBinding, BaseMapViewModel, BaseMapViewState>(FragmentBaseMapBinding::inflate) {

    companion object {
        object TouchEvent {
            const val DRAW_MARKER = "draw_marker"
            const val DRAW_LINE_STEP_1 = "draw_line_step_1"
            const val DRAW_LINE_STEP_2 = "draw_line_step_2"
            const val FIND_ROUTE_STEP_1 = "find_route_step_1"
            const val FIND_ROUTE_STEP_2 = "find_route_step_2"
            const val OFF = "off"
        }

        object MapMode {
            const val TOOL = "tool"
            const val SUGGEST_ROUTE = "suggest_route"
            const val EXPLORE = "explore"
        }

        @JvmStatic
        fun newInstance(mapMode: String) = BaseMapFragment().apply {
            arguments = Bundle().apply {
                putString("mapMode", mapMode)
            }
        }
    }

    private var supportMapFragment: SupportMapFragment? = null
    private var mMap: GoogleMap? = null
    private var mapMode = ""
    var onMarkerClick: (Marker) -> Unit = {}
    var onPolylineClick: (Polyline) -> Unit = {}
    var onNodeAdded: (Node) -> Unit = {}
    var onLineAdded: (Line) -> Unit = {}
    var onNodesLinesLoaded: () -> Unit = {}
    private var lastSelectedMarker: Marker? = null
    private var aStarSearch: AStarSearch? = null
    private var currentGuildPath: Polyline? = null
    private var allSuggestPaths: MutableList<Polyline> = mutableListOf()
    private var allSuggestPlaces: MutableList<Marker> = mutableListOf()

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
                when (mapMode) {
                    MapMode.SUGGEST_ROUTE -> {}
                }
            }

            is BaseMapViewState.GetLinesSuccess -> {

                initAStarSearch()

                when (mapMode) {
                    MapMode.TOOL -> loadAllNodeAndLineToMap()
                    MapMode.SUGGEST_ROUTE -> onNodesLinesLoaded()
                }
            }

            is BaseMapViewState.ToggleLine -> {
                if (viewState.isVisible) {
                    loadAllLineToMap()
                } else {
                    removeLinesFromMap()
                }
            }

            is BaseMapViewState.NodePlaceUpdateSuccess -> {
                updateMarkerIcon(viewState.marker, viewState.nodeId)
            }
        }
    }

    private fun updateMarkerIcon(marker: Marker?, nodeId: String) {
        viewModel.run {
            getPlaceById(getNodeById(nodeId)?.placeId).let { place ->
                val image = if (place != null) {
                    if (place.game != null) {
                        getGameImage()
                    } else {
                        getPlaceImage()
                    }
                } else {
                    getNodeImage()
                }
                marker?.setIcon(image)
            }
        }
    }

    private fun initAStarSearch() {
        aStarSearch = AStarSearch(viewModel.listNode)
    }

    override fun initView() {
        initMap()
        handleMap()
    }

    override fun initListener() {
        viewModel.currentTouchEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                TouchEvent.OFF -> {
                    lastSelectedMarker?.let {
                        updateMarkerIcon(it, it.tag.toString())
                    }
                    currentGuildPath?.remove()
                    viewModel.updateLineViewState(isVisible = true)
                }
            }
        }
    }

    private fun initMap() {

        setCurrentTouchEvent(TouchEvent.OFF)

        mapMode = arguments?.getString("mapMode") ?: ""

        val mapType =
            if (mapMode == MapMode.SUGGEST_ROUTE) GoogleMap.MAP_TYPE_HYBRID else GoogleMap.MAP_TYPE_NORMAL

        supportMapFragment = SupportMapFragment.newInstance(
            GoogleMapOptions()
                .mapType(mapType)
                .rotateGesturesEnabled(false)
                .compassEnabled(false)
                .minZoomPreference(17f)
        )

        supportMapFragment?.let { fragment ->
            openFragment(
                containerId = getContainerId(),
                fragment = fragment
            )
        }
    }

    private fun fetchData() {
        when (mapMode) {
            MapMode.TOOL -> viewModel.fetchAllNodesAndLines()
            MapMode.EXPLORE -> {}
            MapMode.SUGGEST_ROUTE -> viewModel.fetchAllNodesAndLines()
        }
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
                when (viewModel.currentTouchEvent.value) {
                    TouchEvent.DRAW_LINE_STEP_1 -> {
                        handleDrawLineStep1(marker)
                    }

                    TouchEvent.DRAW_LINE_STEP_2 -> {
                        handleDrawLineStep2(marker)
                    }

                    TouchEvent.FIND_ROUTE_STEP_1 -> {
                        handleFindRouteStep1(marker)
                    }

                    TouchEvent.FIND_ROUTE_STEP_2 -> {
                        handleFindRouteStep2(marker)
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
                            viewModel.viewModelScope.launch {
                                drawNodeMarker(latLng, onDrawn = { marker ->
                                    viewModel.addNode(marker)
                                }).await()
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
                        .strokeColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.guidePathColor
                            )
                        )
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

    private suspend fun drawNodeMarker(
        latLng: LatLng,
        node: Node? = null,
        onDrawn: (Marker) -> Unit
    ) = coroutineScope {
        async(Dispatchers.IO) {
            val place = viewModel.getPlaceById(node?.placeId)
            val nodeImage = if (place != null) {
                if (place.game != null) {
                    getGameImage()
                } else {
                    getPlaceImage()
                }
            } else {
                getNodeImage()
            }
            withContext(Dispatchers.Main) {
                drawMarker(latLng, node?.id, nodeImage)?.let(onDrawn)
            }
        }
    }


    private fun drawLine(node1: Marker, node2: Marker, id: String? = null): Polyline? =
        mMap?.addPolyline(
            PolylineOptions()
                .color(ContextCompat.getColor(requireContext(), R.color.lineColor))
                .add(node1.position, node2.position)
                .clickable(true)
                .width(10f)
        )?.apply {
            tag = id
        }

    private fun handleDrawGuildPath(nodes: List<Node>): Polyline? {
        return mMap?.addPolyline(
            PolylineOptions()
                .color(ContextCompat.getColor(requireContext(), R.color.guidePathColor))
                .addAll(nodes.map { LatLng(it.latitude, it.longitude) })
                .jointType(JointType.ROUND)
                .width(20f)
        )
    }

    fun startDrawLine() {
        setCurrentTouchEvent(TouchEvent.DRAW_LINE_STEP_1)
    }

    fun startFindRoute() {
        setCurrentTouchEvent(TouchEvent.FIND_ROUTE_STEP_1)
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
                height = 60,
                width = 60
            )
        )

    private fun getPlaceImage() =
        BitmapDescriptorFactory.fromBitmap(
            resizeMapIcons(
                resId = R.drawable.ic_place_node,
                height = 70,
                width = 70
            )
        )

    private fun getGameImage() =
        BitmapDescriptorFactory.fromBitmap(
            resizeMapIcons(
                resId = R.drawable.ic_game_node,
                height = 90,
                width = 90
            )
        )

    private fun getSelectedNodeImage() =
        BitmapDescriptorFactory.fromBitmap(
            resizeMapIcons(
                resId = R.drawable.ic_node_selected,
                height = 70,
                width = 70
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
                                polyline = polyline,
                                distance = getDistance(lastMarker.position, marker.position)
                            )
                        }
                    }

                    lastMarker.setIcon((getNodeImage()))
                    setCurrentTouchEvent(TouchEvent.DRAW_LINE_STEP_1)
                } else {
                    context?.run {
                        showToast(getString(R.string.err_conflict_line))
                    }
                }
            } else {
                lastMarker.setIcon((getNodeImage()))
                setCurrentTouchEvent(TouchEvent.DRAW_LINE_STEP_1)
            }
        }
    }

    private fun handleFindRouteStep1(marker: Marker) {
        lastSelectedMarker = marker
        currentGuildPath?.remove()
        marker.setIcon(getSelectedNodeImage())
        viewModel.updateLineViewState(isVisible = true)
        setCurrentTouchEvent(TouchEvent.FIND_ROUTE_STEP_2)
    }

    private fun handleFindRouteStep2(marker: Marker) {
        lastSelectedMarker?.let { lastMarker ->
            val start = viewModel.getNodeById(lastMarker.tag?.toString())
            val goal = viewModel.getNodeById(marker.tag?.toString())

            if (lastSelectedMarker != marker && start != null && goal != null) {
                aStarSearch?.findBestPath(start, goal) { nodes, _ ->
                    currentGuildPath?.remove()
                    currentGuildPath = handleDrawGuildPath(nodes)
                    viewModel.updateLineViewState(isVisible = false)
                }
            }

            updateMarkerIcon(lastMarker, lastMarker.tag.toString())
            setCurrentTouchEvent(TouchEvent.FIND_ROUTE_STEP_1)
        }
    }

    private fun loadAllNodeAndLineToMap() {
        viewModel.run {
            viewModelScope.launch {
                val deferredList = mutableListOf<Deferred<*>>()
                listNode.forEach { node ->
                    deferredList.add(
                        node.run {
                            drawNodeMarker(
                                latLng = LatLng(latitude, longitude),
                                node = node,
                                onDrawn = {
                                    marker = it
                                }
                            )
                        }

                    )
                }
                awaitAll(deferreds = deferredList.toTypedArray())
                loadAllLineToMap()
            }
        }
    }

    private fun loadAllLineToMap() {
        viewModel.listLine.forEach { line ->
            line.run {
                viewModel.getNodeById(line.firstNodeId)?.marker?.let { firstMarker ->
                    viewModel.getNodeById(line.secondNodeId)?.marker?.let { secondMarker ->
                        if (polyline == null) {
                            polyline = drawLine(firstMarker, secondMarker, id)
                        }
                    }
                }
            }
        }
    }

    private fun removeLinesFromMap() {
        viewModel.listLine.forEach { line ->
            line.run {
                line.removePolyline()
            }
        }
    }

    private fun getDistance(p1: LatLng, p2: LatLng): Float? {
        return try {
            val results = FloatArray(1)
            Location.distanceBetween(
                p1.latitude, p1.longitude, p2.latitude, p2.longitude, results
            )
            results.first().toRoundedFloat(2)
        } catch (e: Exception) {
            null
        }
    }

    fun handleUpdateNode(nodeId: String) {
        getNodeById(nodeId)?.let { node ->
            EditNodeDialog(node, viewModel.listNode).apply {
                onNodeUpdate = { placeId, onUpdateSuccess ->
                    viewModel.updateNodePlace(
                        nodeId = nodeId,
                        placeId = placeId,
                        onUpdateSuccess
                    )
                }
                onTakePhoto = { placeId ->
                    parentActivity?.handleTakePhoto { image ->
                        viewModel.uploadPlaceImage(image, placeId)
                    }
                }
                onFromGallery = { placeId ->
                    parentActivity?.handleGetPhotoFromGallery { image ->
                        viewModel.uploadPlaceImage(image, placeId)
                    }
                }
                onOpenImageList = {
                    node.placeId?.let { placeId ->
                        viewModel.getPlaceImages(placeId) { imageList ->
                            if (imageList.isNotEmpty()) {
                                ImageListDialog(imageList.toMutableList()).apply {
                                    Event.onDeleteImageListener = { imagePath, onSuccess ->
                                        viewModel.deletePlaceImage(imagePath, placeId, onSuccess)
                                    }
                                }.show(childFragmentManager, null)
                            } else {
                                context?.showToast("No image found")
                            }
                        }
                    }
                }
            }.show(childFragmentManager, null)
        }
    }

    private fun getNodeById(nodeId: String) = viewModel.getNodeById(nodeId)
    fun removeNode(nodeId: String) = viewModel.removeNode(nodeId)
    fun removeLine(lineId: String) = viewModel.removeLine(lineId)
    fun handleSuggestRouteUpdated(suggestRoute: List<RouteItem>) {
        removeCurrentSuggestViewData()
        handleDisplaySuggestPlaceMarker(suggestRoute)
        handleDisplayOverviewRoute(suggestRoute)
    }

    private fun handleDisplayOverviewRoute(suggestRoute: List<RouteItem>) {
        viewModel.run {
            viewModelScope.launch(Dispatchers.IO) {
                for (i in 0 until suggestRoute.size - 1) {
                    val start = getNodeByPlaceId(suggestRoute[i].place.id)
                    val goal = getNodeByPlaceId(suggestRoute[i + 1].place.id)

                    if (start != null && goal != null) {
                        aStarSearch?.findBestPath(start, goal) { nodes, _ ->
                            viewModelScope.launch(Dispatchers.Main) {
                                handleDrawGuildPath(nodes)?.let { polyline ->
                                    allSuggestPaths.add(polyline)
                                }
                            }
                        }
                    } else {
                        log("Error when draw line between two place ${suggestRoute[i].place.id} & ${suggestRoute[i + 1].place.id}")
                    }
                }
            }
        }
    }

    private fun handleDisplaySuggestPlaceMarker(suggestRoute: List<RouteItem>) {
        viewModel.run {
            viewModelScope.launch(Dispatchers.IO) {
                suggestRoute.forEach { routeItem ->
                    getNodeByPlaceId(routeItem.place.id)?.run {
                        routeItem.run {
                            withContext(Dispatchers.Main) {
                                if (place.game != null) {
                                    drawMarker(
                                        latLng = LatLng(latitude, longitude),
                                        nodeId = id,
                                        bitmapDescriptor = getGameImage()
                                    )?.let { allSuggestPlaces.add(it) }
                                } else {
                                    drawMarker(
                                        latLng = LatLng(
                                            latitude,
                                            longitude
                                        ),
                                        nodeId = id,
                                        bitmapDescriptor = getPlaceImageWithDrawable(
                                            res = place.serviceType?.imgRes
                                                ?: R.drawable.ic_place_node,
                                            size = 90
                                        )
                                    )?.let { allSuggestPlaces.add(it) }
                                }
                            }
                        }
                    }

                }
            }
        }

    }

    private fun getPlaceImageWithDrawable(
        res: Int,
        size: Int,
    ): BitmapDescriptor {
        return BitmapDescriptorFactory.fromBitmap(
            resizeMapIcons(
                resId = res,
                height = size,
                width = size
            )
        )
    }

    private fun removeCurrentSuggestViewData() {
        allSuggestPaths.run {
            forEach { it.remove() }
            clear()
        }

        allSuggestPlaces.run {
            forEach { it.remove() }
            clear()
        }
    }
}