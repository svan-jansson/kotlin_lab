package delivery_state

import com.google.gson.Gson
import io.confluent.kafka.serializers.KafkaJsonDeserializerConfig.JSON_VALUE_TYPE
import org.apache.kafka.clients.consumer.ConsumerConfig.*
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration.ofMillis
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

val gson = Gson()

fun main() {
    var brokers = System.getenv("BROKERS") ?: "kafka:9092"
    var connectionString =
        System.getenv("CONNECTION_STRING") ?: "mongodb://lab:SuperSecret123@localhost:27017"

    val logData = mapOf("Kafka Brokers" to brokers, "Connection String" to connectionString)

    println("Starting Delivery State with settings: $logData")

    val repository = Repository(connectionString)
    startSourcingState(brokers, repository)
}

fun startSourcingState(brokers: String, repository: Repository) {
    val topics =
        listOf(
            "delivery-requested",
            "delivery-requested-retry",
            "package-in-transit",
            "package-delivered"
        )

    val config =
        mapOf(
            "bootstrap.servers" to brokers,
            "retries" to 0,
            "linger.ms" to 100,
            KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.qualifiedName,
            VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.qualifiedName,
            JSON_VALUE_TYPE to String::class.java,
            GROUP_ID_CONFIG to "delivery_state",
            AUTO_OFFSET_RESET_CONFIG to "latest"
        )

    val consumer = KafkaConsumer<String, String>(config).apply { subscribe(topics) }

    consumer.use {
        while (true) {
            consumer.poll(ofMillis(100)).forEach {
                val kafkaTopic = it.topic()
                val json = it.value()
                val data = gson.fromJson(json, Map::class.java)

                val id = getPackageId(kafkaTopic, data)
                val droneId = getDroneId(kafkaTopic, data)
                val status = getStatus(kafkaTopic)
                val event = Event(getTimestamp(), status, droneId)

                if (id != null) {
                    repository.storeRequest(id, status, event)
                }
            }
        }
    }
}

fun getTimestamp(): String =
    Instant.now().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_ZONED_DATE_TIME)

fun getStatus(topic: String): Status =
    when {
        topic == "delivery-requested" -> Status.WAITING_FOR_PICKUP
        topic == "delivery-requested-retry" -> Status.DELAYED
        topic == "package-in-transit" -> Status.IN_TRANSIT
        topic == "package-delivered" -> Status.DELIVERED
        else -> Status.UNKOWN
    }

fun getPackageId(topic: String, payload: Map<*, *>): String? =
    when {
        topic == "delivery-requested" -> payload["id"] as String
        topic == "delivery-requested-retry" -> payload["id"] as String
        topic == "package-in-transit" -> (payload["carrying"] as Map<*, *>)["id"] as String
        topic == "package-delivered" -> (payload["carrying"] as Map<*, *>)["id"] as String
        else -> null
    }

fun getDroneId(topic: String, payload: Map<*, *>): String? =
    when {
        topic == "package-in-transit" -> payload["id"] as String
        topic == "package-delivered" -> payload["id"] as String
        else -> null
    }
