package com.example.androidproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.example.androidproject.databinding.ActivityTripTrackingBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth


class TripTrackingActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityTripTrackingBinding
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var locationRequest = LocationRequest.create().apply {
        interval = 2000
        fastestInterval = 1000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private var isTracking = true
    private var pathPoints = mutableListOf<LatLng>()
    private var totalDistance = 0.0
    private var startTime = 0L
    private var elapsedTime = 0L // Store elapsed time
    private var handler = Handler()
    private var timeRunning = true // To track if time is running
    private var currentLocation: Location? = null
    private var mileage = 0f // Mileage input from user

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //firestore variables
        val firestore = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        binding = ActivityTripTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Button listeners
        binding.btnPause.setOnClickListener {
            togglePauseResume()
        }

        binding.btnStop.setOnClickListener {
            stopTracking()
        }

        binding.btnReset.setOnClickListener {
            resetTracking()
        }

        // Start the time updates when the activity is created
        startTime = SystemClock.elapsedRealtime()
        startTimeUpdates()

        // Location callback to handle location updates
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                if (!isTracking || !timeRunning) return
                val newLocation = result.lastLocation ?: return
                currentLocation = newLocation
                val newLatLng = LatLng(newLocation.latitude, newLocation.longitude)
                pathPoints.add(newLatLng)

                if (pathPoints.size > 1) {
                    val last = pathPoints[pathPoints.size - 2]
                    val result = FloatArray(1)
                    Location.distanceBetween(last.latitude, last.longitude, newLatLng.latitude, newLatLng.longitude, result)
                    totalDistance += result[0]

                    val polylineOptions = PolylineOptions()
                        .addAll(pathPoints)
                        .width(8f)
                        .color(resources.getColor(android.R.color.holo_red_dark))
                    mMap.clear()
                    mMap.addPolyline(polylineOptions)

                    // Add start and current markers
                    mMap.addMarker(
                        MarkerOptions()
                            .position(pathPoints.first())
                            .title("Start")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    )
                    mMap.addMarker(
                        MarkerOptions()
                            .position(pathPoints.last())
                            .title("Current")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    )
                }

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 17f))

                // Update UI with distance and speed
                binding.tvDistance.text = "Distance: %.2f km".format(totalDistance / 1000)
                binding.tvSpeed.text = "Speed: %.1f km/h".format(newLocation.speed * 3.6f) // speed in km/h
            }
        }

        // Handle Submit button for mileage input
        binding.btnSubmitMileage.setOnClickListener {
            val mileageInput = binding.etMileage.text.toString().toFloatOrNull()
            if (mileageInput != null && mileageInput > 0 && totalDistance > 10) {
                mileage = mileageInput
                calculateStats()
            } else {
                binding.tvMileageError.text = "Please enter a valid mileage"
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
        mMap.isMyLocationEnabled = true
    }

    private fun startTimeUpdates() {
        if (!timeRunning) return // If time is paused, don't continue updating time

        handler.postDelayed(object : Runnable {
            override fun run() {
                if (timeRunning) { // Only update if time is running
                    elapsedTime = (SystemClock.elapsedRealtime() - startTime) / 1000
                    binding.tvTime.text = "Time: ${elapsedTime}s"
                    handler.postDelayed(this, 1000)
                }
            }
        }, 1000)
    }

    private fun stopTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        val elapsed = elapsedTime

        val mileageInput = binding.etMileage.text.toString()
        val mileage = mileageInput.toFloatOrNull()

        var fuelUsed = 0f
        var co2Emitted = 0f
        var ecoScore = "--"

        if (mileage != null && mileage > 0 && totalDistance > 100) {
            val distanceKm = totalDistance / 1000
            fuelUsed = distanceKm.toFloat() / mileage
            co2Emitted = fuelUsed * 2392  // grams of CO2 per liter of petrol

            // Eco Score based on fuel consumption
            ecoScore = when {
                fuelUsed <= 0.05f -> "100"
                fuelUsed <= 0.1f -> "80"
                fuelUsed <= 0.2f -> "60"
                else -> "40"
            }
        }

        // Save trip data to Firestore
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val tripData = hashMapOf(
                "distance" to totalDistance / 1000, // in km
                "elapsedTime" to elapsed,
                "fuelUsed" to fuelUsed,
                "co2Emitted" to co2Emitted,
                "ecoScore" to ecoScore,
                "timestamp" to System.currentTimeMillis()
            )

            // Save trip data in the Firestore collection for the current user
            FirebaseFirestore.getInstance().collection("users")
                .document(currentUser.uid)
                .collection("trips")
                .add(tripData)
                .addOnSuccessListener {
                    // Successfully saved
                    Log.d("Firestore", "Trip data saved successfully")
                }
                .addOnFailureListener { e ->
                    // Handle error
                    Log.e("Firestore", "Error saving trip data", e)
                }
        }

        // Pass data to OnboardingActivity
        val intent = Intent(this, Dashboard::class.java).apply {
            putExtra("totalDistance", totalDistance)
            putExtra("elapsedTime", elapsed)
            putExtra("fuelUsed", fuelUsed)
            putExtra("co2Emitted", co2Emitted)
            putExtra("ecoScore", ecoScore)
        }

        finish()
        startActivity(intent)
    }



    private fun resetTracking() {
        // Reset all tracking data
        pathPoints.clear()
        totalDistance = 0.0
        startTime = SystemClock.elapsedRealtime()
        elapsedTime = 0L

        // Clear the map and update the UI
        mMap.clear()
        binding.tvDistance.text = "Distance: 0.00 km"
        binding.tvSpeed.text = "Speed: 0 km/h"
        binding.tvTime.text = "Time: 0s"

        // Reset the input field and calculated speed
        binding.etMileage.text.clear() // Clear mileage input
    }

    private fun togglePauseResume() {
        if (timeRunning) {
            timeRunning = false
            binding.btnPause.text = "Resume"
            fusedLocationClient.removeLocationUpdates(locationCallback) // Pause location updates
        } else {
            timeRunning = true
            binding.btnPause.text = "Pause"
            startTime = SystemClock.elapsedRealtime() - elapsedTime * 1000
            startLocationUpdates() // Resume location updates
            startTimeUpdates() // Resume time updates
        }
    }

    private fun calculateStats() {
        if (mileage > 0) {
            val distanceTraveled = totalDistance / 1000 // Convert meters to kilometers
            val fuelUsed = distanceTraveled / mileage

            // Assuming 2.31 kg CO2 per liter of fuel
            val co2Emission = fuelUsed * 2.31f

            // Example eco score calculation
            val maxEcoScore = 100
            val maxCo2Emission = 100f // You can adjust this for actual values
            val ecoScore = maxEcoScore - (co2Emission / maxCo2Emission) * maxEcoScore

            // Update the UI with calculated values
            binding.tvCO2.text = "CO2 Emission: %.2f g".format(co2Emission * 1000) // Convert to grams
            binding.tvFuel.text = "Fuel Used: %.2f L".format(fuelUsed)
            binding.tvEcoScore.text = "Eco Score: %.2f / 100".format(ecoScore)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        handler.removeCallbacksAndMessages(null) // Stop time updates
    }
}
