package com.tuandev.simplemapproject.ui.splash.suggest.routeDetail

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
import java.util.*
import javax.inject.Inject

sealed class RouteDetailViewState : ViewState() {
    class OnSuggestRouteFinish(val suggestList: MutableList<RouteItem>, val estimatedTime: Float) :
        RouteDetailViewState()
}

@HiltViewModel
class RouteDetailViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
    private val localRepository: LocalRepository,
    private val fireStoreRepository: FireStoreRepository,
) : BaseViewModel<ViewState>() {

    private val suggestPlaceList: MutableList<RouteItem> = mutableListOf()
    private var listNode: MutableList<Node> = mutableListOf()
    private var aStarSearch: AStarSearch? = null
    private var listLine: MutableList<Line> = mutableListOf()
    private var placeScoreList: MutableList<Pair<Place, Float>> = mutableListOf()
    private var latestEstimateTime = 0f
    private var startPLace = placeRepository.placeFountain
    private var finishPLace = placeRepository.placeFountain

    fun suggestGame(userFeature: UserFeature) {
        val listGamePlaces = listNode
            .mapNotNull { it.placeId }
            .mapNotNull { localRepository.listPlace.getPlaceById(it) }
            .filter { it.game != null }

        userFeature.run {
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
                add(0, RouteItem(place = startPLace))
                add(RouteItem(place = finishPLace))
            }

            sortRouteByTSP()
            calculatePlaceScore()

            log("Desired time: $availableTime")
            while (calculateEstimateTime() > availableTime) {
                val worstPlace = placeScoreList.maxBy { it.second }
                suggestPlaceList.removeAll { it.place == worstPlace.first }
                placeScoreList.remove(worstPlace)
            }

            updateViewState(
                RouteDetailViewState.OnSuggestRouteFinish(
                    suggestList = suggestPlaceList,
                    estimatedTime = latestEstimateTime
                )
            )

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

    private fun calculatePlaceScore() {
        var maxDistance = 0f
        val entryGate = getNodeByPlaceId(placeRepository.placeEntryGate.id)
        val maxThrillScore = localRepository.listThrillLevel.maxOfOrNull { it.score } ?: 0

        val distanceAndThrills = suggestPlaceList.map { routeItem ->
            val node = getNodeByPlaceId(routeItem.place.id)
            val distance = if (entryGate != null && node != null) {
                var mDistance = Float.POSITIVE_INFINITY
                aStarSearch?.findBestPath(entryGate, node) { _, distance ->
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
                (maxThrillScore - routeItem.place.game.thrillLevel.score) / maxThrillScore.toFloat()
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

        for (i in 0 until suggestPlaceList.size - 1) {
            val startNode = getNodeByPlaceId(suggestPlaceList[i].place.id)
            val endNode = getNodeByPlaceId(suggestPlaceList[i + 1].place.id)

            if (startNode != null && endNode != null) {
                aStarSearch?.findBestPath(startNode, endNode) { _, distance ->
                    log("${suggestPlaceList[i].place.game?.name} -> ${suggestPlaceList[i + 1].place.game?.name}: $distance")
                    totalDistance += distance
                }
            }
        }

        suggestPlaceList.forEach { place ->
            totalDurationInSec += if (place.place.game != null) {
                (place.place.game.duration + 120)
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

        suggestPlaceList.run {
            clear()
            addAll(visited.mapNotNull { it.placeId }
                .mapNotNull { localRepository.listPlace.getPlaceById(it) }.map { RouteItem(place = it) })
            add(0, RouteItem(place = startPLace))
            add(RouteItem(place = finishPLace))
        }
    }

    private fun getSuggestedPlaceNodes(): MutableList<Node> {
        return listNode.filter { node ->
            node.placeId?.let { placeId ->
                localRepository.listPlace.getPlaceById(placeId)?.let { place ->
                    suggestPlaceList.filter { it.place != startPLace || it.place != finishPLace }
                        .map { it.place }
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
}