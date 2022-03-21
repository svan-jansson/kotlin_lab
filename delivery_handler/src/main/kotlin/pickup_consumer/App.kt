package delivery_handler

import arrow.core.*
import com.google.gson.Gson
import java.util.*
import kotlinx.coroutines.*

val gson = Gson()

fun main() {
    var brokers = System.getenv("BROKERS")
    if (brokers == null) {
        brokers = "kafka:9092"
    }

    println("Starting Kafka consumer for broker(s) [${brokers}]...")

    val consumer = PickupConsumer(brokers)
    val inDeliveryProducer = InDeliveryProducer(brokers)
    val retryProducer = RetryProducer(brokers)

    consumer.consume {
        println("Received: $it")
        handle(it, inDeliveryProducer, retryProducer)
    }
}

fun handle(
        details: PickupDetails,
        inDeliveryProducer: InDeliveryProducer,
        retryProducer: RetryProducer
) {
    details.toOption()
            .map { droneTypeByWeight(it.weight) }
            .flatMap { DroneFleet.getAvailable(it) }
            .map { DroneDetails(it.second, it.first, details) }
            .tap { println("Out for delivery with drone: ${it.id}") }
            .tap { inDeliveryProducer.produce(it) }
            .tapNone { println("No drones available") }
            .tapNone {
                GlobalScope.launch {
                    delay(30_000)
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
