package com.tuandev.simplemapproject.ui.toolmap

import com.google.android.gms.maps.model.Marker
import com.tuandev.simplemapproject.base.BaseViewModel
import com.tuandev.simplemapproject.base.ViewState

sealed class ToolMapViewState : ViewState() {
    class ToggleTool(val isToggle: Boolean, val toolKey: String = "") : ToolMapViewState()

}

class ToolMapViewModel : BaseViewModel<ToolMapViewState>() {

    var listUndo: MutableList<Any> = mutableListOf()

    fun openTool(toolKey: String) {
        updateViewState(ToolMapViewState.ToggleTool(true, toolKey))
    }

    fun quitTool() {
        updateViewState(ToolMapViewState.ToggleTool(false))
    }

    fun addMarker(marker: Marker) {
        listUndo.add(marker)
    }

    fun undo() {
        if(listUndo.isNotEmpty()){
            when (val item = listUndo.last()){
                is Marker -> {
                    item.remove()
                }
            }
            listUndo.removeLast()
        }
    }
}