package producer

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig.*
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import io.confluent.kafka.serializers.KafkaJsonSerializer
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.errors.TopicExistsException
import java.util.concurrent.ExecutionException

public class PickupProducer(brokers:String) {
    val topic = "pickup3"
    val partitions = 2
    val replication: Short = 1

    val config = mapOf(
        "bootstrap.servers" to brokers,
        "retries" to 0,
        "linger.ms" to 100,
        ACKS_CONFIG to "all",
        KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.qualifiedName,
        VALUE_SERIALIZER_CLASS_CONFIG to KafkaJsonSerializer::class.qualifiedName)

    fun produce(numMessages: Int, getDetails: () -> PickupDetails) {
        
        //createTopic()

        KafkaProducer<String, PickupDetails>(config).use { kafkaProducer ->
            repeat(numMessages) { _ ->
                val details = getDetails()
                val record = ProducerRecord<String, PickupDetails>(topic, details.id, details)

                kafkaProducer.send(record) { m: RecordMetadata?, e: Exception? ->
                    when (e) {
                        null -> println("Produced record to topic ${m?.topic()} partition [${m?.partition()}] @ offset ${m?.offset()}")
                        else -> e.printStackTrace()
                    }
                }
            }

            kafkaProducer.flush()
        }
    }

    fun createTopic() {
        val newTopic = NewTopic(topic, partitions, replication)

        try {
            with(AdminClient.create(config)) {
                createTopics(listOf(newTopic)).all().get()
            }
        } catch (e: ExecutionException) {
            if (e.cause !is TopicExistsException) throw e
        }
    }
}