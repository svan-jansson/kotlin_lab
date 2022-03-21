package delivery_api

import arrow.core.*
import java.util.*
import org.litote.kmongo.*

enum class Status {
    PICKUP_REQUESTED,
    IN_TRANSIT,
    DELIVERED
}

data class PackageStatus(val parcel: Package, val status: Status) {
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
            val packageStatus = PackageStatus(parcel, Status.PICKUP_REQUESTED)
            collection.insertOne(packageStatus.toString())

            return Some(packageStatus)
        } catch (ex: Exception) {
            println("could not insert into database: $ex")
            return None
        }
    }
}
