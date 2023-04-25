package com.tuandev.simplemapproject.ui.maptool

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.databinding.FragmentMapToolBinding
import com.tuandev.simplemapproject.extension.addFragment
import com.tuandev.simplemapproject.ui.base.BaseFragment
import com.tuandev.simplemapproject.util.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapToolFragment : BaseFragment<FragmentMapToolBinding, MapToolViewModel>(FragmentMapToolBinding::inflate) {


    private var supportMapFragment: SupportMapFragment? = null
    private var currentZoomLevel: Float = 0F
    private var mapBound = LatLngBounds(
        LatLng(16.03803, 108.22535),
        LatLng(16.04429,108.22928)
    )

    companion object {
        fun newInstance() = MapToolFragment()
    }

    override fun initView() {
        supportMapFragment = SupportMapFragment.newInstance(
            GoogleMapOptions()
                .mapType(GoogleMap.MAP_TYPE_NORMAL)
                .rotateGesturesEnabled(false)
                .compassEnabled(false)
                .minZoomPreference(17f)
        )
        supportMapFragment?.let { fragment ->
            addFragment(
                containerId = getContainerId(),
                fragment = fragment,
                addToBackStack = true
            )
        }
        handleMap()

        handleListener()
    }

    private fun handleListener() {
        binding?.run {
            btnAddPoint.setOnClickListener {
                viewModel.showOut {
                    Toast.makeText(context, "zzzzzzzzzz", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getContainerId() = R.id.fragment_map_container

    private fun handleMap() {
        supportMapFragment?.getMapAsync { map ->
//            val kyoto = LatLng(16.043281545784357, 108.22592563424949);
//
//            googleMap.addMarker(
//                MarkerOptions()
//                    .position(kyoto)
//                    .title("Kyoto")
//            )
//
//            googleMap.moveCamera(CameraUpdateFactory.newLatLng(kyoto))
//            googleMap.moveCamera(CameraUpdateFactory.zoomTo(20f))

//            map.setOnCameraMoveListener {
//                if(currentZoomLevel != map.cameraPosition.zoom){
//                    currentZoomLevel = map.cameraPosition.zoom
//                    Log.e("zzzzz", "zoom level change: ${map.cameraPosition.zoom}")
//                }
//
//            }

//            val pos1 = LatLng(40.0617348930446,-73.860182762146)
//            val pos2 = LatLng(40.15459323422441,-73.75683523714542)
//            val myBound = LatLngBounds(pos1, pos2)
//
//            map.addMarker(
//                MarkerOptions().position(pos1)
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
//            )
//
//            map.addMarker(
//                MarkerOptions().position(pos2)
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
//            )
//
//            map.setOnMapClickListener {
//                Log.e("zzzzz", "${map.cameraPosition.target}")
//            }
//
//            val newarkMap = GroundOverlayOptions()
//                .image(BitmapDescriptorFactory.fromResource(R.drawable.khoi))
//                .positionFromBounds(myBound)
//            map.addGroundOverlay(newarkMap)

            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.style_json))

            Constants.AsiaParkMap.run {
                map.setLatLngBoundsForCameraTarget(bound)
                val rectOptions = PolygonOptions().add(nwLatLng, neLatLng, seLatLng, swLatLng)
                map.addPolygon(rectOptions)

                map.moveCamera(CameraUpdateFactory.newCameraPosition(
                    CameraPosition.builder()
                        .target(swLatLng)
                        .bearing(-7f)
                        .build()))
            }
        }
    }

}