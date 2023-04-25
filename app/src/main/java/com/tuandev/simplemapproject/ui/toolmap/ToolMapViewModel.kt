package com.tuandev.simplemapproject.ui.toolmap

import com.tuandev.simplemapproject.base.BaseViewModel
import com.tuandev.simplemapproject.base.ViewState

sealed class ToolMapViewState: ViewState() {
    class ToggleTool(val isToggle: Boolean, val toolKey: String = ""): ToolMapViewState()
}

class ToolMapViewModel: BaseViewModel<ToolMapViewState>() {
    fun openTool(toolKey: String) {
        updateViewState(ToolMapViewState.ToggleTool(true, toolKey))
    }

    fun quitTool() {
        updateViewState(ToolMapViewState.ToggleTool(false))
    }
}