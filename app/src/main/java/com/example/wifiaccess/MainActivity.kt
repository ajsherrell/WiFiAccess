package com.example.wifiaccess

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private lateinit var wifiTextView: TextView
    private lateinit var wifiButton: Button

    private lateinit var wifiManager: WifiManager
    private lateinit var connManager: ConnectivityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        wifiTextView = findViewById<TextView>(R.id.wifi_text)
        wifiButton = findViewById<Button>(R.id.button_wifi)

        wifiButton.setOnClickListener{
           wifiTextView.text = getWifiScanResult()
        }
    }

    private fun getWifiScanResult(): String {
        var result = ""
        connManager = this.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        wifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        if (isWiFiConnected()) {
            val wifiInfo = wifiManager.connectionInfo
            if (wifiInfo != null) {
                val ssid = wifiInfo.ssid.toString()
                val bssid = wifiInfo.bssid.toString()
                val ipAddress = wifiInfo.ipAddress.toString()
                val macAddress = wifiInfo.macAddress.toString()
                val networkId = wifiInfo.networkId.toString()
                val linkSpeed = wifiInfo.linkSpeed.toString()

                result = "ssid = $ssid\n\nbssid = $bssid\n\nipAddress = $ipAddress" +
                        "\n\nmacAddress = $macAddress\n\nnetworkId = $networkId\n\nlinkSpeed = $linkSpeed"
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
            connManager.activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI
        }
    }
}
