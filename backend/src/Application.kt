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
import io.ktor.util.pipeline.PipelineContext
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
            val userId = getUserIdFromSession2(call)

            println("Creating room, userId: ${userId}")

            val roomNumber = roomManager.createRoom(userId)
            call.respondText(roomNumber, contentType = ContentType.Text.Plain)
        }

        // This enables the use of sessions to keep information between requests/refreshes of the browser.
        install(Sessions) {
            cookie<UserSession>("USERSESSION")
        }

        // This adds an interceptor that will create a specific session in each request if no session is available already.
        intercept(ApplicationCallPipeline.Features) {

            if (call.sessions.get<UserSession>() == null) {
                call.sessions.set(UserSession(generateNonce()))
            }

            println("Intercepting, userId ${call.sessions.get<UserSession>()!!.id}")
        }

        webSocket("/voteconnection/{roomnumber}") {
            val roomNumber = call.parameters["roomnumber"]
            if (roomNumber.isNullOrBlank()) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Room number is empty"))
                return@webSocket
            }

            val room = roomManager.getSessionByRoomNumber(roomNumber)

            if (room == null) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "The room ${roomNumber} does not exist"))
                return@webSocket
            }

            val userId = getUserIdFromSession(call)

            println("Joining room, userId: ${userId}")

            try {
                room.onUserJoin(userId, this)

                while (true) {
                    val frame = incoming.receive()
                    if (frame is Frame.Text) {
                        room.onUserMessage(userId, frame.readText())
                    }
                }

            } finally {
                room.onUserDisconnected(userId)
            }
        }
    }
}

private fun getUserIdFromSession(call: ApplicationCall): UserId {
    println("Call: ${call}")

    val cookieSession = call.sessions.get<UserSession>()

    if (cookieSession != null) {
        return UserId(cookieSession.id)
    } else {
        throw Exception("Session is not initialized!")
    }
}

private fun getUserIdFromSession2(call: ApplicationCall): UserId {
    println("Call: ${call}")

    val cookieSession = call.sessions.get<UserSession>()

    if (cookieSession != null) {
        return UserId(cookieSession.id)
    } else {
        throw Exception("Session is not initialized!")
    }
}

data class UserSession(val id: String)



