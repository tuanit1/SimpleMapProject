package com.tuandev.simplemapproject.base.map

import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.base.BaseFragment
import com.tuandev.simplemapproject.databinding.FragmentBaseMapBinding
import com.tuandev.simplemapproject.extension.addFragment
import com.tuandev.simplemapproject.util.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BaseMapFragment :
    BaseFragment<FragmentBaseMapBinding, BaseMapViewModel, BaseMapViewState>(FragmentBaseMapBinding::inflate) {

    companion object {
        fun newInstance() = BaseMapFragment()
    }

    private var supportMapFragment: SupportMapFragment? = null
    private var mMap: GoogleMap? = null

    override val viewModel: BaseMapViewModel by viewModels()

    override val viewStateObserver: (BaseMapViewState) -> Unit = { viewState ->
        when (viewState) {
            is BaseMapViewState.Tuna -> {
                Toast.makeText(context, "view state working", Toast.LENGTH_SHORT).show()
            }
            is BaseMapViewState.ShiBa -> {
                Toast.makeText(context, viewState.text, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    override fun initView() {
        initMap()
        handleMap()
    }

    private fun initMap() {
        supportMapFragment = SupportMapFragment.newInstance(
            GoogleMapOptions()
                .mapType(GoogleMap.MAP_TYPE_NORMAL)
                .rotateGesturesEnabled(false)
                .compassEnabled(false)
                .minZoomPreference(16f)
        )

        supportMapFragment?.let { fragment ->
            addFragment(
                containerId = getContainerId(),
                fragment = fragment,
                addToBackStack = true
            )
        }
    }

    private fun getContainerId() = R.id.fragment_map_container

    private fun handleMap() {
        supportMapFragment?.getMapAsync { map ->
            mMap = map
            setMapStyle()
            drawBorderLine()
        }
    }

    private fun setMapStyle() {
        mMap?.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                requireContext(),
                R.raw.style_json
            )
        )
    }

    private fun drawBorderLine() {
        mMap?.run {
            Constants.AsiaParkMap.run {
                setLatLngBoundsForCameraTarget(bound)
                val rectOptions = PolygonOptions().add(nwLatLng, neLatLng, seLatLng, swLatLng)
                addPolygon(rectOptions)

                moveCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.builder()
                            .target(swLatLng)
                            .bearing(-7f)
                            .build()
                    )
                )
            }
        }
    }

}