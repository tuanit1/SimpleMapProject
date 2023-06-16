package com.tuandev.simplemapproject.widget.placeInfoDialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tuandev.simplemapproject.data.models.ActionItem
import com.tuandev.simplemapproject.databinding.DialogBottomPlaceInfoBinding
import com.tuandev.simplemapproject.extension.gone
import com.tuandev.simplemapproject.extension.show
import com.tuandev.simplemapproject.extension.showIf
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaceInfoDialog : BottomSheetDialogFragment() {

    private var binding: DialogBottomPlaceInfoBinding? = null
    private val viewModel: PlaceInfoDialogViewModel by viewModels()
    private var imageInfoAdapter: ImageInfoAdapter? = null
    private var mActionItem: ActionItem? = null

    companion object {
        private const val KEY_PLACE_ID = "key_place_id"
        fun newInstance(placeId: Int, actionItem: ActionItem) = PlaceInfoDialog().apply {
            mActionItem = actionItem
            arguments = Bundle().apply {
                putInt(KEY_PLACE_ID, placeId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getInt(KEY_PLACE_ID)?.let { placeId ->
            viewModel.run {
                updatePlace(placeId)
                loadPlaceImages()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogBottomPlaceInfoBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listenLiveData()
        initView()
    }

    private fun initView() {
        imageInfoAdapter = ImageInfoAdapter()
        binding?.run {
            rvPhotos.run {
                adapter = imageInfoAdapter
                layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                itemAnimator = DefaultItemAnimator()
            }

            loadActionButton()
        }
    }

    fun updateActionItem(item: ActionItem) {
        mActionItem = item
        loadActionButton()
    }

    private fun loadActionButton() {
        binding?.run {
            btnActionButtons.setOnClickListener {
                dismiss()
                mActionItem?.action?.invoke()
            }
            tvActionTitle.text = mActionItem?.title
        }
    }

    private fun listenLiveData() {
        viewModel.run {
            mPlace.observe(viewLifecycleOwner) { place ->
                binding?.run {
                    tvPlaceName.text = place.name
                    tvZone.text = place.zone.name
                    llGameInfo.showIf(place.game != null)

                    if (place.game != null) {
                        tvPlaceName.text = place.game.name
                        tvThrillLevel.text = place.game.thrillLevel.name
                        tvDuration.text = "${place.game.duration}s"
                        tvStatus.text = if (place.game.isAvailable) "Open" else "Closed"
                    }
                }
            }

            mImageList.observe(viewLifecycleOwner) { listImage ->
                binding?.run {
                    pbLoadingImage.gone()
                    rvPhotos.show()
                    rvPhotos.showIf(listImage.isNotEmpty())
                    tvPhotoEmpty.showIf(listImage.isEmpty())
                    imageInfoAdapter?.submitList(listImage)
                }
            }
        }
    }

    fun updatePlace(placeId: Int) {
        viewModel.run {
            binding?.run {
                pbLoadingImage.show()
                rvPhotos.gone()
            }
            updatePlace(placeId)
            loadPlaceImages()
        }
    }
}