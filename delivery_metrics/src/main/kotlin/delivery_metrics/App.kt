package delivery_metrics

import arrow.core.*
import com.google.gson.Gson
import dev.evo.prometheus.ktor.metricsModule
import io.confluent.kafka.serializers.KafkaJsonDeserializerConfig.JSON_VALUE_TYPE
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.time.Duration.ofMillis
import kotlinx.coroutines.*
import kotlinx.coroutines.GlobalScope
import org.apache.kafka.clients.consumer.ConsumerConfig.*
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer

val gson = Gson()

fun main() {
    var brokers = System.getenv("BROKERS") ?: "kafka:9092"
    var port = System.getenv("PORT")?.toInt() ?: 8081

    val logData = mapOf("Kafka Brokers" to brokers, "HTTP port" to port)

    println("Starting Delivery Metrics with settings: $logData")

    startMetricsServer(port)
    collectMetrics(brokers)
}

fun startMetricsServer(port: Int) =
        embeddedServer(Netty, port = port, module = { metricsModule(DeliveryMetrics) })
                .start(wait = false)

fun collectMetrics(brokers: String) {
    val topics =
            listOf(
                    "delivery-requested",
                    "delivery-requested-retry",
                    "package-in-transit",
                    "package-delivered",
                    "drone-returned"
            )

    val config =
            mapOf(
                    "bootstrap.servers" to brokers,
                    "retries" to 0,
                    "linger.ms" to 100,
                    KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.qualifiedName,
                    VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.qualifiedName,
                    JSON_VALUE_TYPE to String::class.java,
                    GROUP_ID_CONFIG to "delivery_metrics",
                    AUTO_OFFSET_RESET_CONFIG to "latest"
            )

    val consumer = KafkaConsumer<String, String>(config).apply { subscribe(topics) }

    consumer.use {
        while (true) {
            consumer.poll(ofMillis(100)).forEach {
                val kafkaTopic = it.topic()

                GlobalScope.launch {
                    println("measuring event of type $kafkaTopic")
                    DeliveryMetrics.deliveryEvents.inc { topic = kafkaTopic }

                    if (kafkaTopic == "package-delivered") {
                        val json = it.value()
                        val data = gson.fromJson(json, Map::class.java)

                        val droneId = data["id"] as String
                        val droneType = data["type"] as String
                        val parcel = data["carrying"] as Map<*, *>
                        val weight = parcel["weight"] as Double

                        DeliveryMetrics.weightCounter.add(weight) {
                            id = droneId
                            type = droneType
                        }

                        DeliveryMetrics.deliveryCounter.add(1.0) {
                            id = droneId
                            type = droneType
                        }
                    }

                    if (kafkaTopic == "package-in-transit") {
                        val json = it.value()
                        val data = gson.fromJson(json, Map::class.java)

                        val droneId = data["id"] as String
                        val droneType = data["type"] as String

                        DeliveryMetrics.droneActiveGauge.set(1.0) { 
                            id = droneId
                            type = droneType
                        }
                    }

                    if (kafkaTopic == "drone-returned") {
                        val json = it.value()
                        val data = gson.fromJson(json, Map::class.java)

                        val droneId = data["id"] as String
                        val droneType = data["type"] as String

                        DeliveryMetrics.droneActiveGauge.set(0.0) { 
                            id = droneId
                            type = droneType
                        }
                    }
                }
            }
        }
    }
}
