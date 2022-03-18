package pickup_consumer

import java.util.*

fun main() {
    var brokers = System.getenv("BROKERS")
    if (brokers == null) {
        brokers = "kafka:9092"
    }

    println("Starting Kafka consumer for broker(s) [${brokers}]...")

    val consumer = PickupConsumer(brokers)
}
