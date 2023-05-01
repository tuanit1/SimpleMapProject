package com.tuandev.simplemapproject.ui.toolmap

import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.tuandev.simplemapproject.base.BaseViewModel
import com.tuandev.simplemapproject.base.ViewState

sealed class ToolMapViewState : ViewState() {
    class ToggleTool(val isToggle: Boolean) : ToolMapViewState()
}

class ToolMapViewModel : BaseViewModel<ToolMapViewState>() {

    companion object {
        const val ADD_POINT = "add_point"
        const val ADD_LINE = "add_line"
    }

    var currentTool: String = ""
    private var listTempNode: MutableList<Marker> = mutableListOf()
    private var listTempLine: MutableList<Polyline> = mutableListOf()

    fun openTool(toolKey: String) {
        currentTool = toolKey
        updateViewState(ToolMapViewState.ToggleTool(true))
    }

    fun quitTool() {
        updateViewState(ToolMapViewState.ToggleTool(false))
    }

    fun addMarker(marker: Marker) {
        listTempNode.add(marker)
    }

    fun addLine(polyline: Polyline){
        listTempLine.add(polyline)
    }

    fun undo() {
        when (currentTool) {
            ADD_POINT -> {
                if(listTempNode.isNotEmpty()){
                    listTempNode.last().remove()
                    listTempNode.removeLast()
                }
            }

            ADD_LINE -> {
                if(listTempLine.isNotEmpty()){
                    listTempLine.last().remove()
                    listTempLine.removeLast()
                }
            }
        }
    }
}