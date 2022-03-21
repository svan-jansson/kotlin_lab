package delivery_handler

import arrow.core.*
import kotlinx.coroutines.*

class DroneFleet {
    companion object {
        val fleet = mutableMapOf(DroneType.LIGHT to 3, DroneType.MEDIUM to 2, DroneType.HEAVY to 1)

        val lock = Any()

        fun getAvailable(type: DroneType): Option<Pair<DroneType, String>> {
            synchronized(lock) {
                val current = fleet.getOrDefault(type, 0)

                return when {
                    current <= 0 -> None
                    else -> {
                        fleet.put(type, current - 1)
                        scheduleReturnIn(10, type)
                        return Pair(type, droneId(type, current)).toOption()
                    }
                }
            }
        }

        fun droneId(type: DroneType, index: Int): String = "$type-$index"

        fun scheduleReturnIn(seconds: Long, type: DroneType) {
            GlobalScope.launch {
                delay(seconds * 1000)
                synchronized(lock) {
                    val current = fleet.getOrDefault(type, 0)
                    fleet.put(type, current + 1)
                    println("Drone of type $type returned after $seconds seconds")
                }
            }
        }
    }
}
