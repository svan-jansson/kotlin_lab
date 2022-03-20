package pickup_consumer

import arrow.core.*
import io.confluent.kafka.serializers.KafkaJsonDeserializerConfig.JSON_VALUE_TYPE
import java.time.Duration.ofMillis
import java.util.concurrent.ExecutionException
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig.*
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.TopicExistsException
import org.apache.kafka.common.serialization.StringDeserializer

public class PickupConsumer(brokers: String) {
    val topic = "pickup"
    val partitions = 2
    val replication: Short = 1

    val config =
            mapOf(
                    "bootstrap.servers" to brokers,
                    "retries" to 0,
                    "linger.ms" to 100,
                    KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.qualifiedName,
                    VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.qualifiedName,
                    JSON_VALUE_TYPE to String::class.java,
                    GROUP_ID_CONFIG to "pickup_consumer",
                    AUTO_OFFSET_RESET_CONFIG to "latest"
            )

    fun consume(handle: (PickupDetails) -> Unit) {

        val consumer = KafkaConsumer<String, String>(config).apply { subscribe(listOf(topic)) }

        consumer.use {
            while (true) {
                consumer.poll(ofMillis(100)).forEach {
                    val value = it.value()

                    value
                            .toOption<String>()
                            .flatMap { PickupDetails.fromJson(it) }
                            .tap { handle(it) }
                            .tapNone { println("Could not parse message: ${value}") }
                }
            }
        }
    }

    fun createTopic() {
        val newTopic = NewTopic(topic, partitions, replication)

        try {
            with(AdminClient.create(config)) { createTopics(listOf(newTopic)).all().get() }
        } catch (e: ExecutionException) {
            if (e.cause !is TopicExistsException) throw e
        }
    }
}
