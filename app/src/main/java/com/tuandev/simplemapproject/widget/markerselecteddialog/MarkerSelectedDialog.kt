package com.tuandev.simplemapproject.widget.markerselecteddialog

import android.util.Log
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.tuandev.simplemapproject.base.BaseDialogFragment
import com.tuandev.simplemapproject.data.models.OptionItem
import com.tuandev.simplemapproject.databinding.DialogMarkerSelectedBinding

class MarkerSelectedDialog(private val optionList: List<OptionItem>):
    BaseDialogFragment<DialogMarkerSelectedBinding>(DialogMarkerSelectedBinding::inflate) {

    private var optionAdapter: DialogChoiceAdapter? = null
    var onItemClick: (String) -> Unit = {}

    override fun initView() {
        optionAdapter = DialogChoiceAdapter()

        binding?.rvListOption?.run {
            adapter = optionAdapter
            layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator()
        }

        optionAdapter?.submitList(optionList)
    }

    override fun initListener() {
        binding?.run {
            ivCancel.setOnClickListener {
                dismiss()
            }

            optionAdapter?.onItemClick = {
                onItemClick(it.key)
                dismiss()
            }
        }
    }
}