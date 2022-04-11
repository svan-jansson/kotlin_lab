package delivery_handler

import arrow.core.toOption
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val gson = Gson()

fun main() {
    var brokers = System.getenv("BROKERS")
    if (brokers == null) {
        brokers = "kafka:9092"
    }

    println("Starting Kafka consumer for broker(s) [${brokers}]...")

    val consumer = DeliveryRequestedConsumer(brokers)
    val inDeliveryProducer = PackageInTransitProducer(brokers)
    val retryProducer = DeliveryRequestedRetryProducer(brokers)
    val packageDeliveredProducer = PackageDeliveredProducer(brokers)
    val droneReturnedProducer = DroneReturnedProducer(brokers)

    consumer.consume {
        println("Delivery request received: $it")
        handle(it, inDeliveryProducer, retryProducer, packageDeliveredProducer, droneReturnedProducer)
    }
}

fun handle(
    details: Package,
    inDeliveryProducer: PackageInTransitProducer,
    retryProducer: DeliveryRequestedRetryProducer,
    packageDeliveredProducer: PackageDeliveredProducer,
    droneReturnedProducer: DroneReturnedProducer
) {
    details.toOption()
        .map { droneTypeByWeight(it.weight) }
        .flatMap {
            val packageDelivered = fun(drone: Pair<DroneType, String>) {
                packageDeliveredProducer.produce(drone, details)
            }

            val droneReturned = fun(drone: Pair<DroneType, String>) {
                droneReturnedProducer.produce(drone)
            }

            DroneFleet.getAvailable(it, packageDelivered, droneReturned)
        }
        .map { DroneDetails(it.second, it.first, details) }
        .tap { println("Package in transit with drone: ${it.id}") }
        .tap { inDeliveryProducer.produce(it) }
        .tapNone {
            val retryIn = 5
            println("No drones available. Retrying in $retryIn seconds...")

            GlobalScope.launch {
                delay(retryIn * 1000L)
                retryProducer.produce(details)
            }
        }
}

fun droneTypeByWeight(weight: Double): DroneType =
    when {
        weight < 0.5 -> DroneType.LIGHT
        weight < 1.0 -> DroneType.MEDIUM
        else -> DroneType.HEAVY
    }
