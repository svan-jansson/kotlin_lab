package delivery_state

import org.litote.kmongo.KMongo
import org.litote.kmongo.updateOne

enum class Status {
    UNKOWN,
    REGISTERED,
    WAITING_FOR_PICKUP,
    DELAYED,
    IN_TRANSIT,
    DELIVERED
}

data class Event(val timestamp: String, val status: Status, val droneId: String?) {
    override fun toString(): String {
        return gson.toJson(this)
    }
}

public class Repository(val connectionString: String) {

    val client = KMongo.createClient(connectionString)
    val database = client.getDatabase("deliveries")

    fun storeRequest(id: String, status: Status, event: Event) {
        try {
            val collection = database.getCollection("packages")
            val dronePart =
                when {
                    event.droneId == null -> ""
                    else -> ", droneId: '${event.droneId}'"
                }
            val updateQuery =
                """
            { 
                ${'$'}set: { status: '$status' },
                ${'$'}push: {
                    eventLog: {
                        timestamp: '${event.timestamp}',
                        status: '${event.status}'
                        ${dronePart}
                        }
                     }
             }
            """.trimIndent()

            collection.updateOne("{'parcel.id':'$id'}", updateQuery)

            println("updated status of parcel $id to $status")
        } catch (ex: Exception) {
            println("could not insert into database: $ex")
        }
    }
}
