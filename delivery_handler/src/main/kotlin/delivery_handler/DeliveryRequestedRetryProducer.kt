package delivery_handler

import io.confluent.kafka.serializers.KafkaJsonSerializer
import java.util.concurrent.ExecutionException
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig.*
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.errors.TopicExistsException
import org.apache.kafka.common.serialization.StringSerializer

public class DeliveryRequestedRetryProducer(brokers: String) {
    val topic = "delivery-requested-retry"
    val partitions = 2
    val replication: Short = 1

    val config =
            mapOf(
                    "bootstrap.servers" to brokers,
                    "retries" to 0,
                    "linger.ms" to 100,
                    ACKS_CONFIG to "all",
                    KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.qualifiedName,
                    VALUE_SERIALIZER_CLASS_CONFIG to KafkaJsonSerializer::class.qualifiedName
            )
    init {
        createTopic()
    }

    fun produce(retryDetails: Package) {
        KafkaProducer<String, Package>(config).use { kafkaProducer ->
            val record = ProducerRecord<String, Package>(topic, retryDetails.id, retryDetails)

            kafkaProducer.send(record)
            kafkaProducer.flush()
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
