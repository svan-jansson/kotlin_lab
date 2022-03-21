package delivery_handler

import arrow.core.*

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
