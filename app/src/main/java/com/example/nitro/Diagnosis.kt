package com.example.nitro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.util.LinkedList
import java.util.Queue

class Diagnosis : AppCompatActivity() {

    private val recieveTermination: Char = ' '
    private val sendTermination: Char = ' '
    private val speedLimit : Int = 30

    private var isMoving: Boolean = false
    private lateinit var displayText : TextView
    private lateinit var mainB : Button
    private lateinit var forwardB : Button
    private lateinit var leftB : Button
    private lateinit var rightB : Button
    private lateinit var backwardB : Button
    private lateinit var speedBar : SeekBar
    private lateinit var speedBarText : TextView
    private lateinit var unBrick : Button
    private lateinit var limitator : Switch
    private lateinit var claxon: Button
    private var speed : Int = 0

    //private lateinit var streamInput: InputStream
    //private lateinit var streamOutput: OutputStream
    private var sochet: DatagramSocket = DatagramSocket(null)
    private var baffer: ByteArray = ByteArray(64)
    private var lastMesaj: String = ""
    override fun onDestroy() {
        sochet.close()
        super.onDestroy()
    }
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diagnosis)

        sochet.soTimeout = 1000

        MainScope().launch(Dispatchers.IO){
            openSocket("192.168.4.16")
            connectSocket("192.168.4.1")
            //streamInput = sochet.getInputStream()
            //streamOutput = sochet.getOutputStream()
            //println("Stream input: $streamInput")
            //println("Stream output: $streamOutput")
        }

        displayText = findViewById(R.id.requestStatus)
        mainB = findViewById(R.id.mainActivity)
        mainB.setOnClickListener {openMainActivity()}

        forwardB = findViewById(R.id.forward)
        forwardB.setOnTouchListener { _, event ->
            forwardButtonTouchManager(event.action)
            true // Consume the touch event
        }

        claxon = findViewById(R.id.claxon)
        claxon.setOnTouchListener{  _, event ->
            claxonButtonTouchManager(event.action)
            true
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

    private fun openSocket(iplocal: String){
        sochet.bind(InetSocketAddress(iplocal,7876))
        println("Socket creat pe adresa locala $iplocal")
    }
    private fun connectSocket(ipdestinatie: String){
        sochet.connect(InetSocketAddress(ipdestinatie,80))
        println("Socket conectat")
    }
    private fun recieveRequestWhile(){
        MainScope().launch(Dispatchers.IO) {
            println("Astept mesaj")
            var mesaj = ""
            var caracterCitit: Char
            //println("Mesaj neparsat:")
            /*do {
                caracterCitit = streamInput.read().toChar()
                //print(caracterCitit)
                mesaj += caracterCitit
            }while(caracterCitit != recieveTermination)*/
            println("Mesaj primit: $mesaj")
            displayText.text = mesaj
        }
    }

    /*private fun recieveRequestBuffered(){
        MainScope().launch(Dispatchers.IO) {
            val nrBitiDisponibili = streamInput.available()
            println("Am $nrBitiDisponibili biti de citit")
            if(nrBitiDisponibili != 0){
                streamInput.read(baffer)
            }
            lastMesaj = baffer.copyOf(baffer.indexOf(recieveTermination.code.toByte())).decodeToString()//poate bufni
            println("Buffer curent: ${baffer.decodeToString()}")
            println("Mesaj: $lastMesaj")
        }
    }*/
    private fun recieveRequestUDP(){
        MainScope().launch(Dispatchers.IO) {
            val pachet = DatagramPacket(ByteArray(30), 30)
            try{
                sochet.receive(pachet)
                println(pachet.data.decodeToString())
            }
            catch (eroare: SocketTimeoutException){
                println("Socketul si-a luat timeout")
                println(eroare)
            }
        }
    }
    private fun sendRequest(messaj : String) {
        MainScope().launch(Dispatchers.IO){
            println("Trimit mesaj")
            //sochet.getOutputStream().write((messaj+sendTermination).encodeToByteArray())
            sochet.send(DatagramPacket(messaj.encodeToByteArray(),messaj.length))
        }
        //recieveRequestWhile()
        recieveRequestUDP()
    }
    private fun handleUnBrick(){
        val endpoint = "/unbrick"
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

    private fun claxonButtonTouchManager(action: Int){
        when(action) {
            MotionEvent.ACTION_DOWN -> {
                sendRequest("/horn")
            }
            MotionEvent.ACTION_UP -> {
                sendRequest("/hornStop")
            }
        }
    }
    private fun handleBackward(backward: Boolean) {
        val endpoint = if(backward)
            "/backward"
        else
            "/stop"
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
        val endpoint = if(right)
            "/steerRight"
        else
            "/endSteeringRight"
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
        val endpoint = if(left)
            "/steerLeft"
        else
            "/endSteeringLeft"

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

        val endpoint = if(moving)
            "/forward"
        else
            "/stop"

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
        val intent : Intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
    }


}