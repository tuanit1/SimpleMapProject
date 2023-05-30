package com.tuandev.simplemapproject.ui.splash.suggest.routeDetail.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.tuandev.simplemapproject.base.BaseListAdapter
import com.tuandev.simplemapproject.base.BaseViewHolder
import com.tuandev.simplemapproject.data.models.RouteItem
import com.tuandev.simplemapproject.data.repositories.local.PlaceServiceRepository
import com.tuandev.simplemapproject.databinding.LayoutSuggestPlaceItemBinding
import com.tuandev.simplemapproject.di.RepositoryModule
import com.tuandev.simplemapproject.extension.gone
import com.tuandev.simplemapproject.extension.show
import com.tuandev.simplemapproject.extension.showIf
import dagger.hilt.android.EntryPointAccessors

class RouteItemAdapter(
    context: Context
) : BaseListAdapter<RouteItem, RouteItemAdapter.RouteItemViewHolder>() {

    private var placeServiceRepository: PlaceServiceRepository? = null
    var onItemClick: (Int) -> Unit = {}

    init {
        placeServiceRepository = EntryPointAccessors.fromApplication(
            context,
            RepositoryModule.RepositoryEntryPoint::class.java
        ).getPlaceServiceRepository()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteItemViewHolder {
        val binding = LayoutSuggestPlaceItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RouteItemViewHolder(binding)
    }

    inner class RouteItemViewHolder(
        private val binding: LayoutSuggestPlaceItemBinding
    ) : BaseViewHolder<RouteItem>(binding.root) {
        override fun bind(item: RouteItem) {
            binding.run {
                hideAllServiceImage()
                item.place.run {
                    llDuration.showIf(game != null)
                    llThrill.showIf(game != null)
                    if (game != null) {
                        tvPlaceName.text = "#${item.itemIndex} - ${game.name}"
                        tvDuration.text = "${game.duration}s"
                        tvThrill.text = game.thrillLevel.name
                        ivGame.show()
                    } else {
                        tvPlaceName.text = "#${item.itemIndex} - $name"
                        listService.forEach { service ->
                            when (service){
                                placeServiceRepository?.serviceFood -> ivFood.show()
                                placeServiceRepository?.serviceGuest -> ivCustomerService.show()
                                placeServiceRepository?.serviceWC -> ivWC.show()
                                placeServiceRepository?.serviceMedical -> ivMed.show()
                                placeServiceRepository?.serviceSouvenir -> ivSourvenir.show()
                                placeServiceRepository?.serviceTicket -> ivTicket.show()
                                placeServiceRepository?.serviceSightSeeing -> ivSightSeeing.show()
                            }
                        }
                    }
                }
                when (item.itemState){
                    RouteItem.NOT_VISITED -> {
                        rlOutlineCheck.isSelected = false
                        ivCheck.gone()
                        pbCurrent.gone()
                    }
                    RouteItem.SELECTED -> {
                        rlOutlineCheck.isSelected = true
                        ivCheck.gone()
                        pbCurrent.show()
                    }
                    RouteItem.VISITED -> {
                        rlOutlineCheck.isSelected = true
                        ivCheck.show()
                        pbCurrent.gone()
                    }
                }

                cardItem.setOnClickListener {
                    onItemClick(adapterPosition)
                }
            }
        }

        private fun hideAllServiceImage() {
            binding.run {
                ivGame.gone()
                ivWC.gone()
                ivCustomerService.gone()
                ivFood.gone()
                ivMed.gone()
                ivSourvenir.gone()
                ivTicket.gone()
                ivSightSeeing.gone()
            }
        }
    }
}
