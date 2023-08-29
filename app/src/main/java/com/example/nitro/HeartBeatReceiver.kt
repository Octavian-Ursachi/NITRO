package com.example.nitro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class HeartbeatReceiver : BroadcastReceiver() {

    val baseUrl = "http://192.168.4.1"
    val endpoint = "/heartbeat"

    override fun onReceive(context: Context?, intent: Intent?) {
        // Send a heartbeat request using OKhttp to the Raspberry Pi Pico's IP
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(baseUrl + endpoint)
            .build()
        Log.v("HeartbeatReceiver", "Received heartbeat broadcast")
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                // Handle response if needed
            }
        })
    }
}