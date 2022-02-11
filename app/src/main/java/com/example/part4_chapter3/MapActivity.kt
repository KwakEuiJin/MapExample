package com.example.part4_chapter3

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat.requestLocationUpdates
import com.example.part4_chapter3.databinding.ActivityMapBinding
import com.example.part4_chapter3.model.LocationLatLngEntity
import com.example.part4_chapter3.model.SearchResultEntity
import com.example.part4_chapter3.utillity.RetrofitUtil
import com.google.android.gms.location.LocationListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.jar.Manifest
import kotlin.coroutines.CoroutineContext

class MapActivity : AppCompatActivity(), OnMapReadyCallback, CoroutineScope {
    private lateinit var binding: ActivityMapBinding
    private lateinit var map: GoogleMap
    private lateinit var searchResult: SearchResultEntity
    private lateinit var locationManager: LocationManager
    private lateinit var myLocationListener: MyLocationListener

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        job = Job()

        if (::searchResult.isInitialized.not()) {
            intent?.let {
                searchResult = it.getParcelableExtra<SearchResultEntity>(SEARCH_RESULT_EXTRA_KEY)
                    ?: throw Exception("데이터가 없음")
                setUpGoogleMap()
                bindViews()
            }

        }

    }

    private fun bindViews() {
        binding.floatingButton.setOnClickListener {
            getMyLocation()
        }
    }


    private fun setUpGoogleMap() {
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val currentSelectedMarker = setupMaker(searchResult)
        currentSelectedMarker.showInfoWindow()
    }

    private fun setupMaker(searchResultEntity: SearchResultEntity): Marker {
        val positionLatLng = LatLng(
            searchResultEntity.locationLatLng.latitude.toDouble(),
            searchResultEntity.locationLatLng.longitude.toDouble()
        )
        val markerOptions = MarkerOptions().apply {
            position(positionLatLng)
            title(searchResultEntity.name)
            snippet(searchResultEntity.fullAdress)
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(positionLatLng, 17f))
        return map.addMarker(markerOptions)!!
    }

    private fun getMyLocation() {
        if (::locationManager.isInitialized.not()) {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
        val isGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (isGps) {
            if (ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    ),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                setMyLocationListener()
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                setMyLocationListener()
            } else {
                Toast.makeText(this, "권한을 받지 못했습니다", Toast.LENGTH_SHORT).show()
            }

        }
    }

    @SuppressLint("MissingPermission")
    private fun setMyLocationListener() {
        val minTime = 1500L
        val minDistance = 100f
        if (!::myLocationListener.isInitialized) {
            myLocationListener = MyLocationListener()
        }
        with(locationManager) {
            requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTime,
                minDistance,
                myLocationListener
            )
        }


    }


    inner class MyLocationListener : android.location.LocationListener {
        override fun onLocationChanged(location: Location) {
            val locationLatLngEntity = LocationLatLngEntity(
                location.latitude.toFloat(),
                location.longitude.toFloat()
            )
            onCurrentLocationChanged(locationLatLngEntity)
        }

    }

    private fun onCurrentLocationChanged(locationLatLngEntity: LocationLatLngEntity) {
        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    locationLatLngEntity.latitude.toDouble(),
                    locationLatLngEntity.longitude.toDouble()
                ), 17f
            )
        )
        locadReverseGeoInformation(locationLatLngEntity)
        removeLocationListener()
        Log.d("카메라 이동", "카메라 이동")
    }

    private fun locadReverseGeoInformation(locationLatLngEntity: LocationLatLngEntity) {
        launch(coroutineContext) {
            try {
                with(Dispatchers.IO) {
                    val response = RetrofitUtil.apiService.getReverseGeoCode(
                        lat = locationLatLngEntity.latitude.toDouble(),
                        lon = locationLatLngEntity.longitude.toDouble()
                    )
                    if (response.isSuccessful) {
                        val body = response.body()

                        with(Dispatchers.Main) {
                            Log.d("list", body.toString())
                            body?.let {
                                val currentMarker = setupMaker(SearchResultEntity(
                                    fullAdress = it.addressInfo.fullAddress.toString() ?:"주소 없음",
                                    name = "내 위치",
                                    locationLatLng = locationLatLngEntity
                                ))
                                currentMarker.showInfoWindow()
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MapActivity,"예상치 못한 오류가 발생했습니다.",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun removeLocationListener() {
        if (::locationManager.isInitialized && ::myLocationListener.isInitialized) {
            locationManager.removeUpdates(myLocationListener)
        }
    }

    companion object {
        const val PERMISSION_REQUEST_CODE = 101
        const val SEARCH_RESULT_EXTRA_KEY = "SEARCH_RESULT_EXTRA_KEY"
    }
}






