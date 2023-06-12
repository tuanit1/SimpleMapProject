package com.tuandev.simplemapproject.widget.markerSelectedDialog

import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.tuandev.simplemapproject.base.BaseDialogFragment
import com.tuandev.simplemapproject.data.models.OptionItem
import com.tuandev.simplemapproject.databinding.DialogOptionItemBinding
import com.tuandev.simplemapproject.extension.show
import com.tuandev.simplemapproject.extension.showIf

class OptionItemDialog(
    private val optionList: List<OptionItem>,
    private val title: String? = null,
    private val isSearchEnable: Boolean = false
) :
    BaseDialogFragment<DialogOptionItemBinding>(DialogOptionItemBinding::inflate) {

    private var optionAdapter: DialogChoiceAdapter? = null
    var onItemClick: (String) -> Unit = {}

    override fun initView() {
        optionAdapter = DialogChoiceAdapter()


        binding?.run {
            csParent.run {
                viewTreeObserver.addOnGlobalLayoutListener {
                    checkReachedMaxSize(measuredHeight)
                }
            }

            rvListOption.run {
                adapter = optionAdapter
                layoutManager = LinearLayoutManager(context)
                itemAnimator = DefaultItemAnimator()
                addItemDecoration(
                    DividerItemDecoration(
                        requireContext(),
                        DividerItemDecoration.VERTICAL
                    )
                )
            }

            rlSearch.showIf(isSearchEnable)
            edtSearch.doAfterTextChanged { editable ->
                optionAdapter?.submitList(optionList.filter {
                    it.title.lowercase().contains(editable.toString().lowercase())
                })
            }
        }

        optionAdapter?.submitList(optionList)
    }

    override fun initListener() {
        binding?.run {
            optionAdapter?.onItemClick = {
                onItemClick(it.key)
                dismiss()
            }

            title?.run {
                tvHeader.show()
                tvHeader.text = title
            }
        }
    }
}