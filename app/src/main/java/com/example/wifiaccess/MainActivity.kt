package com.example.wifiaccess

import android.Manifest
import android.content.Context
import android.content.IntentSender
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.SupplicantState
import android.net.wifi.WifiManager
import android.os.Bundle
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
    private lateinit var locManager: LocationManager
   // var SSPName = ""
   // var LandingPageURL = ""

   // var resultList = ArrayList<ScanResult>()

    //TODO: used with startScanning() and stopScanning()
//    private val broadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            resultList = wifiManager.scanResults as ArrayList<ScanResult>
//            Log.d("TESTING", "onReceive Called")
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableGps()

        wifiTextView = findViewById<TextView>(R.id.wifi_text)
        wifiButton = findViewById<Button>(R.id.button_wifi)

        connManager = this.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        wifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        locManager = this.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        wifiButton.setOnClickListener{
            wifiTextView.text = getWifiScanResult()
        }
       // getSSPName()
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

                result = "ssid = $ssid\n\nbssid = $bssid\n\nipAddress = $ipAddress" +
                        "\n\nserverAddress = $serverAddress\n\nDHCP = $dhcp\n\nnetworkId = $networkId\n\nlinkSpeed = $linkSpeed"
            }
        }
        return result
    }

    //TODO: this was trying to work with Broadcast receiver to get a list of scanned wifi access points... returned "kotlin.unit" ???
//    private fun startScanning() {
//        var scanList: List<String>
//        registerReceiver(broadcastReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
//        wifiManager.startScan()
//        Handler().postDelayed({
//            scanList = stopScanning()
//        }, 10000)
//    }
//
//    private fun stopScanning(): List<String> {
//        unregisterReceiver(broadcastReceiver)
//        val axisList = ArrayList<String>()
//        for (r in resultList) {
//            axisList.add(r.BSSID.toString())
//            Log.d("scan result: ", "$axisList")
//        }
//        return axisList
//    }

    private fun isWiFiConnected(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val network = connManager.activeNetwork
            val capabilities = connManager.getNetworkCapabilities(network)
            capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } else {
            return false
        }
    }

    //TODO: this was trying to experiment getting the mac address.
//    @RequiresApi(Build.VERSION_CODES.N)
//    private fun macAddressFromAdmin(): String { //TODO: come back to this. https://stackoverflow.com/questions/43338359/get-device-mac-adress-in-android-nougat-and-o-programmatically
//        var macAdd = ""
//        val admin = DeviceAdminReceiver()
//        val devicePolicyManager = admin.getManager(applicationContext)
//        val name1 = admin.getWho(applicationContext)
//        if (devicePolicyManager.isAdminActive(name1)) {
//            macAdd = devicePolicyManager.getWifiMacAddress(name1)!!
//            Log.e("macAddress", "" + macAdd)
//        }
//        return macAdd
//    }

    //TODO: this was Java code from the HalAndroid app.
//    fun getSSPName() {
//        //This code get the DHCP server name and does a reverse lookup to get the ISP name which would be converted to SSP by appending the SSP suffix.
//        //Code will not work for warehouse setup if the ISP doesn't host the network or in non-prod environments. It defaults to airwatch settings in this case.
//        try {
//            val serverAddress = wifiManager.dhcpInfo.serverAddress
//            val ipAddress = ((serverAddress and 0xFF)
//                .toString() + "." + (serverAddress shr 8 and 0xFF)
//                    + "." + (serverAddress shr 16 and 0xFF)
//                    + "." + (serverAddress shr 24 and 0xFF))
//            println(ipAddress)
//            Log.d("getSSPName", "DHCP address $ipAddress")
//            val addr = InetAddress.getByName(ipAddress)
//            val host = addr.hostName
//            Log.d("getSSPname", "ISP name $ipAddress")
//            if (host.startsWith("me") || host.startsWith("bl") || host.startsWith("bo") || host.startsWith(
//                    "mb"
//                )
//            ) {
//                SSPName = host.substring(0, 5) + "asssp01"
//                //Specify landing page based on app
//                LandingPageURL = ""
//                Log.d(
//                    "getSSPname",
//                    "SSP name $SSPName"
//                )
//                Log.d(
//                    "getSSPname",
//                    "LandingPage URL $LandingPageURL"
//                )
//            }
//        } catch (e1: Exception) {
//            e1.printStackTrace()
//        }
//    }

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

