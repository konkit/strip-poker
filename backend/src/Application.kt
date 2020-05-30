package tech.konkit

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.websocket.*
import io.ktor.http.cio.websocket.*
import io.ktor.sessions.sessions
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import java.time.*
import java.util.*



val estmiationSession = EstimationSession()

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

        webSocket("/myws") {
            var userData: UserData? = null

            try {
                userData = estmiationSession.onUserJoin(this)

                while (true) {
                    val frame = incoming.receive()
                    if (frame is Frame.Text) {
                        estmiationSession.onUserMessage(userData, frame.readText())
//                        send(Frame.Text("Client said: " + frame.readText()))
                    }
                }

            } catch (e: ClosedReceiveChannelException) {
                estmiationSession.onUserDisconnected(userData, closeReason)
            } catch (e: Throwable) {
                estmiationSession.onError(userData, e, closeReason)
            }
        }
    }
}

