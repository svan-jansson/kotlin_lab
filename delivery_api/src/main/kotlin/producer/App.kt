package delivery_api

import java.util.*

fun main() {
    var brokers = System.getenv("BROKERS")
    if (brokers == null) {
        brokers = "kafka:9092"
    }

    println("Starting Kafka producer for broker(s) [${brokers}]...")

    val producer = DeliveryRequestedProducer(brokers)

    fun getDetails(): Package {
        val items = Item.randomList()

        return Package(
                id = UUID.randomUUID().toString(),
                contents = items.map { it.name },
                weight = items.map { it.weight }.sum()
        )
    }

    producer.produce(
            Int.MAX_VALUE,
            fun(): Package {
                val details = getDetails()
                println("Delivery requested: $details")
                return details
            }
    )
}
