package tech.konkit

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import java.time.*
import java.util.concurrent.ConcurrentHashMap


val sessions: ConcurrentHashMap<String, EstimationSession> = ConcurrentHashMap()

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(io.ktor.websocket.WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        webSocket("/voteconnection/{roomnumber}") {
            val roomNumber = call.parameters["roomnumber"]
            if (roomNumber.isNullOrBlank()) {
                throw Exception("Room number empty")
            }

            var userData: UserData? = null

            val estimationSession: EstimationSession = if (!sessions.containsKey(roomNumber)) {
                val newSession = EstimationSession()
                sessions.put(roomNumber, newSession)
                newSession
            } else {
                sessions.get(roomNumber)!!
            }

            try {
                userData = estimationSession.onUserJoin(this)

                while (true) {
                    val frame = incoming.receive()
                    if (frame is Frame.Text) {
                        estimationSession.onUserMessage(userData, frame.readText())
                    }
                }

            } catch (e: ClosedReceiveChannelException) {
                estimationSession.onUserDisconnected(userData, closeReason)
                if (estimationSession.hasLeaderLeft()) {
                    estimationSession.disconnectEverybody()
                    sessions.remove(roomNumber)

                    println("Removed room ${roomNumber}, ${sessions.count()} rooms left")
                }
            } catch (e: Throwable) {
                estimationSession.onError(userData, e, closeReason)
                if (estimationSession.hasLeaderLeft()) {
                    estimationSession.disconnectEverybody()
                    sessions.remove(roomNumber)

                    println("Removed room ${roomNumber}, ${sessions.count()} rooms left")
                }
            }
        }
    }
}

