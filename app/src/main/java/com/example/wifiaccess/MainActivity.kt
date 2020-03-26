package com.example.wifiaccess

import android.Manifest
import android.content.Context
import android.content.IntentSender
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.SupplicantState
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.format.Formatter
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class MainActivity : AppCompatActivity(), MultiplePermissionsListener {
    private lateinit var wifiTextView: TextView
    private lateinit var wifiButton: Button
    private val REQUEST_CHECK_SETTINGS = 0x1

    private lateinit var wifiManager: WifiManager
    private lateinit var connManager: ConnectivityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableGps()

        wifiTextView = findViewById<TextView>(R.id.wifi_text)
        wifiButton = findViewById<Button>(R.id.button_wifi)

        connManager = this.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        wifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        wifiButton.setOnClickListener{
            wifiTextView.text = getWifiScanResult()
        }
    }

    override fun onStart() {
        super.onStart()
        locPerms()
    }

    private fun getWifiScanResult(): String {
        var result = ""

        if (isWiFiConnected()) {
            val wifiInfo = wifiManager.connectionInfo
            if (wifiInfo != null && wifiInfo.supplicantState == SupplicantState.COMPLETED) {
                val ssid = wifiInfo.ssid
                val bssid = wifiInfo.bssid
                val ipAddress = wifiInfo.ipAddress
                val serverAddress = wifiManager.dhcpInfo.serverAddress //different from settings
                val dhcp = wifiManager.dhcpInfo
                val networkId = wifiInfo.networkId
                val linkSpeed = wifiInfo.linkSpeed
                val privateIP = Formatter.formatIpAddress(ipAddress)

                result = "ssid = $ssid\n\nbssid = $bssid\n\nprivateIP = $privateIP\n\nipAddress = $ipAddress" +
                        "\n\nserverAddress = $serverAddress\n\nDHCP = $dhcp\n\nnetworkId = $networkId\n\nlinkSpeed = $linkSpeed"
            }
        }
        return result
    }

    private fun isWiFiConnected(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val network = connManager.activeNetwork
            val capabilities = connManager.getNetworkCapabilities(network)
            capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } else {
            return false
        }
    }

    //used to get device location permission.
    private fun enableGps() {

        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(2000)
            .setFastestInterval(1000)

        val settingsBuilder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        settingsBuilder.setAlwaysShow(true)

        val result = LocationServices.getSettingsClient(this).checkLocationSettings(settingsBuilder.build())
        result.addOnCompleteListener { task ->

            //getting the status code from exception
            try {
                task.getResult(ApiException::class.java)
            } catch (ex: ApiException) {

                when (ex.statusCode) {

                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        toast("Location is OFF.")

                        // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().
                        val resolvableApiException = ex as ResolvableApiException
                        resolvableApiException.startResolutionForResult(this,REQUEST_CHECK_SETTINGS
                        )
                    } catch (e: IntentSender.SendIntentException) {
                        toast("PendingIntent unable to execute request.")
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        toast("Something is wrong in your GPS")
                    }
                }
            }
            Log.d("enableGPS: ", "was called!!!")
        }
    }

    //used library: https://github.com/Karumi/Dexter --used to get app-level location permission.
    private fun locPerms() {

        val permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )

        Dexter.withActivity(this)
            .withPermissions(permissions)
            .withListener(this)
            ?.check()
        Log.d("locPerms: ", "was called!!!")
    }

    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
        toast("Location ON.")
    }

    override fun onPermissionRationaleShouldBeShown(
        permissions: MutableList<PermissionRequest>?,
        token: PermissionToken?
    ) {
        toast("Location permissions required for WiFi info.")
    }
}

