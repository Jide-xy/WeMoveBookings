package com.example.wemovebookings.ui.map

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.wemovebookings.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val origin = LatLng(intent.getDoubleExtra("lat1", 0.0), intent.getDoubleExtra("long1", 0.0))
        val dest = LatLng(intent.getDoubleExtra("lat2", 0.0), intent.getDoubleExtra("long2", 0.0))
        // Add a marker in Sydney and move the camera
        mMap.addMarker(MarkerOptions().position(origin).title("Origin Marker"))
        mMap.addMarker(MarkerOptions().position(dest).title("Destination Marker"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(origin))

        val routeLine = PolylineOptions()
        routeLine.add(origin, dest)
        mMap.addPolyline(routeLine)
    }
}
