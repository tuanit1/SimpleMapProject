package com.tuandev.simplemapproject.ui.splash.suggest

import android.location.Location
import android.os.Looper
import androidx.fragment.app.viewModels
import com.google.android.gms.location.*
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.base.BaseFragment
import com.tuandev.simplemapproject.base.ViewState
import com.tuandev.simplemapproject.data.models.RouteItem
import com.tuandev.simplemapproject.data.models.UserFeature
import com.tuandev.simplemapproject.databinding.FragmentSuggestBinding
import com.tuandev.simplemapproject.extension.log
import com.tuandev.simplemapproject.extension.openFragment
import com.tuandev.simplemapproject.extension.showToast
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

    private var isSuggestRouteUpdated = false
    private var fusedLocationClient: FusedLocationProviderClient? = null
    var onLocationUpdate: (Location) -> Unit = {}
    var onUserFeatureUpdatedListener: (UserFeature) -> Unit = {}
    var invokeSuggestRouteUpdate: () -> Unit = {}

    override fun initView() {
        parentActivity?.run {
            checkLocationPermission {
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this).apply {
                    lastLocation.addOnSuccessListener { location ->
                        viewModel.mCurrentLocation.value = location
                    }
                }
            }
        }
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
                        positiveAction = {
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
            }

            mCurrentLocation.observe(viewLifecycleOwner) { location ->
                location?.let(onLocationUpdate)
            }
        }
    }

    private fun getLocationRequest() = LocationRequest
        .Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
        .setMinUpdateDistanceMeters(5f).build()

    private fun startLocationUpdates() {
        parentActivity?.checkLocationPermission {
            fusedLocationClient?.requestLocationUpdates(
                getLocationRequest(),
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient?.removeLocationUpdates(locationCallback)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                log("current location: lat -> ${location.latitude}, lng -> ${location.longitude}")
                viewModel.mCurrentLocation.value = location
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
        isSuggestRouteUpdated = true
        viewModel.updateSuggestList(suggestList)
    }

    fun checkIfNeedUpdateMap() {
        if (isSuggestRouteUpdated) {
            isSuggestRouteUpdated = false
            context?.showToast("Updating suggest route...")
            invokeSuggestRouteUpdate()
        }
    }

    fun getSaveSuggestList() = viewModel.getSuggestList()
    fun getUserFeature() = viewModel.mUserFeature.value

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }
}