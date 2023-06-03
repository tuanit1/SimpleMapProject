package com.tuandev.simplemapproject.base.map

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.tuandev.simplemapproject.base.BaseViewModel
import com.tuandev.simplemapproject.base.ViewState
import com.tuandev.simplemapproject.data.models.ImageData
import com.tuandev.simplemapproject.data.models.Line
import com.tuandev.simplemapproject.data.models.NeighborWithDistance
import com.tuandev.simplemapproject.data.models.Node
import com.tuandev.simplemapproject.data.repositories.local.LocalRepository
import com.tuandev.simplemapproject.data.repositories.remote.FireStoreRepository
import com.tuandev.simplemapproject.extension.mapToImageData
import com.tuandev.simplemapproject.extension.mapToLine
import com.tuandev.simplemapproject.extension.mapToNode
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

        callApiFromFireStore(
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

        callApiFromFireStore(
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
        callApiFromFireStore(
            task = fireStoreRepository.deleteNode(nodeId),
            onSuccess = {
                listNode.find { it.id == nodeId }?.let { node ->
                    val deleteLines =
                        listLine.filter { line -> line.firstNodeId == node.id || line.secondNodeId == node.id }

                    if (deleteLines.isNotEmpty()) {
                        callApiFromFireStore(
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
        callApiFromFireStore(
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
        callApiFromFireStore(
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
    }

    fun checkIfLineNotExist(firstNodeId: String?, secondNodeId: String?): Boolean =
        listLine.none {
            (it.firstNodeId == firstNodeId && it.secondNodeId == secondNodeId) ||
                    (it.firstNodeId == secondNodeId && it.secondNodeId == firstNodeId)
        }

    fun fetchAllNodesAndLines() {
        callApiFromFireStore(
            task = fireStoreRepository.getAllNodes(),
            onSuccess = { nodeResult ->
                listNode.clear()
                listNode.addAll(nodeResult.map { it.mapToNode() })
                updateViewState(BaseMapViewState.GetNodesSuccess)

                callApiFromFireStore(
                    task = fireStoreRepository.getAllLines(),
                    onSuccess = { lineResult ->
                        viewModelScope.launch(Dispatchers.IO) {
                            listLine.clear()
                            listLine.addAll(lineResult.map { it.mapToLine() })
                            listNode.updateNeighbors()
                            updateViewState(BaseMapViewState.GetLinesSuccess)
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

    fun getNodeById(id: String?) = listNode.find { it.id == id }

    fun getPlaceById(id: Int?) = localRepository.listPlace.find { it.id == id }

    fun updateLineViewState(isVisible: Boolean) {
        updateViewState(BaseMapViewState.ToggleLine(isVisible))
    }

    fun uploadPlaceImage(image: ByteArray, placeId: Int) {
        uploadImage(image) { imageName, imageUrl ->
            callApiFromFireStore(
                task = fireStoreRepository.updatePlaceImage(imageName, imageUrl, placeId),
                onSuccess = {}
            )
        }
    }

    fun deletePlaceImage(imagePath: String, placeId: Int, onSuccess: () -> Unit) {
        callApiFromFireStore(
            task = fireStoreRepository.getPlaceImageByFilter(imagePath, placeId),
            onSuccess = { querySnapshot ->
                val refs = querySnapshot.map { doc -> doc.reference }
                callApiFromFireStore(
                    task = fireStoreRepository.deleteRefs(refs),
                    onSuccess = {
                        removeImage(imagePath) {
                            onSuccess()
                        }
                    }, isShowLoading = true
                )
            }, isShowLoading = true
        )
    }

    fun getPlaceImages(placeId: Int, onSuccess: (List<ImageData>) -> Unit) {
        callApiFromFireStore(
            task = fireStoreRepository.getPlaceImageList(placeId),
            onSuccess = { querySnapshot ->
                val imageList = querySnapshot.map { it.mapToImageData() }
                onSuccess(imageList)
            },
            isShowLoading = true
        )
    }

    fun getNodeByPlaceId(placeId: Int) = listNode.find { it.placeId == placeId }
}