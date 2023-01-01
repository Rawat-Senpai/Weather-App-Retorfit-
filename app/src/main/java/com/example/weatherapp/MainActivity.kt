package com.example.weatherapp

import MODELS_CLASS.ModelClass
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.Display.Mode
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        getCurrentLocation()

        findCurrentDate()

        serchWeather.setOnClickListener(){
            val cityName=getCityName.text.toString()
            if(cityName.isNotEmpty()){
                getCityWeather(cityName)
            }
            else
            {
                Toast.makeText(this@MainActivity,"Please enter city name first",Toast.LENGTH_SHORT).show()
            }
        }
    }

    //finding location
    @RequiresApi(Build.VERSION_CODES.P)
    private fun getCurrentLocation() {

        if(checkPermission())
        {

            if(isLocationEnabled()){

                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this){task->
                    val location:Location?=task.result
                    if(location==null)
                    {
                        Toast.makeText(this, "Null Result", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        val latitude=location.latitude
                        val longitude=location.longitude
                     //   Toast.makeText(this, latitude.toString(), Toast.LENGTH_SHORT).show()
                        getCurrentWeather(latitude.toString(),longitude.toString())
                    }
                }


            }else{
                // setting open here
                Toast.makeText(this, "Turn on Location", Toast.LENGTH_SHORT).show()
                val intent=Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        }else{
            // request the permission
            requestPermission()
        }
    }

    private fun getCurrentWeather(latitude: String, longitude: String) {
ApiUtilities.getApiInterface()?.getCurrentWeather(latitude,longitude, API_KEY)?.enqueue(object :Callback<ModelClass>
{
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResponse(call: Call<ModelClass>, response: Response<ModelClass>) {
        setDataView(response.body())
    }
    override fun onFailure(call: Call<ModelClass>, t: Throwable) {
        Toast.makeText(this@MainActivity, "Getting some error in fetching data from api ", Toast.LENGTH_SHORT).show()
    }

})
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun isLocationEnabled():Boolean{
        val locationManager:LocationManager=getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )

    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode== PERMISSION_REQUEST_ACCESS_LOCATION)
        {
            if(grantResults.isNotEmpty()  && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(applicationContext,"Granted",Toast.LENGTH_SHORT).show()
                getCurrentLocation()
            }
            else{
                Toast.makeText(applicationContext, "Denied", Toast.LENGTH_SHORT).show()
            }
        }

    }


    private fun checkPermission():Boolean{

        if(ActivityCompat.checkSelfPermission(this,
            android.Manifest.permission.ACCESS_COARSE_LOCATION)
            ==PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
            android.Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
        {
            return true

        }
        return false
    }

    private fun findCurrentDate() {
        progressBar.visibility=View.VISIBLE
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val currentDate = sdf.format(Date())
        data_and_time.text=currentDate.toString()
        progressBar.visibility=View.GONE
    }

    private fun getCityWeather(cityName: String) {
        ApiUtilities.getApiInterface()?.getCityWeatherData(cityName, API_KEY)?.enqueue(object : Callback<ModelClass>
        {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<ModelClass>, response: Response<ModelClass>) {
                setDataView(response.body())
            }
            override fun onFailure(call: Call<ModelClass>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Getting some error in fetching data from api ", Toast.LENGTH_SHORT).show()
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataView(body: ModelClass?) {
        if (body != null) {
            day_max_temp.text="Day-temperature: "+kelvinToCelsius(body.main.temp_max)
            day_min_temp.text="Night-temperature: "+kelvinToCelsius(body.main.temp_min)
            tv_temp.text=" "+kelvinToCelsius(body.main.temp)
            feelsLike.text=""+kelvinToCelsius(body.main.feels_like)
            current_mausam.text=body.weather[0].main
            cityHumidity.text=body.main.humidity.toString()
            seaLevel.text=body.main.humidity.toString()
            sunRise.text="" +timeStampToDate(body.sys.sunrise.toLong())
        }
    }
    companion object{
        private const val PERMISSION_REQUEST_ACCESS_LOCATION=100
        const val API_KEY="c72c574e3e5f3a228ac56f0ca7cd968e"
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun timeStampToDate(timeStamp:Long):String{

        val localTime=timeStamp.let {
            Instant.ofEpochSecond(it)
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
        }
        return localTime.toString()

    }

    private fun kelvinToCelsius(temp:Double):Double{

        var intTemp=temp
        intTemp=intTemp.minus(273)

        return intTemp.toBigDecimal().setScale(1,RoundingMode.UP).toDouble()
    }

}