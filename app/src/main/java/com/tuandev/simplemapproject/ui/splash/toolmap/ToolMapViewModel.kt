package com.tuandev.simplemapproject.ui.splash.toolmap

import com.tuandev.simplemapproject.base.BaseViewModel
import com.tuandev.simplemapproject.base.ViewState
import com.tuandev.simplemapproject.data.models.Line
import com.tuandev.simplemapproject.data.models.Node

sealed class ToolMapViewState : ViewState() {
    class ToggleTool(val isToggle: Boolean) : ToolMapViewState()
    object Undo: ToolMapViewState()
}

class ToolMapViewModel : BaseViewModel<ToolMapViewState>() {

    companion object {
        const val ADD_POINT = "add_point"
        const val ADD_LINE = "add_line"
        const val FIND_ROUTE = "find_route"
    }

    var currentTool: String = ""
    val listTempNode: MutableList<Node> = mutableListOf()
    val listTempLine: MutableList<Line> = mutableListOf()
    fun openTool(toolKey: String) {
        currentTool = toolKey
        updateViewState(ToolMapViewState.ToggleTool(true))
    }

    fun quitTool() {
        listTempLine.clear()
        listTempNode.clear()
        updateViewState(ToolMapViewState.ToggleTool(false))
    }

    fun addTempNode(node: Node) {
        listTempNode.add(node)
    }

    fun addTempLine(line: Line) {
        listTempLine.add(line)
    }

    fun undo() {
        updateViewState(ToolMapViewState.Undo)
    }
}