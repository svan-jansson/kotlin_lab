package pickup_consumer

import com.google.gson.Gson

val gson = Gson()

data class PickupDetails(val id: String, val contents: Collection<String>, val weight: Double) {
    companion object {
        fun fromJson(json: String): PickupDetails = gson.fromJson(json, PickupDetails::class.java)
    }
    override fun toString(): String = gson.toJson(this)
}
