package pickup_consumer

data class DroneDetails(val id: String, val type: DroneType, val carrying: PickupDetails) {
    override fun toString(): String = gson.toJson(this)
}

enum class DroneType {
    LIGHT,
    MEDIUM,
    HEAVY
}
