package MODELS_CLASS

import com.google.gson.annotations.SerializedName

data class Main(
    @SerializedName("temp")val temp:Double,
    @SerializedName("feels_like") val feels_like:Double,
    @SerializedName("temp_min")val temp_min:Double,
    @SerializedName("temp_max")val temp_max:Double,
    @SerializedName("humidity") val humidity:Double,
    @SerializedName("pressure") val pressure:Double
)
