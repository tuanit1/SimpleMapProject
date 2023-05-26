package com.tuandev.simplemapproject.ui.splash.suggest.routeDetail

import androidx.lifecycle.viewModelScope
import com.tuandev.simplemapproject.base.BaseViewModel
import com.tuandev.simplemapproject.base.ViewState
import com.tuandev.simplemapproject.data.models.*
import com.tuandev.simplemapproject.data.repositories.local.LocalRepository
import com.tuandev.simplemapproject.data.repositories.local.PlaceRepository
import com.tuandev.simplemapproject.data.repositories.remote.FireStoreRepository
import com.tuandev.simplemapproject.extension.mapToLine
import com.tuandev.simplemapproject.extension.mapToNode
import com.tuandev.simplemapproject.util.AStarSearch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Stack
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
                    isThrillOnly -> listGamePlaces.filter { it.game?.thrillLevel?.score!! > 0 }
                    else -> listGamePlaces
                }
            )
        }
        sortRoute()
    }

    private fun sortRoute() {
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

        sortRouteByTSP()
    }

    private fun sortRouteByTSP(listNode: List<Node>) {

        val visited: MutableList<Node> = mutableListOf()
        val stack = Stack<Node>()
        stack.push(listNode.first())
        var currentMin: Int = Int.MAX_VALUE
        var minFlag = false

        while (!stack.isEmpty()){
            val currentNode = stack.peek()
            currentMin = Int.MAX_VALUE
            currentNode.neighbors.forEach {
                val neighBor = listNode.getNodeById(it.id)
            }
        }

        listNode.forEach { node ->
            node.neighbors.forEach { neighBor ->

            }
        }
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