package delivery_handler

import arrow.core.toOption
import io.confluent.kafka.serializers.KafkaJsonDeserializerConfig.JSON_VALUE_TYPE
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig.*
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.TopicExistsException
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration.ofMillis
import java.util.concurrent.ExecutionException

public class DeliveryRequestedConsumer(brokers: String) {
    val topics = listOf("delivery-requested", "delivery-requested-retry")
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
            GROUP_ID_CONFIG to "delivery_handler",
            AUTO_OFFSET_RESET_CONFIG to "latest"
        )

    fun consume(handle: (Package) -> Unit) {

        val consumer = KafkaConsumer<String, String>(config).apply { subscribe(topics) }

        consumer.use {
            while (true) {
                consumer.poll(ofMillis(100)).forEach {
                    val value = it.value()

                    value.toOption<String>()
                        .flatMap { Package.fromJson(it) }
                        .tap { handle(it) }
                        .tapNone { println("Could not parse message: ${value}") }
                }
            }
        }
    }

    fun createTopics() {
        topics.forEach {
            val newTopic = NewTopic(it, partitions, replication)

            try {
                with(AdminClient.create(config)) { createTopics(listOf(newTopic)).all().get() }
            } catch (e: ExecutionException) {
                if (e.cause !is TopicExistsException) throw e
            }
        }
    }
}
