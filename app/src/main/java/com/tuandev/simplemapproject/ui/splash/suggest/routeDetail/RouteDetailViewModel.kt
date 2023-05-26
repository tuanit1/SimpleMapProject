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

@HiltViewModel
class RouteDetailViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
    private val localRepository: LocalRepository,
    private val fireStoreRepository: FireStoreRepository,
) : BaseViewModel<ViewState>() {

    private val suggestPlaceList: MutableList<Place> = mutableListOf()
    private var listNode: MutableList<Node> = mutableListOf()
    private var aStarSearch: AStarSearch? = null
    var listLine: MutableList<Line> = mutableListOf()

    fun suggestGame(userFeature: UserFeature) {
        val listGamePlaces = listNode
            .mapNotNull { it.placeId }
            .mapNotNull { localRepository.listPlace.getPlaceById(it) }
            .filter { it.game != null }

        userFeature.run {
            suggestPlaceList.clear()
            suggestPlaceList.addAll(
                when {
                    isFamilyOnly -> listGamePlaces.filter { it.game?.thrillLevel?.score == 0 }
                    isThrillOnly -> listGamePlaces.filter {
                        it.game?.thrillLevel?.score!! in 1..(maxThrill?.score ?: 3)
                    }
                    else -> listGamePlaces.filter {
                        it.game?.thrillLevel?.score!! in 0..(maxThrill?.score ?: 3)
                    }
                }
            )
        }

        sortRouteByTSP()

        val a = calculateEstimateTime()
        log("estimate time cost $a hour")
    }

    private fun calculateEstimateTime(): Float {
        var totalDistance = 0f
        var totalDurationInSec = 0
        val walkVelocity = 1.34

        for (i in 0 until suggestPlaceList.size - 1) {
            val startNode = listNode.find { it.placeId == suggestPlaceList[i].id }
            val endNode = listNode.find { it.placeId == suggestPlaceList[i + 1].id }

            if (startNode != null && endNode != null) {
                aStarSearch?.findBestPath(startNode, endNode) { _, distance ->
                    log("${suggestPlaceList[i].game?.name} -> ${suggestPlaceList[i + 1].game?.name}: $distance")
                    totalDistance += distance
                }
            }
        }

        suggestPlaceList.forEach { place ->
            totalDurationInSec += if (place.game != null) {
                (place.game.duration + 120)
            } else {
                120
            }
        }

        totalDurationInSec += (totalDistance / walkVelocity).toInt()

        return totalDurationInSec / 3600f
    }

    private fun getSuggestPlaceNodesWithNeighbor(): List<Node> {
        val placeNodes = getSuggestedPlaceNodes()
        listNode.find { it.placeId == placeRepository.placeEntryGate.id }?.let { startNode ->
            placeNodes.add(0, startNode)
        }

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

        suggestPlaceList.clear()
        suggestPlaceList.addAll(visited.mapNotNull { it.placeId }
            .mapNotNull { localRepository.listPlace.getPlaceById(it) })
    }

    private fun getSuggestedPlaceNodes(): MutableList<Node> {
        return listNode.filter { node ->
            node.placeId?.let { placeId ->
                localRepository.listPlace.getPlaceById(placeId)?.let { place ->
                    suggestPlaceList.contains(place)
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


}