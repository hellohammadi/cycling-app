package com.example.cyclingtracker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity(), LocationListener {

    private lateinit var locationManager: LocationManager
    private lateinit var tvSpeed: TextView
    private lateinit var tvDistance: TextView
    private lateinit var chronometer: Chronometer
    private lateinit var btnPause: Button

    private var isPaused = false
    private var totalDistance = 0f
    private var lastLocation: Location? = null
    private var pauseOffset: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
            gravity = android.view.Gravity.CENTER
        }

        tvSpeed = TextView(this).apply { textSize = 36f; text = "0.0 km/h" }
        tvDistance = TextView(this).apply { textSize = 24f; text = "0.00 km" }
        chronometer = Chronometer(this).apply { textSize = 28f }
        btnPause = Button(this).apply { text = "Pause" }

        layout.addView(tvSpeed)
        layout.addView(tvDistance)
        layout.addView(chronometer)
        layout.addView(btnPause)
        setContentView(layout)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        } else {
            startTracking()
        }

        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.start()

        btnPause.setOnClickListener {
            if (isPaused) {
                isPaused = false
                btnPause.text = "Pause"
                chronometer.base = SystemClock.elapsedRealtime() - pauseOffset
                chronometer.start()
            } else {
                isPaused = true
                btnPause.text = "Resume"
                pauseOffset = SystemClock.elapsedRealtime() - chronometer.base
                chronometer.stop()
            }
        }
    }

    private fun startTracking() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1f, this)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onLocationChanged(location: Location) {
        if (isPaused) return

        if (location.hasSpeed()) {
            val speedKmH = location.speed * 3.6f
            tvSpeed.text = String.format("%.1f km/h", speedKmH)
        }

        if (lastLocation != null) {
            totalDistance += lastLocation!!.distanceTo(location)
            tvDistance.text = String.format("%.2f km", totalDistance / 1000)
        }
        lastLocation = location
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}
