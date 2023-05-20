package com.tuandev.simplemapproject.data.repositories.remote

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tuandev.simplemapproject.data.models.Line
import com.tuandev.simplemapproject.data.models.Node

class FireStoreRepository {

    companion object {
        const val nodeCollection = "Nodes"
        const val lineCollection = "Lines"
        const val placeImageCollection = "PlaceImage"
    }

    private val db = Firebase.firestore

    fun addNode(node: Node) = db.collection(nodeCollection).add(node.convertToHashMap())

    fun deleteNode(nodeId: String) = db.collection(nodeCollection).document(nodeId).delete()

    fun deleteLines(listIds: List<String>) = db.runTransaction { transition ->
        listIds.forEach { lineId ->
            transition.delete(db.collection(lineCollection).document(lineId))
        }
    }

    fun addLine(line: Line) = db.collection(lineCollection).add(line.convertToHashMap())

    fun deleteLine(lineId: String) = db.collection(lineCollection).document(lineId).delete()

    fun getAllNodes() = db.collection(nodeCollection).get()

    fun getAllLines() = db.collection(lineCollection).get()

    fun updateNodePlace(nodeId: String, placeId: Int?) =
        db.collection(nodeCollection).document(nodeId)
            .update("placeId", placeId)

    fun updatePlaceImage(imageName: String, imageUrl: String, placeId: Int) =
        db.collection(placeImageCollection).add(
            hashMapOf<String, Any>(
                "placeId" to placeId,
                "imageName" to imageName,
                "imageUrl" to imageUrl
            )
        )

    fun getPlaceImageByFilter(imageName: String, placeId: Int) = db.collection(placeImageCollection)
        .whereEqualTo("imageName", imageName)
        .whereEqualTo("placeId", placeId)
        .get()

    fun getPlaceImageList(placeId: Int) = db.collection(placeImageCollection)
        .whereEqualTo("placeId", placeId)
        .get()

    fun deleteRefs(refs: List<DocumentReference>) = db.runTransaction { transition ->
        refs.forEach {
            transition.delete(it)
        }
    }
}