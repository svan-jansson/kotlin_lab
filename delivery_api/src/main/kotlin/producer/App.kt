package delivery_api

import java.util.*

fun main() {
    var brokers = System.getenv("BROKERS")
    if (brokers == null) {
        brokers = "kafka:9092"
    }

    println("Starting Kafka producer for broker(s) [${brokers}]...")

    val producer = PickupProducer(brokers)

    fun getDetails(): PickupDetails {
        val items = Item.randomList()

        return PickupDetails(
                id = UUID.randomUUID().toString(),
                contents = items.map { it.name },
                weight = items.map { it.weight }.sum()
        )
    }

    producer.produce(
            Int.MAX_VALUE,
            fun(): PickupDetails {
                val details = getDetails()
                println("Sending pickup order: $details")
                return details
            }
    )
}
