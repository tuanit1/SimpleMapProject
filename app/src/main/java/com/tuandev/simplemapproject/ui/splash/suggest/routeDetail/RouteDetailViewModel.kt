package com.tuandev.simplemapproject.ui.splash.suggest.routeDetail

import android.location.Location
import androidx.lifecycle.viewModelScope
import com.tuandev.simplemapproject.base.BaseViewModel
import com.tuandev.simplemapproject.base.ViewState
import com.tuandev.simplemapproject.data.models.*
import com.tuandev.simplemapproject.data.repositories.local.LocalRepository
import com.tuandev.simplemapproject.data.repositories.local.PlaceRepository
import com.tuandev.simplemapproject.data.repositories.remote.FireStoreRepository
import com.tuandev.simplemapproject.extension.log
import com.tuandev.simplemapproject.extension.mapToLine
import com.tuandev.simplemapproject.extension.mapToNode
import com.tuandev.simplemapproject.util.AStarSearch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

sealed class RouteDetailViewState : ViewState() {
    class OnSuggestListUpdated(
        val suggestList: MutableList<RouteItem>,
        val estimatedTime: Float,
        val isUpdateViewOnly: Boolean = false
    ) :
        RouteDetailViewState()

    class OnUpdateCurrentPlace(val suggestList: MutableList<RouteItem>) : RouteDetailViewState()

    object OnFetchNodeLineDataSuccess : RouteDetailViewState()
}

@HiltViewModel
class RouteDetailViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
    private val localRepository: LocalRepository,
    private val fireStoreRepository: FireStoreRepository,
) : BaseViewModel<RouteDetailViewState>() {

    var mUserFeature: UserFeature? = null
    var isFirstLoad = true
    private val suggestPlaceList: MutableList<RouteItem> = mutableListOf()
    private var saveSuggestPlaceList: MutableList<RouteItem> = mutableListOf()
    private var listNode: MutableList<Node> = mutableListOf()
    private var listLine: MutableList<Line> = mutableListOf()
    private var aStarSearch: AStarSearch? = null
    private var placeScoreList: MutableList<Pair<Place, Float>> = mutableListOf()
    private var latestEstimateTime = 0f
    private var currentUserNode: Node? = null
    private var finishPLace = placeRepository.placeFountain

    fun suggestRoute() {
        viewModelScope.launch(Dispatchers.IO) {
            val listGamePlaces = listNode
                .mapNotNull { it.placeId }
                .mapNotNull { localRepository.listPlace.getPlaceById(it) }
                .filter { it.game != null }

            mUserFeature?.run {
                suggestPlaceList.run {
                    clear()
                    addAll(
                        when {
                            isFamilyOnly -> listGamePlaces.filter { it.game?.thrillLevel?.score == 0 }
                                .map { RouteItem(place = it) }
                            isThrillOnly -> listGamePlaces.filter {
                                it.game?.thrillLevel?.score!! in 1..(maxThrill?.score ?: 3)
                            }.map { RouteItem(place = it) }
                            else -> listGamePlaces.filter {
                                it.game?.thrillLevel?.score!! in 0..(maxThrill?.score ?: 3)
                            }.map { RouteItem(place = it) }
                        }
                    )
                }

                sortRouteByTSP()
                calculatePlaceScore()

                log("Desired time: $availableTime")
                while (calculateEstimateTime() > availableTime) {
                    val worstPlace = placeScoreList.maxBy { it.second }
                    suggestPlaceList.removeAll { it.place == worstPlace.first }
                    placeScoreList.remove(worstPlace)
                }

                updateSuggestRouteIndex()

                withContext(Dispatchers.Main) {
                    updateViewState(
                        RouteDetailViewState.OnSuggestListUpdated(
                            suggestList = suggestPlaceList,
                            estimatedTime = latestEstimateTime
                        )
                    )
                }

                log("\nSuggest list:")
                suggestPlaceList.forEach { routeItem ->
                    routeItem.place.run {
                        if (game != null) {
                            log(game.name)
                        } else {
                            log(name)
                        }
                    }
                }
            }
        }
    }

    private fun updateSuggestRouteIndex() {
        for (i in 0 until suggestPlaceList.size) {
            suggestPlaceList[i].itemIndex = i + 1
        }
    }

    private fun calculatePlaceScore() {
        var maxDistance = 0f
        val maxThrillScore = localRepository.listThrillLevel.maxOfOrNull { it.score } ?: 0

        val distanceAndThrills =
            suggestPlaceList.filterNot { it.place == finishPLace }.map { routeItem ->
                val node = getNodeByPlaceId(routeItem.place.id)
                val distance = if (currentUserNode != null && node != null) {
                    var mDistance = Float.POSITIVE_INFINITY
                    aStarSearch?.findBestPath(currentUserNode!!, node) { _, distance ->
                        if (maxDistance < distance) {
                            maxDistance = distance
                        }
                        mDistance = distance
                    }
                    mDistance
                } else {
                    Float.POSITIVE_INFINITY
                }

                val invertNormalizerThrillLevel = if (routeItem.place.game != null) {
                    (maxThrillScore - routeItem.place.game!!.thrillLevel.score) / maxThrillScore.toFloat()
                } else {
                    0f
                }

                Triple(routeItem.place, distance, invertNormalizerThrillLevel)
            }.toMutableList()

        for (index in distanceAndThrills.indices) {
            val item = distanceAndThrills[index]
            distanceAndThrills[index] = item.copy(second = item.second / maxDistance)
        }

        placeScoreList.run {
            clear()
            addAll(distanceAndThrills.map {
                Pair(
                    first = it.first,
                    second = (it.second + it.third) / 2f
                )
            })
        }
    }

    private fun calculateEstimateTime(): Float {
        var totalDistance = 0f
        var totalDurationInSec = 0
        val walkVelocity = 1.34
        val distanceThresholdToBreakTime = 500 //in second
        val breakTime = 60 * 10

        val tempList = suggestPlaceList.toMutableList()
        tempList.add(0, RouteItem(isStart = true, place = placeRepository.placeStart))

        for (i in 0 until tempList.size - 1) {
            val start = if (i == 0) currentUserNode else getNodeByPlaceId(tempList[i].place.id)
            val goal = getNodeByPlaceId(tempList[i + 1].place.id)

            if (start != null && goal != null) {
                aStarSearch?.findBestPath(start, goal) { _, distance ->
                    log("${tempList[i].place.run { game?.name ?: name }} -> ${tempList[i + 1].place.run { game?.name ?: name }}: $distance")
                    totalDistance += distance
                }
            }
        }

        tempList.forEach { place ->
            totalDurationInSec += if (place.place.game != null) {
                (place.place.game!!.duration + 120)
            } else {
                120
            }
        }

        totalDurationInSec += (totalDistance / walkVelocity).toInt()
        totalDurationInSec += ((totalDistance / distanceThresholdToBreakTime).toInt() * breakTime)

        val totalDurationInHour = totalDurationInSec / 3600f
        latestEstimateTime = totalDurationInHour
        log("Calculated total time: ${totalDurationInSec / 3600f}, Total distance: $totalDistance")
        return totalDurationInHour
    }

    private fun getSuggestPlaceNodesWithNeighbor(): List<Node> {
        val placeNodes = getSuggestedPlaceNodes()
        currentUserNode?.let { placeNodes.add(0, it) }
        val placeNodesWithNeighbor = placeNodes.map { it.copy(neighbors = mutableListOf()) }

        placeNodes.forEachIndexed { index, currentNode ->
            val exceptList = placeNodes.filter { it.id != currentNode.id }
            exceptList.forEach { neighBor ->
                aStarSearch?.findBestPath(
                    start = currentNode,
                    goal = neighBor
                ) { _, totalDistance ->
                    placeNodesWithNeighbor[index].neighbors.add(
                        NeighborWithDistance(
                            neighBor.id,
                            totalDistance
                        )
                    )
                }
            }
        }

        return placeNodesWithNeighbor
    }

    private fun sortRouteByTSP() {
        val visitedRouteItems = suggestPlaceList.filter { it.itemState == RouteItem.VISITED }
        suggestPlaceList.removeAll { it.itemState == RouteItem.VISITED }

        val suggestPlaceNodes = getSuggestPlaceNodesWithNeighbor()
        val startNode = suggestPlaceNodes.first()
        val stack = Stack<Node>()
        val visited: MutableList<Node> = mutableListOf(startNode)
        var currentMin: Float
        var bestNeighBor: Node? = null
        var minFlag = false

        stack.push(startNode)

        while (stack.isNotEmpty()) {
            val currentNode = stack.peek()
            currentMin = Float.POSITIVE_INFINITY
            currentNode.neighbors.forEach {
                val neighBor = suggestPlaceNodes.getNodeById(it.id)
                if (!visited.contains(neighBor)) {
                    if (currentMin > (it.distance ?: 0f)) {
                        currentMin = it.distance ?: 0f
                        minFlag = true
                        bestNeighBor = neighBor
                    }
                }
            }
            if (minFlag) {
                bestNeighBor?.let {
                    visited.add(it)
                    stack.push(it)
                    minFlag = false
                }
                continue
            }
            stack.pop()
        }

        visited.removeFirst()

        suggestPlaceList.run {
            clear()
            addAll(visitedRouteItems)
            addAll(visited.mapNotNull { it.placeId }
                .mapNotNull { localRepository.listPlace.getPlaceById(it) }
                .mapIndexed { index, place ->
                    RouteItem(
                        place = place,
                        itemState = if (index == 0) RouteItem.SELECTED else RouteItem.NOT_VISITED
                    )
                })
            add(
                RouteItem(
                    place = finishPLace,
                    itemState = if (visited.isEmpty()) RouteItem.SELECTED else RouteItem.NOT_VISITED
                )
            )
        }
    }

    private fun getSuggestedPlaceNodes(): MutableList<Node> {
        return listNode.filter { node ->
            node.placeId?.let { placeId ->
                localRepository.listPlace.getPlaceById(placeId)?.let { place ->
                    suggestPlaceList
                        .map { it.place }
                        .filter { it != finishPLace }
                        .contains(place)
                } ?: false
            } ?: false
        }.toMutableList()
    }

    private fun List<Place>.getPlaceById(id: Int) = find { it.id == id }
    private fun List<Node>.getNodeById(id: String?) = find { it.id == id }
    fun fetchAllNodesAndLines() {
        callApiFromFireStore(
            task = fireStoreRepository.getAllNodes(),
            onSuccess = { nodeResult ->
                listNode.clear()
                listNode.addAll(nodeResult.map { it.mapToNode() })

                callApiFromFireStore(
                    task = fireStoreRepository.getAllLines(),
                    onSuccess = { lineResult ->
                        viewModelScope.launch(Dispatchers.IO) {
                            listLine.clear()
                            listLine.addAll(lineResult.map { it.mapToLine() })
                            listNode.updateNeighbors()
                            aStarSearch = AStarSearch(listNode)
                            updateViewState(RouteDetailViewState.OnFetchNodeLineDataSuccess)
                        }
                    },
                    isShowLoading = true
                )
            }, isShowLoading = true
        )
    }

    private fun MutableList<Node>.updateNeighbors() {
        forEach { node ->
            node.neighbors.clear()
            node.neighbors.addAll(
                listLine.mapNotNull { line ->
                    when {
                        line.firstNodeId == node.id -> NeighborWithDistance(
                            line.secondNodeId,
                            line.distance
                        )
                        line.secondNodeId == node.id -> NeighborWithDistance(
                            line.firstNodeId,
                            line.distance
                        )
                        else -> null
                    }
                }
            )
        }
    }

    private fun getNodeByPlaceId(placeId: Int) = listNode.find { it.placeId == placeId }
    fun getAddablePlace(): List<Place> {
        return localRepository.listPlace.filter { place ->
            !suggestPlaceList.map { routeItem -> routeItem.place }
                .contains(place) && getNodeByPlaceId(place.id) != null
        }
    }

    fun getReplaceablePlace(replaceIndex: Int?): List<Place> {
        return replaceIndex?.run {
            localRepository.listPlace.filter { place ->
                (getNodeByPlaceId(place.id) != null) && (suggestPlaceList[replaceIndex].place != place)
            }
        } ?: listOf()
    }

    fun handleAddRouteItem(placeId: Int) {
        if (getNodeByPlaceId(placeId) != null) {
            localRepository.listPlace.find { it.id == placeId }?.let { place ->
                saveCurrentSuggestList()
                suggestPlaceList.add(RouteItem(place = place))
                handleUpdateSuggestNode()
            }
        } else {
            showErrorPopup("Node of this place has not been assigned yet")
        }
    }

    fun handleReplaceItem(placeId: Int, replaceIndex: Int) {
        if (getNodeByPlaceId(placeId) != null) {
            localRepository.listPlace.find { it.id == placeId }?.let { place ->
                if (replaceIndex == suggestPlaceList.lastIndex) {
                    finishPLace = place
                }
                saveCurrentSuggestList()

                if (suggestPlaceList.map { it.place }.contains(place)) {
                    suggestPlaceList.indexOfFirst { it.place == place }.takeIf { it != -1 }
                        ?.let { selectedIndex ->
                            suggestPlaceList[selectedIndex].place =
                                suggestPlaceList[replaceIndex].place
                        }
                }
                suggestPlaceList[replaceIndex] = RouteItem(place = place)

                handleUpdateSuggestNode(isSort = false)
            }
        } else {
            showErrorPopup("Node of this place has not been assigned yet")
        }
    }

    fun handleDeleteSuggestNode(removeIndex: Int) {
        if (removeIndex == suggestPlaceList.lastIndex) {
            showErrorPopup("You can not remove the finishing place")
        } else {
            suggestPlaceList.removeAt(removeIndex)
            handleUpdateSuggestNode()
        }
    }

    fun handleUpdateSuggestNode(isSort: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isSort) {
                sortRouteByTSP()
            }
            updateSuggestRouteIndex()
            val newEstimatedTime = calculateEstimateTime()
            withContext(Dispatchers.Main) {
                updateViewState(
                    RouteDetailViewState.OnSuggestListUpdated(
                        suggestPlaceList,
                        newEstimatedTime
                    )
                )
            }
        }
    }

    fun updateCurrentPlace(position: Int) {
        val currentIndex = suggestPlaceList.indexOfFirst { it.itemState == RouteItem.SELECTED }

        if (currentIndex != position) {
            for (i in suggestPlaceList.indices) {
                val newState = when {
                    i < position -> RouteItem.VISITED
                    i == position -> RouteItem.SELECTED
                    else -> RouteItem.NOT_VISITED
                }
                suggestPlaceList[i].itemState = newState
            }
            updateViewState(RouteDetailViewState.OnUpdateCurrentPlace(suggestPlaceList))
        } else {
            showErrorPopup("You are currently on this place")
        }
    }

    private fun saveCurrentSuggestList() {
        saveSuggestPlaceList.run {
            clear()
            addAll(suggestPlaceList)
        }
    }

    fun restoreSavedSuggestList() {
        suggestPlaceList.run {
            clear()
            addAll(saveSuggestPlaceList)
        }
    }

    fun getSuggestList() = suggestPlaceList

    fun updateSuggestList(suggestList: List<RouteItem>) {
        suggestPlaceList.run {
            clear()
            addAll(suggestList)
            val newEstimatedTime = calculateEstimateTime()
            updateViewState(
                RouteDetailViewState.OnSuggestListUpdated(
                    suggestPlaceList,
                    newEstimatedTime,
                    isUpdateViewOnly = true
                )
            )
        }
    }

    fun setCurrentUserNode(location: Location) {
        val tempNode = Node(
            latitude = location.latitude,
            longitude = location.longitude
        )
        currentUserNode =
            listNode.minBy { aStarSearch?.getDistance(it, tempNode) ?: Float.POSITIVE_INFINITY }
    }
}