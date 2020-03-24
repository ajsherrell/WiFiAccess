package com.example.wifiaccess

import android.app.admin.DeviceAdminReceiver
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.ScanResult
import android.net.wifi.SupplicantState
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.net.InetAddress


class MainActivity : AppCompatActivity() {
    private lateinit var wifiTextView: TextView
    private lateinit var wifiButton: Button

    private lateinit var wifiManager: WifiManager
    private lateinit var connManager: ConnectivityManager

   // var SSPName = ""
   // var LandingPageURL = ""

    var resultList = ArrayList<ScanResult>()

    //TODO: used with startScanning() and stopScanning()
//    private val broadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(contxt: Context?, intent: Intent?) {
//            resultList = wifiManager.scanResults as ArrayList<ScanResult>
//            Log.d("TESTING", "onReceive Called")
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        wifiTextView = findViewById<TextView>(R.id.wifi_text)
        wifiButton = findViewById<Button>(R.id.button_wifi)

        connManager = this.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        wifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        wifiButton.setOnClickListener{
            wifiTextView.text = getWifiScanResult()
        }
       // getSSPName()
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
}
