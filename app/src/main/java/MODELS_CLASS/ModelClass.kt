package MODELS_CLASS

import com.google.gson.annotations.SerializedName

data class ModelClass(
    @SerializedName("weather") val weather:List<Weather>,
    @SerializedName("main") val main:Main,
    @SerializedName("sys") val sys:Sys,
    @SerializedName("id") val id:Int,
    @SerializedName("name") val name:String,


)
