package com.tuandev.simplemapproject

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.tuandev.simplemapproject.databinding.FragmentMapBinding
import com.tuandev.simplemapproject.extension.addFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking


class MapFragment : Fragment() {

    private var binding: FragmentMapBinding? = null
    private var supportMapFragment: SupportMapFragment? = null
    private var currentZoomLevel: Float = 0F

    companion object {
        fun newInstance() = MapFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapBinding.inflate(inflater)

        supportMapFragment = SupportMapFragment.newInstance(
            GoogleMapOptions()
                .mapType(GoogleMap.MAP_TYPE_TERRAIN)
                .rotateGesturesEnabled(false)
        )
        supportMapFragment?.let { fragment ->
            addFragment(
                containerId = getContainerId(),
                fragment = fragment,
                addToBackStack = true
            )
        }
        handleMap()

        return binding?.root
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


            //limit interacting area
//            val adelaideBounds = LatLngBounds(
//                LatLng(-35.0, 138.58),  // SW bounds
//                LatLng(-34.9, 138.61) // NE bounds
//            )
//
//            map.setLatLngBoundsForCameraTarget(adelaideBounds)

//            map.setOnCameraMoveListener {
//                if(currentZoomLevel != map.cameraPosition.zoom){
//                    currentZoomLevel = map.cameraPosition.zoom
//                    Log.e("zzzzz", "zoom level change: ${map.cameraPosition.zoom}")
//                }
//
//            }

            val pos1 = LatLng(40.0617348930446,-73.860182762146)
            val pos2 = LatLng(40.15459323422441,-73.75683523714542)
            val myBound = LatLngBounds(pos1, pos2)

            map.addMarker(
                MarkerOptions().position(pos1)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )

            map.addMarker(
                MarkerOptions().position(pos2)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )

            map.setOnMapClickListener {
                Log.e("zzzzz", "${map.cameraPosition.target}")
            }

            val newarkMap = GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.khoi))
                .positionFromBounds(myBound)
            map.addGroundOverlay(newarkMap)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(pos1, 10f))
        }
    }

}