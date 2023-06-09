package com.tuandev.simplemapproject.ui.splash.toolMap

import androidx.fragment.app.viewModels
import com.google.android.gms.maps.model.Polyline
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.base.BaseFragment
import com.tuandev.simplemapproject.base.map.BaseMapFragment
import com.tuandev.simplemapproject.base.map.BaseMapFragment.Companion.TouchState
import com.tuandev.simplemapproject.data.models.OptionItem
import com.tuandev.simplemapproject.databinding.FragmentToolMapBinding
import com.tuandev.simplemapproject.extension.openFragment
import com.tuandev.simplemapproject.extension.showIf
import com.tuandev.simplemapproject.widget.markerSelectedDialog.OptionItemDialog

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

                        if (vs.isToggle) {
                            when (viewModel.currentTool) {
                                ToolMapViewModel.ADD_POINT -> {
                                    mapFragment?.setCurrentTouchEvent(TouchState.DRAW_MARKER)
                                }

                                ToolMapViewModel.ADD_LINE -> {
                                    mapFragment?.startDrawLine()
                                }

                                ToolMapViewModel.FIND_ROUTE -> {
                                    mapFragment?.startFindRoute()
                                }
                            }
                        }
                    }

                    is ToolMapViewState.Undo -> {
                        viewModel.run {
                            when (currentTool) {
                                ToolMapViewModel.ADD_POINT -> {
                                    listTempNode.run {
                                        if (isNotEmpty()) {
                                            removeLast().id?.let {
                                                mapFragment?.removeNode(it)
                                            }
                                        }
                                    }
                                }

                                ToolMapViewModel.ADD_LINE -> {
                                    listTempLine.run {
                                        if (isNotEmpty()) {
                                            removeLast().id?.let {
                                                mapFragment?.removeLine(it)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun initView() {
        mapFragment = BaseMapFragment.newInstance(mapMode = BaseMapFragment.Companion.MapMode.TOOL)
        mapFragment?.let {
            openFragment(
                containerId = containerID,
                fragment = it
            )
        }
    }

    override fun initListener() {
        binding?.run {

            btnMapType.setOnClickListener {
                mapFragment?.toggleSatellite()
            }

            btnToolAddNode.setOnClickListener {
                viewModel.openTool(ToolMapViewModel.ADD_POINT)
            }

            btnToolAddLine.setOnClickListener {
                viewModel.openTool(ToolMapViewModel.ADD_LINE)
            }

            btnToolAddNodeLocation.setOnClickListener {
                mapFragment?.addCurrentLocationNode()
            }

            btnFindRoute.setOnClickListener {
                viewModel.openTool(ToolMapViewModel.FIND_ROUTE)
            }

            btnSave.setOnClickListener {
                mapFragment?.setCurrentTouchEvent(TouchState.OFF)
                viewModel.quitTool()
            }

            btnUndo.setOnClickListener {
                viewModel.undo()
            }
        }

        mapFragment?.run {
            onNodeAdded = {
                viewModel.addTempNode(it)
            }

            onLineAdded = { line ->
                viewModel.addTempLine(line)
            }

            onMarkerClick = { node ->
                OptionItemDialog(
                    optionList = listOf(
                        OptionItem(OptionItem.KEY_EDIT_MAP_ITEM, "Edit"),
                        OptionItem(OptionItem.KEY_DELETE_MAP_ITEM, "Remove")
                    ),
                    title = "Node #${node.id}"
                ).apply {
                    onItemClick = {
                        markerItemClickListener(it, node.id.toString())
                    }
                }.show(this@ToolMapFragment.childFragmentManager, null)
            }

            onPolylineClick = { polyline ->
                OptionItemDialog(
                    optionList = listOf(OptionItem(OptionItem.KEY_DELETE_MAP_ITEM, "Remove")),
                    title = "Line #${polyline.tag}"
                ).apply {
                    onItemClick = {
                        lineItemClickListener(it, polyline)
                    }
                }.show(childFragmentManager, null)
            }
        }
    }

    private val containerID = R.id.container_tool_map

    private val markerItemClickListener: (String, String) -> Unit = { key, nodeId ->
        when (key) {
            OptionItem.KEY_DELETE_MAP_ITEM -> {
                mapFragment?.removeNode(nodeId)
            }
            OptionItem.KEY_EDIT_MAP_ITEM -> {
                mapFragment?.handleUpdateNode(nodeId)
            }
        }
    }

    private val lineItemClickListener: (String, Polyline) -> Unit = { key, polyline ->
        when (key) {
            OptionItem.KEY_DELETE_MAP_ITEM -> {
                polyline.tag.toString().let { lineId ->
                    mapFragment?.removeLine(lineId)
                }
            }
        }
    }
}