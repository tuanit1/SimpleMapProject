package com.tuandev.simplemapproject.ui.toolmap

import androidx.fragment.app.viewModels
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.base.BaseFragment
import com.tuandev.simplemapproject.base.map.BaseMapFragment
import com.tuandev.simplemapproject.base.map.BaseMapFragment.Companion.TouchEvent
import com.tuandev.simplemapproject.databinding.FragmentToolMapBinding
import com.tuandev.simplemapproject.extension.replaceFragment
import com.tuandev.simplemapproject.extension.showIf

class ToolMapFragment :
    BaseFragment<FragmentToolMapBinding, ToolMapViewModel, ToolMapViewState>(FragmentToolMapBinding::inflate) {

    companion object {
        @JvmStatic
        fun newInstance() = ToolMapFragment()
    }

    private var mapFragment: BaseMapFragment? = null

    override val viewModel: ToolMapViewModel by viewModels()
    override val viewStateObserver: (viewState: ToolMapViewState) -> Unit = { vs ->
        binding?.run {
            viewModel.run {
                when (vs) {
                    is ToolMapViewState.ToggleTool -> {
                        llTool.showIf(!vs.isToggle)
                        llEdit.showIf(vs.isToggle)
                        when (viewModel.currentTool) {
                            ToolMapViewModel.ADD_POINT -> {
                                mapFragment?.setCurrentTouchEvent(TouchEvent.DRAW_MARKER)
                            }
                            ToolMapViewModel.ADD_LINE -> {
                                mapFragment?.startDrawLine()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun initView() {
        mapFragment = BaseMapFragment.newInstance()
        mapFragment?.let {
            replaceFragment(
                containerId = containerID,
                fragment = it,
                addToBackStack = false
            )
        }
    }

    override fun initListener() {
        binding?.run {
            btnToolAddNode.setOnClickListener {
                viewModel.openTool(ToolMapViewModel.ADD_POINT)
            }

            btnToolAddLine.setOnClickListener {
                viewModel.openTool(ToolMapViewModel.ADD_LINE)
            }

            btnQuit.setOnClickListener {
                mapFragment?.setCurrentTouchEvent(TouchEvent.OFF)
                viewModel.quitTool()
            }

            btnUndo.setOnClickListener {
                viewModel.undo()
            }
        }

        mapFragment?.run {
            onMarkerDrawn = {
                viewModel.addMarker(it)
            }

            onLineDrawn = {
                viewModel.addLine(it)
            }
        }
    }

    private val containerID = R.id.container_tool_map
}