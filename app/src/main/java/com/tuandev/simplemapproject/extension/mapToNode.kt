package com.tuandev.simplemapproject.extension

import com.google.firebase.firestore.QueryDocumentSnapshot
import com.tuandev.simplemapproject.data.models.ImageData
import com.tuandev.simplemapproject.data.models.Line
import com.tuandev.simplemapproject.data.models.Node

fun QueryDocumentSnapshot.mapToNode(): Node {
    return Node(
        id = id,
        latitude = data["latitude"]?.toDoubleOrNull() ?: 0.0,
        longitude = data["longitude"]?.toDoubleOrNull() ?: 0.0,
        placeId = data["placeId"]?.toIntOrNull()
    )
}

fun QueryDocumentSnapshot.mapToLine(): Line {
    return Line(
        id = id,
        firstNodeId = data["firstNodeId"].toString(),
        secondNodeId = data["secondNodeId"].toString(),
        distance = data["distance"]?.toFloatOrNull()
    )
}

fun QueryDocumentSnapshot.mapToImageData(): ImageData {
    return ImageData(
        name = data["imageName"].toString(),
        url = data["imageUrl"].toString(),
        placeId = data["placeId"]?.toIntOrNull()
    )
}