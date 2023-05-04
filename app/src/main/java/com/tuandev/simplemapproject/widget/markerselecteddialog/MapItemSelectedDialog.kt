package com.tuandev.simplemapproject.widget.markerselecteddialog

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.tuandev.simplemapproject.base.BaseDialogFragment
import com.tuandev.simplemapproject.data.models.OptionItem
import com.tuandev.simplemapproject.databinding.DialogMapItemSelectedBinding
class MapItemSelectedDialog(private val optionList: List<OptionItem>):
    BaseDialogFragment<DialogMapItemSelectedBinding>(DialogMapItemSelectedBinding::inflate) {

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
            optionAdapter?.onItemClick = {
                onItemClick(it.key)
                dismiss()
            }
        }
    }
}