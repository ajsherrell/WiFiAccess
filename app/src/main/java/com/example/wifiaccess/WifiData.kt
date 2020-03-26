package com.example.wifiaccess

data class WifiData(
    val ipAddress: String,
    val ssid: String,
    val bssid: String,
    val serverAddress: String,
    val dhcp: String,
    val networkId: String,
    val linkSpeed: String,
    val privateIP: String
)