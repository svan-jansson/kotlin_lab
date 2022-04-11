package delivery_api

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.toOption
import org.litote.kmongo.*
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

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

data class PackageStatus(val parcel: Package, val status: Status, val eventLog: List<Event>) {
    override fun toString(): String {
        return gson.toJson(this)
    }
}

public class Repository(val connectionString: String) {

    val client = KMongo.createClient(connectionString)
    val database = client.getDatabase("deliveries")

    fun getPackageStatus(id: String): Option<PackageStatus> {
        try {
            val collection = database.getCollection<PackageStatus>("packages")
            val packageStatus =
                collection.findOne(PackageStatus::parcel / Package::id eq id).toOption()

            return packageStatus
        } catch (ex: Exception) {
            println("could not insert into database: $ex")
            return None
        }
    }

    fun storeRequest(parcel: Package): Option<PackageStatus> {
        try {
            val collection = database.getCollection("packages")
            val packageStatus =
                PackageStatus(
                    parcel,
                    Status.REGISTERED,
                    listOf(
                        Event(
                            Instant.now()
                                .atZone(ZoneOffset.UTC)
                                .format(DateTimeFormatter.ISO_ZONED_DATE_TIME),
                            Status.REGISTERED,
                            null
                        )
                    )
                )
            collection.insertOne(packageStatus.toString())

            return Some(packageStatus)
        } catch (ex: Exception) {
            println("could not insert into database: $ex")
            return None
        }
    }
}
