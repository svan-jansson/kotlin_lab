package delivery_handler

import arrow.core.*

data class Package(val id: String, val contents: Collection<String>, val weight: Double) {
    companion object {
        fun fromJson(json: String): Option<Package> {
            try {
                return gson.fromJson(json, Package::class.java).toOption()
            } catch (ex: com.google.gson.JsonSyntaxException) {
                return None
            }
        }
    }
    override fun toString(): String = gson.toJson(this)
}
