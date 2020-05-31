package tech.konkit

import io.ktor.application.*
import io.ktor.features.CORS
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.websocket.*
import io.ktor.http.cio.websocket.*
import io.ktor.sessions.*
import io.ktor.util.generateNonce
import java.time.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    val roomManager = RoomManager()

    install(io.ktor.websocket.WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    install(CORS)
    {
        method(HttpMethod.Options)
        header(HttpHeaders.XForwardedProto)
        header(HttpHeaders.AccessControlAllowOrigin)
        anyHost()
        allowCredentials = true
        allowNonSimpleContentTypes = true
    }

    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        post("/createroom") {
            val roomNumber = roomManager.createRoom()
            call.respondText(roomNumber, contentType = ContentType.Text.Plain)
        }

        // This enables the use of sessions to keep information between requests/refreshes of the browser.
        install(Sessions) {
            cookie<UserSession>("SESSION")
        }

        // This adds an interceptor that will create a specific session in each request if no session is available already.
        intercept(ApplicationCallPipeline.Features) {
            if (call.sessions.get<UserSession>() == null) {
                call.sessions.set(UserSession(generateNonce()))
            }
        }

        webSocket("/voteconnection/{roomnumber}") {
            val roomNumber = call.parameters["roomnumber"]
            if (roomNumber.isNullOrBlank()) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Room number is empty"))
                return@webSocket
            }

            val cookieSession = call.sessions.get<UserSession>()

            if (cookieSession == null) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
                return@webSocket
            }

            val userId = UserId(cookieSession.id)
            val estimationSession = roomManager.getSessionByRoomNumber(roomNumber)

            if (estimationSession == null) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "The room ${roomNumber} does not exist"))
                return@webSocket
            }

            try {
                estimationSession.onUserJoin(userId, this)

                while (true) {
                    val frame = incoming.receive()
                    if (frame is Frame.Text) {
                        estimationSession.onUserMessage(userId, frame.readText())
                    }
                }

            } finally {
                estimationSession.onUserDisconnected(userId!!, closeReason)
            }
        }
    }
}

data class UserSession(val id: String)



