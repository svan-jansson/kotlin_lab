package delivery_handler

import arrow.core.*
import kotlinx.coroutines.*

class DroneFleet {
    companion object {
        val fleet = mutableMapOf(DroneType.LIGHT to 5, DroneType.MEDIUM to 4, DroneType.HEAVY to 3)

        val lock = Any()

        fun getAvailable(
                type: DroneType,
                onDeliveryCompleted: (Pair<DroneType, String>) -> Unit,
                onDroneReturned: (Pair<DroneType, String>) -> Unit
        ): Option<Pair<DroneType, String>> {
            synchronized(lock) {
                val current = fleet.getOrDefault(type, 0)

                return when {
                    current <= 0 -> None
                    else -> {
                        val timeToDeliver: Long =
                                when {
                                    type == DroneType.LIGHT -> 6
                                    type == DroneType.MEDIUM -> 8
                                    type == DroneType.HEAVY -> 10
                                    else -> 0
                                }
                        val id = droneId(type, current)
                        val toReturn = Pair(type, id)
                        fleet.put(type, current - 1)
                        scheduleReturnIn(timeToDeliver, toReturn, onDeliveryCompleted, onDroneReturned)
                        return toReturn.toOption()
                    }
                }
            }
        }

        fun droneId(type: DroneType, index: Int): String = "$type-$index"

        fun scheduleReturnIn(
                seconds: Long,
                drone: Pair<DroneType, String>,
                onDeliveryCompleted: (Pair<DroneType, String>) -> Unit,
                onDroneReturned: (Pair<DroneType, String>) -> Unit
        ) {
            GlobalScope.launch {

                // Drop off package at midpoint
                delay((seconds / 2) * 1000)
                onDeliveryCompleted(drone)

                // Return drone
                delay((seconds / 2) * 1000)
                synchronized(lock) {
                    val current = fleet.getOrDefault(drone.first, 0)
                    fleet.put(drone.first, current + 1)
                    onDroneReturned(drone)
                    println("Drone with id ${drone.second} returned after $seconds seconds")
                }
            }
        }
    }
}
