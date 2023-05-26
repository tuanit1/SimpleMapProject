package com.tuandev.simplemapproject.util

import android.location.Location
import com.tuandev.simplemapproject.data.models.Node
import com.tuandev.simplemapproject.extension.toRoundedFloat

data class AStarSearch(
    private val listNode: List<Node>
) {
    private val openList: MutableList<Node> = mutableListOf()
    private val visitedList: MutableList<String> = mutableListOf()
    private val cameFrom: MutableMap<Node, Node> = mutableMapOf()
    private val gScore: MutableMap<Node, Float> = mutableMapOf()
    private val fScore: MutableMap<Node, Float> = mutableMapOf()
    var onFindPathFail: () -> Unit = {}

    private fun regeneratePath(current: Node, action: (List<Node>, Float) -> Unit) {
        val path = mutableListOf(current)
        var currentNode = current
        while (cameFrom.containsKey(currentNode)) {
            cameFrom[currentNode]?.let { node ->
                currentNode = node
                path.add(0, node)
            }
        }
        action(path, gScore[current] ?: 0f)
    }

    fun findBestPath(
        start: Node,
        goal: Node,
        onSuccessSearch: (List<Node>, Float) -> Unit
    ) {
        initData(start, goal)

        while (openList.isNotEmpty()) {
            val current = fScore.filter { openList.contains(it.key) }.minBy { it.value }.key
            if (current == goal) {
                regeneratePath(current, onSuccessSearch)
                return
            }

            openList.remove(current)
            visitedList.add(current.id.toString())

            current.neighbors
                .filterNot { visitedList.contains(it.id) }
                .forEach { neighborWithDistance ->
                    getNode(neighborWithDistance.id)?.let { neighBor ->
                        val g = gScore[current] ?: Float.POSITIVE_INFINITY
                        val tentativeGScore = g.plus(neighborWithDistance.distance ?: 0f)
                        val neighBorGScore = gScore[neighBor] ?: Float.POSITIVE_INFINITY


                        if (tentativeGScore < neighBorGScore) {
                            cameFrom[neighBor] = current
                            gScore[neighBor] = tentativeGScore
                            fScore[neighBor] = tentativeGScore + (getDistance(neighBor, goal) ?: 0f)

                            if (!openList.contains(neighBor)) {
                                openList.add(neighBor)
                            }
                        }
                    }
                }
        }

//        log("Open set is empty but goal was never reached")
        onFindPathFail()
    }

    private fun initData(start: Node, goal: Node) {
        visitedList.clear()
        cameFrom.clear()
        openList.run {
            clear()
            add(start)
        }
        gScore.run {
            clear()
            put(start, 0f)
        }
        fScore.run {
            clear()
            put(start, getDistance(start, goal) ?: 0f)
        }
    }

    private fun getNode(id: String?) = listNode.find { it.id == id }

    private fun getDistance(node1: Node, node2: Node): Float? {
        return try {
            val results = FloatArray(1)
            Location.distanceBetween(
                node1.latitude, node1.longitude, node2.latitude, node2.longitude, results
            )
            results.first().toRoundedFloat(2)
        } catch (e: Exception) {
            null
        }
    }
}