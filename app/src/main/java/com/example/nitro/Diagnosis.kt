package com.example.nitro

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.HandlerCompat.postDelayed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketTimeoutException


class Diagnosis : AppCompatActivity() {

    private val recieveTermination: Char = ' '
    private val sendTermination: Char = ' '
    private val speedLimit : Int = 30

    //private var isMoving: Boolean = false
    private lateinit var mainB : Button
    private lateinit var forwardB : Button
    private lateinit var leftB : Button
    private lateinit var rightB : Button
    private lateinit var backwardB : Button
    private lateinit var speedBar : SeekBar

    private lateinit var speedBarText : TextView
    private lateinit var carspeedText: TextView
    private lateinit var pacheteTrimiseText: TextView
    private lateinit var pachetePrimiteText: TextView
    private lateinit var pachetPrimitText: TextView
    private lateinit var pachetePierduteText: TextView

    private lateinit var unBrick : Button
    private lateinit var limitator : Switch
    private lateinit var claxon: Button

    private var speed : Int = 0
    private var steer: Float = 0F
    private var claxonez: Int = 0
    private var unbrichez: Int = 0
    private var directieViteza: Int = 0
    private var nrPacTrimise = 0
    private var nrPacPrimite = 0
    private var nrPacPierdute = 0
    private var sochet: DatagramSocket = DatagramSocket(null)
    override fun onDestroy() {
        sochet.close()
        super.onDestroy()
    }
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diagnosis)

        sochet.soTimeout = 500

        MainScope().launch(Dispatchers.IO){
            openSocket("192.168.4.16")
            connectSocket("192.168.4.1")
        }
        carspeedText = findViewById(R.id.carSpeed)
        pacheteTrimiseText = findViewById(R.id.pacheteTrimise)
        pachetePrimiteText = findViewById(R.id.pachetePrimite)
        pachetPrimitText = findViewById(R.id.pachetPrimit)
        pachetePierduteText = findViewById(R.id.pachetePierdute)

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

        val handler = Handler()
        val delay = 60 // 1000 milliseconds == 1 second


        handler.postDelayed(object : Runnable {
            override fun run() {
                sendRequest((speed * directieViteza).toString()+"$sendTermination$steer$sendTermination$claxonez$sendTermination$unbrichez")
                recieveRequestUDP()
                handler.postDelayed(this, delay.toLong())
            }
        }, delay.toLong())
    }

    private fun openSocket(iplocal: String){
        sochet.bind(InetSocketAddress(iplocal,7876))
        println("Socket creat pe adresa locala $iplocal")
    }
    private fun connectSocket(ipdestinatie: String){
        sochet.connect(InetSocketAddress(ipdestinatie,80))
        println("Socket conectat")
    }

    private fun recieveRequestUDP(){
        MainScope().launch(Dispatchers.IO) {
            val pachet = DatagramPacket(ByteArray(30), 30)
            try{
                sochet.receive(pachet)
                pachetPrimitText.text = "Pachet primit: ${pachet.data.decodeToString()}"
                pachetePrimiteText.text = "Pachete primite: "+(++nrPacPrimite)
                //println(pachet.data.decodeToString())
            }
            catch (eroare: SocketTimeoutException){
                println("timeout")
                pachetePierduteText.text = "Pachete pierdute: "+(++nrPacPierdute)
                println(eroare)
            }
        }
    }
    private fun sendRequest(messaj : String) {
        MainScope().launch(Dispatchers.IO){
            println("Trimit mesaj")
            pacheteTrimiseText.text = "Pachete trimise: "+(++nrPacTrimise)
            sochet.send(DatagramPacket(messaj.encodeToByteArray(),messaj.length))
        }
        //recieveRequestUDP()
    }
    private fun handleUnBrick(){
        val endpoint = "/unbrick"
        //sendRequest(endpoint)
        //recieveRequestUDP()
        unbrichez = 1       //TODO AI GRIJA asta nu cred ca e safe dupa prima utilizare
    }

    private fun backwardButtonTouchManager(action: Int) {
        when(action) {
            MotionEvent.ACTION_DOWN -> {
                //handleBackward(true)
                directieViteza = -1
                //isMoving = true
            }
            MotionEvent.ACTION_UP -> {
                //handleBackward(false)
                //isMoving = false
                directieViteza = 0
            }
        }
    }

    private fun claxonButtonTouchManager(action: Int){
        when(action) {
            MotionEvent.ACTION_DOWN -> {
                //sendRequest("/horn")
                //recieveRequestUDP()
                claxonez = 1
            }
            MotionEvent.ACTION_UP -> {
                claxonez = 0
                //sendRequest("/hornStop")
                //recieveRequestUDP()
            }
        }
    }
    private fun handleBackward(backward: Boolean) {
        val endpoint = if(backward)
            "/backward"
        else
            "/stop"
        //sendRequest(endpoint)
        //recieveRequestUDP()
    }

    private fun rightButtonTouchManager(action: Int) {
        when(action) {
            MotionEvent.ACTION_DOWN -> {
                //handleRight(true)
                steer = 1f
            }
            MotionEvent.ACTION_UP -> {
                //handleRight(false)
                steer = 0f
            }
        }
    }

    private fun handleRight(right: Boolean) {
        val endpoint = if(right)
            "/steerRight"
        else
            "/endSteeringRight"
        sendRequest(endpoint)
        recieveRequestUDP()
    }

    private fun leftButtonTouchManager(action: Int) {
        when(action) {
            MotionEvent.ACTION_DOWN -> {
                //handleLeft(true)
                steer = -1f
            }
            MotionEvent.ACTION_UP -> {
                //handleLeft(false)
                steer = 0f
            }
        }
    }

    private fun handleLeft(left: Boolean) {
        val endpoint = if(left)
            "/steerLeft"
        else
            "/endSteeringLeft"

        sendRequest(endpoint)
        recieveRequestUDP()
    }

    private fun forwardButtonTouchManager(action: Int) {
        when(action) {
            MotionEvent.ACTION_DOWN -> {
                //handleForward(true)
                //isMoving = true
                directieViteza = 1
            }
            MotionEvent.ACTION_UP -> {
                //handleForward(false)
                //isMoving = false
                directieViteza = 0
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
        recieveRequestUDP()
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
                /*val currentTime = System.currentTimeMillis()
                if (currentTime - lastUpdateTime >= debounceDelay) {
                    lastUpdateTime = currentTime*/

                    speedBarText.text = "Speed: $speed"
                    //sendRequest("/speed:$speed")
                    //recieveRequestUDP()
                //}
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {
            }
            override fun onStopTrackingTouch(p0: SeekBar?) {

                var progress = speedBar.progress
                if(limitator.isChecked && progress > speedLimit)
                    progress = speedLimit

                speedBarText.text = "Speed: $progress"
                //sendRequest("/speed:$progress ")
                //recieveRequestUDP()
            }
        }
    }

    private fun openMainActivity() {
        val intent : Intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
    }


}