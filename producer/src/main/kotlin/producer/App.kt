package producer

import java.util.*

fun main() {
    println("Starting Kafka producer...")

    val producer = PickupProducer("kafka:9092")
    
    fun getDetails(): PickupDetails {
        return PickupDetails(
            id = UUID.randomUUID().toString(),
            contents = listOf("a screwdriver", "diapers", "toilet paper"),
            weight = 100.0)
    }

    producer.produce(5, fun(): PickupDetails {
        val details = getDetails()
        println("Sending pickup order: $details")
        return details
    })
}
