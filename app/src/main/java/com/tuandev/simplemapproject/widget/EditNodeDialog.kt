package com.tuandev.simplemapproject.widget

import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.base.BaseDialogFragment
import com.tuandev.simplemapproject.data.models.Node
import com.tuandev.simplemapproject.data.models.OptionItem
import com.tuandev.simplemapproject.data.repositories.local.LocalRepository
import com.tuandev.simplemapproject.databinding.DialogEditNodeBinding
import com.tuandev.simplemapproject.extension.gone
import com.tuandev.simplemapproject.extension.showIf
import com.tuandev.simplemapproject.widget.markerselecteddialog.OptionItemDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
data class EditNodeDialog(
    var node: Node,
    val listNode: MutableList<Node>
) : BaseDialogFragment<DialogEditNodeBinding>(DialogEditNodeBinding::inflate) {

    @Inject
    lateinit var localRepository: LocalRepository
    var onNodeUpdate: (Int?, () -> Unit) -> Unit = { _, _ -> }

    override fun initView() {
        binding?.run {
            llParent.run {
                viewTreeObserver.addOnGlobalLayoutListener {
                    checkReachedMaxSize(measuredHeight)
                }
            }
            updatePlaceViewState()
        }
    }

    override fun initListener() {
        binding?.run {
            node.run {
                tvTitle.text = "Node #${id}"
                tvLat.text = latitude.toString()
                tvLon.text = longitude.toString()

                tvPlace.setOnClickListener {
                    OptionItemDialog(
                        title = "Select a place",
                        optionList = getPlaceList(),
                        isSearchEnable = true
                    ).apply {
                        onItemClick = {
                            val placeId = if (it.toInt() != -1) it.toInt() else null
                            onNodeUpdate(placeId) {
                                updatePlaceViewState()
                            }
                        }
                    }.show(childFragmentManager, null)
                }
            }
        }
    }

    private fun getPlaceList() = localRepository.listPlace.filterNot { place ->
        listNode.mapNotNull { it.placeId }.contains(place.id)
    }.map { place ->
        if (place.game != null) {
            OptionItem(place.id.toString(), "Game: ${place.game.name}")
        } else {
            OptionItem(place.id.toString(), "Place: ${place.name}")
        }
    }.toMutableList().apply { add(0, OptionItem("-1", "No item")) }

//    private fun getNodeById(id: String) = listNode.find { it.id == id }

    private fun findPlaceById(id: Int?) = localRepository.listPlace.find { it.id == id }

    private fun updatePlaceViewState() {
        binding?.run {
            tvZone.showIf(node.placeId != null)
            if (node.placeId != null) {
                val place = findPlaceById(node.placeId)
                tvPlace.text = if (place?.game != null) "${place.game.name} (Game)" else place?.name
                tvZone.text = place?.zone?.name
                csGame.showIf(place?.game != null)

                place?.game?.run {
                    tvThrill.text = thrillLevel.name
                    tvDuration.text = "${duration}s"
                    tvAvailable.text = isAvailable.toString()
                }
            } else {
                tvPlace.text = getString(R.string.no_place_selected)
                csGame.gone()
            }
        }
    }

}