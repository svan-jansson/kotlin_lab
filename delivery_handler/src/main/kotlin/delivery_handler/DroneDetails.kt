package delivery_handler

data class DroneDetails(val id: String, val type: DroneType, val carrying: Package) {
    override fun toString(): String = gson.toJson(this)
}

enum class DroneType {
    LIGHT,
    MEDIUM,
    HEAVY
}
