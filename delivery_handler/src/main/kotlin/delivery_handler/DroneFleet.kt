package delivery_handler

import arrow.core.*
import kotlinx.coroutines.*

class DroneFleet {
    companion object {
        val fleet = mutableMapOf(DroneType.LIGHT to 2, DroneType.MEDIUM to 2, DroneType.HEAVY to 2)

        val lock = Any()

        fun getAvailable(
                type: DroneType,
                onDeliveryCompleted: (Pair<DroneType, String>) -> Unit
        ): Option<Pair<DroneType, String>> {
            synchronized(lock) {
                val current = fleet.getOrDefault(type, 0)

                return when {
                    current <= 0 -> None
                    else -> {
                        val id = droneId(type, current)
                        val toReturn = Pair(type, id)
                        fleet.put(type, current - 1)
                        scheduleReturnIn(10, toReturn, onDeliveryCompleted)
                        return toReturn.toOption()
                    }
                }
            }
        }

        fun droneId(type: DroneType, index: Int): String = "$type-$index"

        fun scheduleReturnIn(
                seconds: Long,
                drone: Pair<DroneType, String>,
                onDeliveryCompleted: (Pair<DroneType, String>) -> Unit
        ) {
            GlobalScope.launch {
                delay(seconds * 1000)
                synchronized(lock) {
                    val current = fleet.getOrDefault(drone.first, 0)
                    fleet.put(drone.first, current + 1)
                    println("Drone with id ${drone.second} returned after $seconds seconds")
                    onDeliveryCompleted(drone)
                }
            }
        }
    }
}
