package com.tuandev.simplemapproject.base.map

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.tuandev.simplemapproject.base.BaseViewModel
import com.tuandev.simplemapproject.base.ViewState
import com.tuandev.simplemapproject.data.models.Line
import com.tuandev.simplemapproject.data.models.Node
import com.tuandev.simplemapproject.data.repositories.FireStoreRepository
import com.tuandev.simplemapproject.extension.toDoubleOrNull
import com.tuandev.simplemapproject.extension.toFloatOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class BaseMapViewState : ViewState() {
    class AddNodeSuccess(val newNode: Node) : BaseMapViewState()

    class AddLineSuccess(val newLine: Line) : BaseMapViewState()

    object GetNodesSuccess : BaseMapViewState()

    object GetLinesSuccess : BaseMapViewState()

    object RemoveNodeSuccess : BaseMapViewState()
}

@HiltViewModel
class BaseMapViewModel @Inject constructor(
    private val fireStoreRepository: FireStoreRepository
) : BaseViewModel<BaseMapViewState>() {


    var currentTouchEvent: MutableLiveData<String> = MutableLiveData()
    var listNode: MutableList<Node> = mutableListOf()
    var listLine: MutableList<Line> = mutableListOf()

    fun addLine(firstNodeId: String, secondNodeId: String, polyline: Polyline) {
        val newLine = polyline.run {
            Line(
                firstNodeId = firstNodeId,
                secondNodeId = secondNodeId,
                polyline = polyline
            )
        }

        fetchFromFireStore(
            task = fireStoreRepository.addLine(newLine),
            onSuccess = {
                val lineId = it.id
                newLine.polyline?.tag = lineId
                newLine.id = lineId
                listLine.add(newLine)
                updateViewState(BaseMapViewState.AddLineSuccess(newLine))
            }
        )
    }

    fun addNode(marker: Marker) {
        val newNode = marker.run {
            Node(
                latitude = position.latitude,
                longitude = position.longitude,
                marker = this
            )
        }

        fetchFromFireStore(
            task = fireStoreRepository.addNode(newNode),
            onSuccess = {
                val nodeId = it.id
                newNode.marker?.tag = nodeId
                newNode.id = nodeId
                listNode.add(newNode)
                updateViewState(BaseMapViewState.AddNodeSuccess(newNode))
            }
        )
    }

    fun removeNode(nodeId: String) {
        fetchFromFireStore(
            task = fireStoreRepository.deleteNode(nodeId),
            onSuccess = {
                listNode.find { it.id == nodeId }?.let { node ->
                    val deleteLines =
                        listLine.filter { line -> line.firstNodeId == node.id || line.secondNodeId == node.id }

                    if (deleteLines.isNotEmpty()) {
                        fetchFromFireStore(
                            task = fireStoreRepository.deleteLines(deleteLines.map { it.id ?: "" }),
                            onSuccess = {
                                deleteLines.forEach {
                                    listLine.remove(it)
                                    it.removePolyline()
                                }
                                node.removeMarker()
                                listNode.remove(node)
                            }
                        )
                    } else {
                        node.marker?.remove()
                        listNode.remove(node)
                    }
                }
                updateViewState(BaseMapViewState.RemoveNodeSuccess)
            }
        )
    }

    fun removeLine(lineId: String) {
        fetchFromFireStore(
            task = fireStoreRepository.deleteLine(lineId),
            onSuccess = {
                listLine.find { it.id == lineId }?.let { line ->
                    line.removePolyline()
                    listLine.remove(line)
                }
            }
        )
    }

    fun checkIfLineNotExist(firstNodeId: String?, secondNodeId: String?): Boolean =
        listLine.none {
            (it.firstNodeId == firstNodeId && it.secondNodeId == secondNodeId) ||
                    (it.firstNodeId == secondNodeId && it.secondNodeId == firstNodeId)
        }

    fun getAllNodesAndLines() {
        fetchFromFireStore(
            task = fireStoreRepository.getAllNodes(),
            onSuccess = { nodeResult ->
                listNode.clear()
                listNode.addAll(nodeResult.map { it.mapToNode() })

                updateViewState(BaseMapViewState.GetNodesSuccess)

                fetchFromFireStore(
                    task = fireStoreRepository.getAllLines(),
                    onSuccess = { lineResult ->
                        listLine.clear()
                        listLine.addAll(lineResult.map { it.mapToLine() })
                        updateViewState(BaseMapViewState.GetLinesSuccess)
                    }
                )
            }
        )
    }

    fun getNodeById(id: String?) = listNode.find { it.id == id }

    private fun QueryDocumentSnapshot.mapToNode(): Node {
        return Node(
            id = id,
            title = data["title"].toString(),
            latitude = data["latitude"]?.toDoubleOrNull() ?: 0.0,
            longitude = data["longitude"]?.toDoubleOrNull() ?: 0.0
        )
    }

    private fun QueryDocumentSnapshot.mapToLine(): Line {
        return Line(
            id = id,
            firstNodeId = data["firstNodeId"].toString(),
            secondNodeId = data["secondNodeId"].toString(),
            distance = data["distance"]?.toFloatOrNull()
        )
    }
}