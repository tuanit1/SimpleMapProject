package com.tuandev.simplemapproject.ui.toolmap

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
