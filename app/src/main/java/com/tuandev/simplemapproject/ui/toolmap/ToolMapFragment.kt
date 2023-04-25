package com.tuandev.simplemapproject.ui.toolmap

import androidx.fragment.app.viewModels
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.base.BaseFragment
import com.tuandev.simplemapproject.base.map.BaseMapFragment
import com.tuandev.simplemapproject.databinding.FragmentToolMapBinding
import com.tuandev.simplemapproject.extension.replaceFragment
import com.tuandev.simplemapproject.extension.showIf

class ToolMapFragment :
    BaseFragment<FragmentToolMapBinding, ToolMapViewModel, ToolMapViewState>(FragmentToolMapBinding::inflate) {

    companion object {

        const val ADD_POINT_LINE = "add_point_line"
        const val SET_BORDER_TOOL = "set_border_tool"

        @JvmStatic
        fun newInstance() = ToolMapFragment()
    }

    private var mapFragment: BaseMapFragment? = null

    override val viewModel: ToolMapViewModel by viewModels()
    override val viewStateObserver: (viewState: ToolMapViewState) -> Unit = { vs ->
        binding?.run {
            when (vs) {
                is ToolMapViewState.ToggleTool -> {
                    llTool.showIf(!vs.isToggle)
                    llEdit.showIf(vs.isToggle)
                    when (vs.toolKey) {
                        ADD_POINT_LINE -> {

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
            btnToolAddLine.setOnClickListener {
                viewModel.openTool(ADD_POINT_LINE)
            }

            btnSetBorder.setOnClickListener {
                viewModel.openTool(SET_BORDER_TOOL)
            }

            btnQuit.setOnClickListener {
                viewModel.quitTool()
            }
        }
    }

    private val containerID = R.id.container_tool_map
}