package com.tuandev.simplemapproject.base.map

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.tuandev.simplemapproject.base.BaseViewModel
import com.tuandev.simplemapproject.base.ViewState
import com.tuandev.simplemapproject.data.models.Line
import com.tuandev.simplemapproject.data.models.NeighborWithDistance
import com.tuandev.simplemapproject.data.models.Node
import com.tuandev.simplemapproject.data.repositories.local.LocalRepository
import com.tuandev.simplemapproject.data.repositories.remote.FireStoreRepository
import com.tuandev.simplemapproject.extension.toDoubleOrNull
import com.tuandev.simplemapproject.extension.toFloatOrNull
import com.tuandev.simplemapproject.extension.toIntToNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BaseMapViewState : ViewState() {
    class AddNodeSuccess(val newNode: Node) : BaseMapViewState()

    class AddLineSuccess(val newLine: Line) : BaseMapViewState()

    object GetNodesSuccess : BaseMapViewState()

    object GetLinesSuccess : BaseMapViewState()

    class ToggleLine(val isVisible: Boolean) : BaseMapViewState()

    class NodePlaceUpdateSuccess(val marker: Marker?, val nodeId: String) : BaseMapViewState()

}

@HiltViewModel
class BaseMapViewModel @Inject constructor(
    private val fireStoreRepository: FireStoreRepository,
    private val localRepository: LocalRepository,
) : BaseViewModel<BaseMapViewState>() {


    var currentTouchEvent: MutableLiveData<String> = MutableLiveData()
    var listNode: MutableList<Node> = mutableListOf()
    var listLine: MutableList<Line> = mutableListOf()

    fun addLine(firstNodeId: String, secondNodeId: String, distance: Float?, polyline: Polyline) {
        val newLine = polyline.run {
            Line(
                firstNodeId = firstNodeId,
                secondNodeId = secondNodeId,
                polyline = polyline,
                distance = distance
            )
        }

        fetchFromFireStore(
            task = fireStoreRepository.addLine(newLine),
            onSuccess = {
                val lineId = it.id
                newLine.polyline?.tag = lineId
                newLine.id = lineId
                listLine.add(newLine)
                listNode.updateNeighbors()
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
                                listNode.updateNeighbors()
                            }
                        )
                    } else {
                        node.marker?.remove()
                        listNode.remove(node)
                    }
                }
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
                listNode.updateNeighbors()
            }
        )
    }

    fun updateNodePlace(nodeId: String, placeId: Int?, onUpdateSuccess: () -> Unit) {
        val placeExisted = listNode.mapNotNull { node -> node.placeId }
            .any { pId -> pId == placeId }

        if (!placeExisted) {
            fetchFromFireStore(
                task = fireStoreRepository.updateNodePlace(nodeId, placeId),
                onSuccess = {
                    getNodeById(nodeId)?.placeId = placeId
                    updateViewState(
                        BaseMapViewState.NodePlaceUpdateSuccess(
                            marker = getNodeById(nodeId)?.marker,
                            nodeId = nodeId
                        )
                    )
                    onUpdateSuccess()
                }
            )
        } else {
            showErrorPopup("This place has been assigned to a node")
        }
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
                        viewModelScope.launch(Dispatchers.IO) {
                            listLine.clear()
                            listLine.addAll(lineResult.map { it.mapToLine() })
                            listNode.updateNeighbors()
                            updateViewState(BaseMapViewState.GetLinesSuccess)
                        }
                    }
                )
            }
        )
    }

    private fun MutableList<Node>.updateNeighbors() {
        forEach { node ->
            node.neighbors = listLine.mapNotNull { line ->
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
        }
    }

    fun getNodeById(id: String?) = listNode.find { it.id == id }

    private fun QueryDocumentSnapshot.mapToNode(): Node {
        return Node(
            id = id,
            latitude = data["latitude"]?.toDoubleOrNull() ?: 0.0,
            longitude = data["longitude"]?.toDoubleOrNull() ?: 0.0,
            placeId = data["placeId"]?.toIntToNull()
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

    fun getPlaceById(id: Int?) = localRepository.listPlace.find { it.id == id }

    fun updateLineViewState(isVisible: Boolean) {
        updateViewState(BaseMapViewState.ToggleLine(isVisible))
    }
}