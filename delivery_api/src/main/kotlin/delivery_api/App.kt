package delivery_api

import arrow.core.*
import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.util.*

val gson = Gson()

fun main() {
    var brokers = System.getenv("BROKERS") ?: "kafka:9092"
    var port = System.getenv("PORT")?.toInt() ?: 8080
    var connectionString =
            System.getenv("CONNECTION_STRING") ?: "mongodb://lab:SuperSecret123@localhost:27017"

    val logData = mapOf("Kafka Brokers" to brokers, "HTTP port" to port)

    println("Starting Delivery API with settings: $logData")

    val eventProducer = DeliveryRequestedProducer(brokers)
    val repository = Repository(connectionString)

    embeddedServer(Netty, port) {
                routing {
                    route("delivery-request") {
                        get("{id}") {
                            call.parameters["id"]
                                    .toOption()
                                    .flatMap { repository.getPackageStatus(it) }
                                    .map { it.toString() }
                                    .tap { call.respondText(it, ContentType.Application.Json) }
                                    .tapNone { call.respond(HttpStatusCode.NotFound) }
                        }

                        post {
                            Package.random()
                                    .toOption()
                                    .flatMap { repository.storeRequest(it) }
                                    .tap { eventProducer.produce(it.parcel) }
                                    .map { it.toString() }
                                    .tap { call.respondText(it, ContentType.Application.Json) }
                                    .tapNone { call.respond(HttpStatusCode.InternalServerError) }
                        }
                    }
                }
            }
            .start(wait = true)
}
