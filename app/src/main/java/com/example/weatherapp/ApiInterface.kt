package com.example.weatherapp

import MODELS_CLASS.ModelClass
import android.telecom.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {

    @GET("weather")
    fun getCityWeatherData(
        @Query("q") cityName:String,
        @Query("appid")API_key:String
    ):retrofit2.Call<ModelClass>

    //https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={API key}

    @GET("weather")
    fun getCurrentWeather(
        @Query("lat") lat:String,
        @Query("lon") lon:String,
        @Query("appid")API_key:String
    ):retrofit2.Call<ModelClass>
}