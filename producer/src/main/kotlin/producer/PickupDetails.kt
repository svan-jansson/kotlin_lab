package producer

import com.google.gson.Gson

val gson = Gson()

data class PickupDetails(val id: String, val contents: Collection<String>, val weight: Double) {
    override fun toString(): String {
        return gson.toJson(this)
    }
}
