package pickup_consumer

import arrow.core.*
import com.google.gson.Gson

val gson = Gson()

data class PickupDetails(val id: String, val contents: Collection<String>, val weight: Double) {
    companion object {
        fun fromJson(json: String): Option<PickupDetails> {
            try {
                return gson.fromJson(json, PickupDetails::class.java).toOption()
            } catch (ex: com.google.gson.JsonSyntaxException) {
                return None
            }
        }
    }
    override fun toString(): String = gson.toJson(this)
}
