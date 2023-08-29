package com.example.nitro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class Diagnosis : AppCompatActivity() {


    private val speedLimit : Int = 30

    private var isMoving: Boolean = false
    private lateinit var displayText : TextView
    private val client = OkHttpClient()
    private lateinit var mainB : Button
    private lateinit var forwardB : Button
    private lateinit var leftB : Button
    private lateinit var rightB : Button
    private lateinit var backwardB : Button
    private lateinit var speedBar : SeekBar
    private lateinit var speedBarText : TextView
    private lateinit var unBrick : Button
    private lateinit var limitator : Switch
    private var speed : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diagnosis)

        displayText = findViewById(com.example.nitro.R.id.requestStatus)
        mainB = findViewById(R.id.mainActivity)
        mainB.setOnClickListener {openMainActivity()}

        forwardB = findViewById(R.id.forward)
        forwardB.setOnTouchListener { _, event ->
            forwardButtonTouchManager(event.action)
            true // Consume the touch event
        }

        backwardB = findViewById(R.id.backward)
        backwardB.setOnTouchListener { _, event ->
            backwardButtonTouchManager(event.action)
            true // Consume the touch event
        }

        speedBar = findViewById(R.id.speedBar)
        speedBar.setOnSeekBarChangeListener(handleSpeedBar())
        speedBarText = findViewById(R.id.speedText)

        leftB = findViewById(R.id.left)
        leftB.setOnTouchListener { _, event ->
            leftButtonTouchManager(event.action)
            true
        }

        rightB = findViewById(R.id.right)
        rightB.setOnTouchListener { _, event ->
            rightButtonTouchManager(event.action)
            true
        }

        unBrick = findViewById(R.id.unBrick)
        unBrick.setOnClickListener{handleUnBrick()}

        limitator = findViewById(R.id.limitator)
        limitator.setOnClickListener{if(limitator.isChecked && (speed > speedLimit) ) speed = speedLimit ; speedBarText.text = "Speed: $speed"}

    }

    private fun handleUnBrick(){
        var endpoint : String = "/unbrick"

        sendRequest(endpoint)
    }

    private fun backwardButtonTouchManager(action: Int) {
        when(action) {
            MotionEvent.ACTION_DOWN -> {
                handleBackward(true)
                isMoving = true
            }
            MotionEvent.ACTION_UP -> {
                handleBackward(false)
                isMoving = false
            }
        }
    }

    private fun handleBackward(backward: Boolean) {

        var endpoint : String = ""
        if(backward)
            endpoint = "/backward"
        else
            endpoint = "/stop"

        sendRequest(endpoint)
    }

    private fun rightButtonTouchManager(action: Int) {
        when(action) {
            MotionEvent.ACTION_DOWN -> {
                handleRight(true)
            }
            MotionEvent.ACTION_UP -> {
                handleRight(false)
            }
        }
    }

    private fun handleRight(right: Boolean) {

        var endpoint : String = ""
        if(right)
            endpoint = "/steerRight"
        else
            endpoint = "/endSteeringRight"

        sendRequest(endpoint)
    }

    private fun leftButtonTouchManager(action: Int) {
        when(action) {
            MotionEvent.ACTION_DOWN -> {
                handleLeft(true)
            }
            MotionEvent.ACTION_UP -> {
                handleLeft(false)
            }
        }
    }

    private fun handleLeft(left: Boolean) {

        var endpoint : String = ""
        if(left)
            endpoint = "/steerLeft"
        else
            endpoint = "/endSteeringLeft"

        sendRequest(endpoint)
    }

    private fun forwardButtonTouchManager(action: Int) {
        when(action) {
            MotionEvent.ACTION_DOWN -> {
                handleForward(true)
                isMoving = true
            }
            MotionEvent.ACTION_UP -> {
                handleForward(false)
                isMoving = false
            }
        }
    }

    private fun handleForward(moving : Boolean){

        Log.v("app","FORWARD");

        var endpoint : String = ""
        if(moving)
            endpoint = "/forward"
        else
            endpoint = "/stop"

        sendRequest(endpoint)
    }

    private fun handleSpeedBar() : SeekBar.OnSeekBarChangeListener {
        val debounceDelay = 100 // 100 miliseconds
        var lastUpdateTime = 0L

        return object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                speed = p1;
                if(limitator.isChecked) {
                    if(speed > speedLimit)
                        speed = speedLimit
                }
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastUpdateTime >= debounceDelay) {
                    lastUpdateTime = currentTime

                    speedBarText.text = "Speed: $speed"
                    sendRequest("/speed:$speed")

                }
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {
            }
            override fun onStopTrackingTouch(p0: SeekBar?) {

                var progress = speedBar.progress
                if(limitator.isChecked && progress > speedLimit)
                    progress = speedLimit

                speedBarText.text = "Speed: $progress"
                sendRequest("/speed:$progress ")
            }
        }
    }

    private fun openMainActivity() {
        var intent : Intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
    }

    private fun sendRequest(endpoint : String) {

        val baseUrl = "http://192.168.4.1"

        val request = Request.Builder()
            .url(baseUrl + endpoint)
            .build()

        client.newCall(request).enqueue(object : Callback {
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

}