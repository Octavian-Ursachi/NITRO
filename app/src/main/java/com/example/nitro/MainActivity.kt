package com.example.nitro

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


class MainActivity : AppCompatActivity()  {
    private val client = OkHttpClient()
    lateinit var viewPager : ViewPager
    lateinit var adapter : ImageAdapter
    lateinit var diagnosis : Button

    var count = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //sendHeartBeatSignals()

        viewPager = findViewById(R.id.viewPager)
        adapter= ImageAdapter(this)
        viewPager.adapter = adapter

        diagnosis = findViewById(R.id.diagnosis)
        diagnosis.setOnClickListener {openDiagnosisMode()}

        var bCedeaza : Button = findViewById(R.id.bCedeaza)
        var bPrioritate : Button = findViewById(R.id.bPrioritate)
        var bStop : Button = findViewById(R.id.bStop)
        var bUnic : Button = findViewById(R.id.bUnic)
        var bPietoni : Button = findViewById(R.id.bPietoni)

        bCedeaza.setOnClickListener { selectImage(0) }
        bPrioritate.setOnClickListener { selectImage(1) }
        bStop.setOnClickListener { selectImage(2) }
        bUnic.setOnClickListener { selectImage(3) }
        bPietoni.setOnClickListener { selectImage(4) }


    }

    private fun sendHeartBeatSignals() {
        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post(object : Runnable {
            override fun run() {
                sendRequest("/heartbeat")
                Log.v("HEARTBEAT","HEARTBEAT")
                mainHandler.postDelayed(this, 2000)
            }
        })
    }

    private fun openDiagnosisMode() {
        val intent : Intent
        intent = Intent(this,Diagnosis::class.java)
        startActivity(intent)
    }

    private fun selectImage(position : Int) {
        viewPager.currentItem = position
    }

    fun turnOnLed(view : View) {
        sendRequest("/on")
    }

    fun turnOffLed(view: View) {
        sendRequest("/off")
    }

    private fun sendRequest(endpoint : String) {

        val displayText : TextView = findViewById(R.id.textView)

        val baseUrl = "http://192.168.4.1"

        val request = Request.Builder()
            .url(baseUrl + endpoint)
            .build()

        client.newCall(request).enqueue(object  : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Request failed: ${e.message}")
                runOnUiThread {
                    displayText.setText("Request failed: ${e.message}")
                }
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    displayText.setText("Request succeded!")
                }
                response.close()
            }
        })
    }

    fun onClick(view: View) {

        //aprinde led
        count++
        val displayBText : Button = findViewById(R.id.turnOnButton)

        if(count % 2 == 0) {
            displayBText.setText("Turn off")
            turnOnLed(view)
        }
        else {
            displayBText.setText("Turn on")
            turnOffLed(view)
        }


    }
}

