package com.tuandev.simplemapproject.ui.splash.suggest

import androidx.fragment.app.viewModels
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.base.BaseFragment
import com.tuandev.simplemapproject.base.ViewState
import com.tuandev.simplemapproject.data.models.RouteItem
import com.tuandev.simplemapproject.data.models.UserFeature
import com.tuandev.simplemapproject.databinding.FragmentSuggestBinding
import com.tuandev.simplemapproject.extension.openFragment
import com.tuandev.simplemapproject.ui.splash.suggest.routeDetail.RouteDetailFragment
import com.tuandev.simplemapproject.ui.splash.suggest.routeDetail.featureQuestion.FeatureQuestionFragment
import com.tuandev.simplemapproject.ui.splash.suggest.suggestMap.SuggestMapFragment
import com.tuandev.simplemapproject.widget.ConfirmMessageDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SuggestFragment :
    BaseFragment<FragmentSuggestBinding, SuggestViewModel, ViewState>(FragmentSuggestBinding::inflate) {

    companion object {
        fun newInstance() = SuggestFragment()
    }

    override val viewModel: SuggestViewModel by viewModels()
    override val viewStateObserver: (viewState: ViewState) -> Unit = {}

    var onUserFeatureUpdatedListener: (UserFeature) -> Unit = {}
    var onSuggestRouteUpdatedListener: (List<RouteItem>) -> Unit = {}
    private var isLoadRouteFromMapFragment = true

    override fun initView() {
        openSuggestMapFragment()
    }

    override fun initListener() {
        listenOnLiveData()
    }

    private fun listenOnLiveData() {
        viewModel.run {
            mUserFeature.observe(viewLifecycleOwner) { userFeature ->
                if (userFeature != null) {
                    onUserFeatureUpdatedListener(userFeature)
                } else {
                    ConfirmMessageDialog(
                        title = "Message",
                        message = "At first, we need to know how do you want to play in Asia Park."
                    ).apply {
                        successAction = {
                            showFeatureQuestionFragment(true)
                        }
                    }.show(childFragmentManager, null)
                }
            }

            suggestRouteDao.getAll().observe(viewLifecycleOwner) { suggestList ->
                mSuggestList.run {
                    clear()
                    addAll(suggestList)
                }
                if (!isLoadRouteFromMapFragment) {
                    onSuggestRouteUpdatedListener(getSuggestList())
                }
                isLoadRouteFromMapFragment = false
            }
        }
    }

    private fun getContainerId() = R.id.container_suggest

    private fun openSuggestMapFragment() {
        openFragment(
            containerId = getContainerId(),
            fragment = SuggestMapFragment.newInstance()
        )
    }

    fun showRouteDetailFragment() {
        openFragment(
            containerId = getContainerId(),
            fragment = RouteDetailFragment.newInstance()
        )
    }

    fun showFeatureQuestionFragment(isInitFeature: Boolean) {
        openFragment(
            containerId = getContainerId(),
            fragment = FeatureQuestionFragment.newInstance().apply {
                this.isInitFeature = isInitFeature
            }
        )
    }

    fun updateUserFeature(userFeature: UserFeature) {
        viewModel.updateUserFeature(userFeature)
    }

    fun updateSuggestRouteList(suggestList: List<RouteItem>) {
        viewModel.updateSuggestList(suggestList)
    }

    fun getSaveSuggestList() = viewModel.getSuggestList()
    fun getUserFeature() = viewModel.mUserFeature.value
}