package com.danyberlin.dgnightstand2020

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.Map
import kotlin.math.roundToInt

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@Suppress("DEPRECATION")
class FullscreenActivity : AppCompatActivity() {
    private val lampTopId = "784138e96effdd231df94dd5e4eafcd70b3f8aaecd9ae1c1e68270fb6188c7d7"
    private val lampBottomId = "42e5ff844c93d10b810aae1de1476348f33b6dc38e470ba855306e556e00ae37"
    private val lampLeftId = "94a4bf9c5dfc579925f2bf1e7db786136784732daf9aa04f35ee2e24349c95e8"

    private var authKey =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6InhwZXJpYS16MyIsIm5hbWUiOiJTb255IFhwZXJpYSBaMyIsImFkbWluIjpmYWxzZSwiaW5zdGFuY2VJZCI6IjYyN2NkMDkyYmEyMjgxY2VlMTBkNzU0YWY2YmEwNDY5MzNlOWNkOTA2NzFmNTZhYTk4MjZmOTJmZjkxMjRlNTIiLCJpYXQiOjE2MDE2ODQwMzQsImV4cCI6MTYwMTcxMjgzNH0.MLl7etyO2MsxcgMBAHkt60ZDJf0jH65mZRp1MNEutvQ"
    private lateinit var fullscreenContent: TextView
    private lateinit var fullscreenContentControls: LinearLayout
    private val hideHandler = Handler()

    private lateinit var h1View: TextView
    private lateinit var h2View: TextView
    private lateinit var m1View: TextView
    private lateinit var m2View: TextView
    private lateinit var weekdayView: TextView
    private lateinit var dayView: TextView
    private lateinit var monthView: TextView
    private lateinit var colonView: TextView
    private lateinit var weatherView: TextView
    private lateinit var textView: TextView
    private lateinit var innenTempView: TextView

    private lateinit var aAn: Button
    private lateinit var bAn: Button
    private lateinit var cAn: Button
    private lateinit var bAuf: Button
    private lateinit var cAuf: Button
    private lateinit var bAb: Button
    private lateinit var cAb: Button
    // Settings Layer
    private lateinit var settingsLayer: TableLayout
    private lateinit var seekBar : SeekBar

    @SuppressLint("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar
        actionBar?.hide()

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        fullscreenContent.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
        fullscreenContentControls.visibility = View.VISIBLE
    }
    private var isFullscreen: Boolean = false

    private val hideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val delayHideTouchListener = View.OnTouchListener { view, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS)
            }
            MotionEvent.ACTION_UP -> view.performClick()
            else -> {
            }
        }
        false
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//        View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_fullscreen)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        isFullscreen = true

        // Set up the user interaction to manually show or hide the system UI.
        fullscreenContent = findViewById(R.id.fullscreen_content)
//        fullscreenContent.setOnClickListener { toggle() }

        fullscreenContentControls = findViewById(R.id.fullscreen_content_controls)

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById<Button>(R.id.dummy_button).setOnTouchListener(delayHideTouchListener)

        h1View = findViewById(R.id.h1)
        h2View = findViewById(R.id.h2)
        m1View = findViewById(R.id.m1)
        m2View = findViewById(R.id.m2)
        weekdayView = findViewById(R.id.weekday)
        dayView = findViewById(R.id.day)
        monthView = findViewById(R.id.month)
        colonView = findViewById(R.id.colon)
        weatherView = findViewById(R.id.weather)
        innenTempView = findViewById(R.id.textView24)
        innenTempView.text = "00"

        aAn = findViewById(R.id.a_an)
        bAn = findViewById(R.id.b_an)
        cAn = findViewById(R.id.c_an)

        aAn.text = "Middle"
        bAn.text = "Top"
        cAn.text = "Back"

        textView = findViewById(R.id.textView)

        textView.text = ""



//        bAn.setOnLongClickListener {
//            toggleSettings()
//            true
//        }
        aAn.setOnClickListener {
            sendCommandToggle(lampLeftId)
        }
        aAn.setOnLongClickListener {
            sendCommandToggleOn(lampBottomId, true)
            sendCommandToggleOn(lampLeftId, true)
            true
        }
        bAn.setOnClickListener {
            sendCommandToggle(lampTopId)
        }
        bAn.setOnLongClickListener {
            sendCommandToggleOn(lampBottomId, true)
            sendCommandToggleOn(lampTopId, true)
            sendCommandToggleOn(lampLeftId, true)
            true
        }
        cAn.setOnClickListener {
            sendCommandToggle(lampBottomId)
        }
        cAn.setOnLongClickListener {
            sendCommandToggleOn(lampBottomId, false)
            sendCommandToggleOn(lampTopId, false)
            sendCommandToggleOn(lampLeftId, false)
            true
        }


        // Settings Layer
        settingsLayer = findViewById(R.id.settingLayer)
        settingsLayer.visibility = View.GONE

        seekBar = findViewById(R.id.seekBar)

        CoroutineScope(Main).launch {
            getTime()
        }
        CoroutineScope(Main).launch {
            getWeather()
        }
        CoroutineScope(Main).launch {
            authHB()
        }
        CoroutineScope(Main).launch {
            checkMessages()
        }
        CoroutineScope(Main).launch {
            getBedroomTemperature()
        }
    }

    var messageMap = mutableMapOf<String, Int>("Hallo Welt!" to 1)


    private fun toggleSettings(){
        if (settingsLayer.visibility == View.GONE){
            settingsLayer.visibility = View.VISIBLE
        }else{
            settingsLayer.visibility = View.GONE
        }
    }

    private suspend fun checkMessages() {
        while (true) {
            var toRemove = ""
            for (i in messageMap) {
                Log.d("MESSAGEBOARD", "Key: ${i.key} Value: ${i.value}")
                if (i.value <= 0) toRemove = i.key
                else {
                    withContext(Main) {
                        Log.d("MESSAGEBOARD", "Setting Key: ${i.key}")
                        textView.text = i.key
                    }
                }
                messageMap.set(i.key, i.value - 1)
            }
            if (!toRemove.isEmpty()) {
                Log.d("MESSAGEBOARD", "Removing: $toRemove")
                messageMap.remove(toRemove)
                toRemove = ""
            }
            if (messageMap.isEmpty()) {
                withContext(Main) {
                    textView.text = ""
                }
            }
            delay(500)
        }
    }


    data class LightData(
        @SerializedName("values") val values: LightValues,

        )

    data class LightValues(
        @SerializedName("On") val On: Int,
        @SerializedName("Brightness") val Brightness: Int,
        @SerializedName("Hue") val Hue: Int,
        @SerializedName("Saturation") val Saturation: Int
    )

    data class AccessToken(
        @SerializedName("access_token") val access_token: String
    )

    data class SensorData(
        @SerializedName("state") val state: SensorState
    )
    data class SensorState(
        @SerializedName("temperature") val temperature: Int,
        @SerializedName("lastupdated") val lastUpdated: String
    )

    private fun sendCommandToggle(lampId: String) {
        val url = "http://10.10.0.2:8581/api/accessories/$lampId"
        val client = OkHttpClient().newBuilder().build()
        var hbresponse: String
        Log.d("HomeBridge/LampTop", "Checking State")

        var gson = Gson()
        var lightData: LightData

        val request = Request.Builder().url(url)
            .addHeader("Authorization", "Bearer $authKey")
            .addHeader("Content-Type", "application/json")
            .build()
//            Log.d("HomebridgeLamp/Request", request.toString())

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("HomebridgeLamp/Failure", e.toString())
            }

            @SuppressLint("LongLogTag")
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
//                        for ((name, value) in response.headers) {
//                            Log.d("HomebridgeLamp/Header", " $name, $value")
//                        }
                    lightData = gson.fromJson(response.body!!.string(), LightData::class.java)
                    Log.d("HomebridgeLamp/Response", "Current State: $lightData")
                    if (lightData.values.On == 1) {
                        sendCommandToggleOn(lampId, false)
                        messageMap.put("TURNING OFF", 4)
                    } else {
                        sendCommandToggleOn(lampId, true)
                        messageMap.put("TURNING ON", 4)
                    }
                }
            }
        })

    }
private var innenTempBedroom : Int = 0
private suspend fun getBedroomTemperature(){
    val url =
        "http://10.10.0.15/api/6DFEDF089A/sensors/2"
    val client = OkHttpClient()
    val gson = Gson()
    var sensorData: SensorData
    while (true) {
        Log.d("Innentemperatur: Schlafzimmer", "Starting Sensor bedroom loop")

        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("Sensor", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
//                            for ((name, value) in response.headers) {
//                                Log.d("Weather/Response", " $name, $value")
//                            }
                    sensorData =
                        gson.fromJson(response.body!!.string(), SensorData::class.java)
                    Log.d("Sensor/Response", sensorData.state.temperature.toString())
                    innenTempBedroom = (sensorData.state.temperature.toString().toDouble() / 100).roundToInt()
                    messageMap.put("Innensensor updated", 10)
                }
            }
        })
        delay(30000 * 10)
//        delay(5000)
    }
}

    private fun sendCommandToggleOn(lampId: String, On: Boolean) {
        val url = "http://10.10.0.2:8581/api/accessories/$lampId"
        val client = OkHttpClient().newBuilder().build()
        var hbresponse: String
        Log.d("HomeBridge/LampTop", "Sending Command ON:$On")

        var gson = Gson()
        var lightData: LightData

        val requestBody = FormBody.Builder()
            .add("characteristicType", "On")
            .add("value", "$On")
            .build()

        val request = Request.Builder().url(url)
            .addHeader("Authorization", "Bearer $authKey")
            .addHeader("Content-Type", "application/json")
            .put(requestBody)
            .build()
//            Log.d("HomebridgeLamp/Request", request.toString())

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("HomebridgeLamp/Failure", e.toString())
            }

            @SuppressLint("LongLogTag")
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
//                        for ((name, value) in response.headers) {
//                            Log.d("HomebridgeLamp/Header", " $name, $value")
//                        }
                    lightData = gson.fromJson(response.body!!.string(), LightData::class.java)
                    Log.d("HomebridgeLamp/Response", "Last State: $lightData")

                 }
            }
        })

    }

    private suspend fun authHB() {
        val urlAuth = "http://10.10.0.2:8581/api/auth/login"
        val client = OkHttpClient().newBuilder().build()
        val gson = Gson()
        var accessToken: AccessToken
        Log.d("HomeBridge", "Starting HB ")
        while (true) {

            val requestBody = FormBody.Builder()
                .add("username", "xperia-z3")
                .add("password", "fthg72")
                .build()

            val request = Request.Builder().url(urlAuth)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()
            Log.d("Homebridge/RequestTxt", request.toString())
            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    accessToken = gson.fromJson(response.body!!.string(), AccessToken::class.java)
                    Log.d("HomeBridge/Response", "New Access Token: ${accessToken.access_token}")
                    authKey = accessToken.access_token
                }

                override fun onFailure(call: Call, e: IOException) {
                    Log.d("HomeBridge/Failure", e.toString())
                }
            })

            delay(60000 * 240)
        }
    }
//    private fun checkDeviceState(uid: String) {
//        val url = "http://10.10.0.2:8581/api/accessories/$uid"
//        val client = OkHttpClient().newBuilder().build()
//        val gson = Gson()
//        var lightData: LightData
//            Log.d("HomeBridge", "Starting HB ")
//
//            fun run() {
//                val request = Request.Builder().url(url).method("GET", null)
//                    .addHeader("Authorization", "Bearer $authKey")
//                    .build()
//                Log.d("Homebridge/RequestTxt", request.toString())
//                client.newCall(request).enqueue(object : Callback {
//                    override fun onFailure(call: Call, e: IOException) {
//                        Log.d("Homebridge/Failure", e.toString())
//                    }
//
//                    override fun onResponse(call: Call, response: Response) {
//                        response.use {
//                            if (!response.isSuccessful) throw IOException("Unexpected code $response")
////                            for ((name, value) in response.headers) {
////                                Log.d("Homebridge/Header", " $name, $value")
////
////                            }
////                            hbresponse = response.body!!.string()
////                            Log.d("Homebridge/Response", hbresponse)
//                            lightData = gson.fromJson(response.body!!.string(), LightData::class.java)
//                            Log.d("Homebridge/Light", "IS LIGHT ON?? -> ${lightData.values.On}")
//                        }
//                    }
//                })
//            }
//            run()
//    }

    private var colboo: Boolean = true
    private fun setNewTime(input: String) {
        h1View.text = input.elementAt(0).toString()
        h2View.text = input.elementAt(1).toString()
        m1View.text = input.elementAt(3).toString()
        m2View.text = input.elementAt(4).toString()
        if (colboo) {
            colonView.text = ":"
            colboo = false
        } else {
            colonView.text = ""
            colboo = true
        }
    }

    private fun setNewDate(weekday: String, day: String, month: String) {
        weekdayView.text = weekday
        dayView.text = day.plus(".")
        monthView.text = month.subSequence(0, month.length - 1)
    }

    private fun setNewWeather(input: Int) {
        weatherView.text = input.toString()
    }

    private fun setNewInnenTempBedroom(input: Int){
        innenTempView.text = input.toString()
    }

    private suspend fun setTimeOnMainThread(
        input: String,
        weekday: String,
        day: String,
        month: String
    ) {
        withContext(Main) {
            setNewTime(input)
            setNewDate(weekday, day, month)
            setNewWeather(curTemp)
            setNewInnenTempBedroom(innenTempBedroom)
        }
    }


    private lateinit var date: Date

    //    private val formatter = SimpleDateFormat("MMM dd yyyy HH:mm:ss E", Locale.GERMANY)
    private val formatterTime = SimpleDateFormat("HH:mm:ss", Locale.GERMANY)
    private val formatterMonth = SimpleDateFormat("MMM", Locale.GERMANY)
    private val formatterDay = SimpleDateFormat("dd", Locale.GERMANY)
    private val formatterWeekDay = SimpleDateFormat("E", Locale.GERMANY)

    private suspend fun getTime() {
        while (true) {
            date = Date()
//            Log.d("Current Date", formatter.format(date))

            setTimeOnMainThread(
                formatterTime.format(date),
                formatterWeekDay.format(date),
                formatterDay.format(date),
                formatterMonth.format(date)
            )
            delay(1000)
        }
    }

    data class WeatherData(
        @SerializedName("main") val main: TemperatureData,
    )

    data class TemperatureData(
        @SerializedName("temp") val temp: Double,
    )

    var curTemp: Int = -33

    private suspend fun getWeather() {
        val url =
            "https://api.openweathermap.org/data/2.5/weather?id=2950157&APPID=386371523446a5a1ef1272512c75f28b"
        val client = OkHttpClient()
        val gson = Gson()
        var weatherData: WeatherData
        while (true) {
            Log.d("Weather", "Starting Weather loop")

            val request = Request.Builder().url(url).build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("Weather", e.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")
//                            for ((name, value) in response.headers) {
//                                Log.d("Weather/Response", " $name, $value")
//                            }
                        weatherData =
                            gson.fromJson(response.body!!.string(), WeatherData::class.java)
                        Log.d("Weather/Response", weatherData.main.temp.toString())
                        curTemp = (weatherData.main.temp.toString().toDouble() - 273.15).roundToInt()
                        messageMap.put("Weather updated", 10)
                    }
                }
            })
            delay(60000 * 10)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    override fun onResume() {
        super.onResume()
        hide()
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        fullscreenContentControls.visibility = View.GONE
        isFullscreen = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }


    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }
}