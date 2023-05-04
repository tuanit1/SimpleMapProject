package com.tuandev.simplemapproject.base.map

import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.tuandev.simplemapproject.base.BaseViewModel
import com.tuandev.simplemapproject.base.ViewState
import com.tuandev.simplemapproject.data.models.Line
import com.tuandev.simplemapproject.data.models.Node
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class BaseMapViewState : ViewState() {

}

@HiltViewModel
class BaseMapViewModel @Inject constructor() : BaseViewModel<BaseMapViewState>() {

    private var fakeId: Int = 0
    var currentTouchEvent: String = ""
    private var listNode: MutableList<Node> = mutableListOf()
    private var listLine: MutableList<Line> = mutableListOf()

    fun addLine(firstNodeId: Int, secondNodeId: Int, polyline: Polyline): Line {
        val id = ++fakeId
        return polyline.run {
            tag = id
            val newLine = Line(
                id = id,
                firstNodeId,
                secondNodeId,
                polyline
            )
            listLine.add(newLine)
            newLine
        }
    }

    fun addNode(marker: Marker): Node {
        val id = ++fakeId
        return marker.run {
            tag = id
            val newNode = Node(
                id = id,
                title = "",
                latitude = position.latitude,
                longitude = position.longitude,
                marker = this
            )
            listNode.add(newNode)
            newNode
        }
    }

    fun removeNode(nodeId: Int) {
        listNode.find { it.id == nodeId }?.let { node ->
            node.marker?.remove()
            listNode.remove(node)
            listLine.filter { line ->
                line.firstNodeId == node.id || line.secondNodeId == node.id
            }.forEach {
                listLine.remove(it)
                it.polyline?.remove()
            }
        }
    }

    fun removeLine(lineId: Int) {
        listLine.find { it.id == lineId }?.let { line ->
            line.polyline?.remove()
            listLine.remove(line)
        }
    }

    fun checkIfLineNotExist(firstNodeId: Int, secondNodeId: Int): Boolean =
        listLine.none {
            (it.firstNodeId == firstNodeId && it.secondNodeId == secondNodeId) ||
                    (it.firstNodeId == secondNodeId && it.secondNodeId == firstNodeId)
        }
}