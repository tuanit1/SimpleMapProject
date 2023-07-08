package com.tuandev.simplemapproject.base.map

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.base.BaseFragment
import com.tuandev.simplemapproject.data.models.Line
import com.tuandev.simplemapproject.data.models.Node
import com.tuandev.simplemapproject.data.models.Place
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
import com.tuandev.simplemapproject.widget.imageListDialog.ImageListDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class BaseMapFragment :
    BaseFragment<FragmentBaseMapBinding, BaseMapViewModel, BaseMapViewState>(FragmentBaseMapBinding::inflate) {

    companion object {
        object TouchState {
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
    var onMarkerClick: (Node) -> Unit = {}
    var onPolylineClick: (Polyline) -> Unit = {}
    var onNodeAdded: (Node) -> Unit = {}
    var onLineAdded: (Line) -> Unit = {}
    var onNodesLinesLoaded: () -> Unit = {}
    private var currentUserNode: Node? = null
    private var focusedGuildPath: Polyline? = null
    private var lastSelectedMarker: Marker? = null
    private var aStarSearch: AStarSearch? = null
    private var currentGuildPath: Polyline? = null
    private var allSuggestPlaces: MutableList<Marker> = mutableListOf()
    private var allServicePlaces: MutableList<Marker> = mutableListOf()
    private var selectablePlaces: MutableList<Marker> = mutableListOf()
    private var fusedLocationClient: FusedLocationProviderClient? = null

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
                    MapMode.SUGGEST_ROUTE -> {
                        handleDisplayMapPaths()
                        showServicePlacesToMap()
                        onNodesLinesLoaded()
                    }

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

        parentActivity?.run {
            checkLocationPermission {
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            }
        }

        initMap()
        handleMap()
    }

    override fun initListener() {
        viewModel.currentTouchEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                TouchState.OFF -> {
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

        setCurrentTouchEvent(TouchState.OFF)

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

//            if (mapMode == MapMode.SUGGEST_ROUTE) {
//                mMap?.setPadding(0, 0, 0, 350)
//            }
            setMapStyle()
            setMapListener()
            drawBorderLine()
            fetchData()
        }
    }

    private fun setMapListener() {
        mMap?.run {

            setOnPolylineClickListener { polyline ->
                if (viewModel.currentTouchEvent.value == TouchState.OFF) {
                    onPolylineClick(polyline)
                }
            }

            setOnMarkerClickListener { marker ->
                when (viewModel.currentTouchEvent.value) {
                    TouchState.DRAW_LINE_STEP_1 -> {
                        handleDrawLineStep1(marker)
                    }

                    TouchState.DRAW_LINE_STEP_2 -> {
                        handleDrawLineStep2(marker)
                    }

                    TouchState.FIND_ROUTE_STEP_1 -> {
                        handleFindRouteStep1(marker)
                    }

                    TouchState.FIND_ROUTE_STEP_2 -> {
                        handleFindRouteStep2(marker)
                    }

                    TouchState.OFF -> {
                        getNodeById(marker.tag.toString())?.let { node ->
                            onMarkerClick(node)
                        }
                    }
                }

                return@setOnMarkerClickListener true
            }

            setOnMapClickListener { latLng ->
                log(latLng.toString())
                TouchState.run {
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

    private fun handleDrawStreetPath(nodes: List<Node>): Polyline? {
        return mMap?.addPolyline(
            PolylineOptions()
                .color(ContextCompat.getColor(requireContext(), R.color.guidePathColor))
                .addAll(nodes.map { LatLng(it.latitude, it.longitude) })
                .jointType(JointType.ROUND)
                .width(10f)
        )
    }

    private fun handleDrawGuildPath(nodes: List<Node>): Polyline? {
        return mMap?.addPolyline(
            PolylineOptions()
                .color(ContextCompat.getColor(requireContext(), R.color.cyanBlueAzure))
                .addAll(nodes.map { LatLng(it.latitude, it.longitude) })
                .jointType(JointType.ROUND)
                .zIndex(Float.POSITIVE_INFINITY)
                .width(15f)
        )
    }

    fun startDrawLine() {
        setCurrentTouchEvent(TouchState.DRAW_LINE_STEP_1)
    }

    fun startFindRoute() {
        setCurrentTouchEvent(TouchState.FIND_ROUTE_STEP_1)
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

    private fun getUnselectedGameImage() =
        BitmapDescriptorFactory.fromBitmap(
            resizeMapIcons(
                resId = R.drawable.ic_unselected_game,
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
        setCurrentTouchEvent(TouchState.DRAW_LINE_STEP_2)
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
                    setCurrentTouchEvent(TouchState.DRAW_LINE_STEP_1)
                } else {
                    context?.run {
                        showToast(getString(R.string.err_conflict_line))
                    }
                }
            } else {
                lastMarker.setIcon((getNodeImage()))
                setCurrentTouchEvent(TouchState.DRAW_LINE_STEP_1)
            }
        }
    }

    private fun handleFindRouteStep1(marker: Marker) {
        lastSelectedMarker = marker
        currentGuildPath?.remove()
        marker.setIcon(getSelectedNodeImage())
        viewModel.updateLineViewState(isVisible = true)
        setCurrentTouchEvent(TouchState.FIND_ROUTE_STEP_2)
    }

    private fun handleFindRouteStep2(marker: Marker) {
        lastSelectedMarker?.let { lastMarker ->
            val start = viewModel.getNodeById(lastMarker.tag?.toString())
            val goal = viewModel.getNodeById(marker.tag?.toString())

            if (lastSelectedMarker != marker && start != null && goal != null) {
                aStarSearch?.findBestPath(start, goal) { nodes, _ ->
                    currentGuildPath?.remove()
                    currentGuildPath = handleDrawStreetPath(nodes)
                    viewModel.updateLineViewState(isVisible = false)
                }
            }

            updateMarkerIcon(lastMarker, lastMarker.tag.toString())
            setCurrentTouchEvent(TouchState.FIND_ROUTE_STEP_1)
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

    fun getDistance(p1: LatLng, p2: LatLng): Float? {
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
        allSuggestPlaces.run {
            forEach { it.remove() }
            clear()
        }
        handleDisplaySuggestPlaceMarker(suggestRoute)
    }

    fun handleDisplaySelectableNode(listPlace: List<Place>) {
        allSuggestPlaces.run {
            forEach {
                it.isVisible = false
            }
        }
        allServicePlaces.run {
            forEach {
                it.isVisible = false
            }
        }
        focusedGuildPath?.isVisible = false
        selectablePlaces.run {
            forEach {
                it.remove()
            }
            clear()
        }
        viewModel.run {
            loadingProgressLiveData.value = true
            viewModelScope.launch(Dispatchers.IO) {
                launch {
                    listPlace.forEach { place ->
                        getNodeByPlaceId(place.id)?.run {
                            withContext(Dispatchers.Main) {
                                if (place.game != null) {
                                    drawMarker(
                                        latLng = LatLng(latitude, longitude),
                                        nodeId = id,
                                        bitmapDescriptor = getGameImage()
                                    )?.let { selectablePlaces.add(it) }
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
                                    )?.let { selectablePlaces.add(it) }
                                }
                            }
                        }
                    }
                }.join()
                withContext(Dispatchers.Main) {
                    loadingProgressLiveData.value = false
                }
            }
        }
    }

    private fun handleDisplayMapPaths() {
        viewModel.run {
            viewModelScope.launch(Dispatchers.IO) {
                val visited = mutableListOf(listNode.first().id ?: "")
                listNode.forEach { node ->
                    node.neighbors.forEach { neighbor ->
                        visited.add(neighbor.id ?: "")
                        getNodeById(neighbor.id)?.let { neighborNode ->
                            withContext(Dispatchers.Main) {
                                handleDrawStreetPath(listOf(neighborNode, node))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showServicePlacesToMap() {
        viewModel.run {
            viewModelScope.launch(Dispatchers.IO) {
                listNode.forEach { node ->
                    getPlaceById(node.placeId)?.let { place ->
                        if (place.game == null) {
                            withContext(Dispatchers.Main) {
                                drawMarker(
                                    latLng = LatLng(
                                        node.latitude,
                                        node.longitude
                                    ),
                                    nodeId = node.id,
                                    bitmapDescriptor = getPlaceImageWithDrawable(
                                        res = place.serviceType?.imgRes
                                            ?: R.drawable.ic_place_node,
                                        size = 90
                                    )
                                )?.let { allServicePlaces.add(it) }
                            }
                        }
                    }
                }
            }
        }
    }

    fun updateCurrentLocation(location: Location) {
        currentUserNode?.removeMarker()
        currentUserNode = Node(latitude = location.latitude, longitude = location.longitude).apply {
            marker =
                drawMarker(latLng = LatLng(latitude, longitude), bitmapDescriptor = getNodeImage())
        }
    }

    private fun handleDisplaySuggestPlaceMarker(suggestRoute: List<RouteItem>) {
        viewModel.run {
            loadingProgressLiveData.value = true
            viewModelScope.launch(Dispatchers.IO) {
                launch {
                    localRepository.listPlace.forEach { place ->
                        getNodeByPlaceId(place.id)?.run {
                            withContext(Dispatchers.Main) {
                                if (place.game != null) {
                                    drawMarker(
                                        latLng = LatLng(latitude, longitude),
                                        nodeId = id,
                                        bitmapDescriptor =
                                        if (suggestRoute.map { it.place }.contains(place)) {
                                            getGameImage()
                                        } else {
                                            getUnselectedGameImage()
                                        }
                                    )?.let { allSuggestPlaces.add(it) }
                                }
                            }
                        }
                    }
                    localRepository.listPlace.filterNot { place ->
                        suggestRoute.map { it.place }.contains(place)
                    }
                        .forEach {

                        }

                }.join()
                withContext(Dispatchers.Main) {
                    loadingProgressLiveData.value = false
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

    fun drawSelectedGuildPath(placeId: Int, currentLocation: Location) {
        focusedGuildPath?.remove()
        viewModel.run {
            viewModelScope.launch(Dispatchers.IO) {
                getNodeByPlaceId(placeId)?.let { goal ->
                    val yourNode = Node(
                        latitude = currentLocation.latitude,
                        longitude = currentLocation.longitude
                    )
                    val nearestNode = getNearestNode(yourNode, goal)
                    nearestNode.let { start ->
                        aStarSearch?.findBestPath(start, goal) { nodes, _ ->
                            launch(Dispatchers.Main) {
                                focusedGuildPath = handleDrawGuildPath(nodes.toMutableList().apply {
                                    add(0, yourNode)
                                })
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getNearestNode(currentNode: Node, destination: Node): Node {
        return viewModel.run {
            listNode
                .filter { node ->
                    val isOnNorthEast =
                        destination.latitude > currentNode.latitude && destination.longitude > currentNode.longitude
                    val isOnSouthEast =
                        destination.latitude < currentNode.latitude && destination.longitude > currentNode.longitude
                    val isOnNorthWest =
                        destination.latitude > currentNode.latitude && destination.longitude < currentNode.longitude
                    val isOnSouthWest =
                        destination.latitude < currentNode.latitude && destination.longitude < currentNode.longitude

                    when {
                        isOnNorthEast -> destination.latitude > node.latitude && node.longitude > currentNode.longitude
                        isOnSouthEast -> destination.latitude < node.latitude && node.longitude > currentNode.longitude
                        isOnNorthWest -> destination.latitude > node.latitude && node.longitude < currentNode.longitude
                        isOnSouthWest -> destination.latitude < node.latitude && node.longitude < currentNode.longitude
                        else -> false
                    }
                }
                .minBy {
                    aStarSearch?.getDistance(it, currentNode) ?: Float.POSITIVE_INFINITY
                }
        }
    }

    fun clearDisplayedSelectableNode() {
        allSuggestPlaces.run {
            forEach {
                it.isVisible = true
            }
        }
        allServicePlaces.run {
            forEach {
                it.isVisible = true
            }
        }
        focusedGuildPath?.isVisible = true
        selectablePlaces.run {
            forEach {
                it.remove()
            }
            clear()
        }
    }

    fun getNodeByPlaceId(placeId: Int) = viewModel.getNodeByPlaceId(placeId)

    private fun getLatestLocation(action: (Location) -> Unit) {
        parentActivity?.checkLocationPermission {
            fusedLocationClient?.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                ?.addOnSuccessListener { location ->
                    location?.let(action)
                }
        }
    }

    fun addCurrentLocationNode() {
        getLatestLocation { location ->
            viewModel.run {
                loadingProgressLiveData.value = true
                viewModelScope.launch {
                    drawNodeMarker(
                        latLng = LatLng(location.latitude, location.longitude),
                        onDrawn = { marker ->
                            viewModel.addNode(marker)
                            loadingProgressLiveData.value = false
                            context?.showToast("Your current location added")
                        }).await()
                }
            }
        }
    }

    fun showPlaceByFilter(filter: String) {
        viewModel.run {
            allSuggestPlaces.forEach { marker ->
                getPlaceById(getNodeById(marker.tag.toString())?.placeId)?.let { place ->
                    marker.isVisible =
                        place.game?.name?.lowercase()?.contains(filter.lowercase()) == true
                }
            }
            allServicePlaces.forEach { marker ->
                getPlaceById(getNodeById(marker.tag.toString())?.placeId)?.let { place ->
                    marker.isVisible = place.name.lowercase().contains(filter.lowercase()) == true
                }
            }
        }
    }
}