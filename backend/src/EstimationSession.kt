package tech.konkit

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import kotlinx.coroutines.Deferred
import java.util.*
import java.util.concurrent.ConcurrentHashMap


data class UserData(val id: String, val webSocketSession: WebSocketSession, val vote: String)

interface Message {
    val messagetype: String
}
data class SelectVoteMessage(override val messagetype: String, val id: String, val vote: String) : Message
data class SendRevealMessage(override val messagetype: String, val id: String) : Message
data class SendResetMessage(override val messagetype: String, val id: String) : Message


data class UserStatusMessage(override val messagetype: String,
                             val yourId: String,
                             val leaderId: String,
                             val users: List<UserStatus>) : Message

data class UserStatus(val id: String, val vote: String)


class EstimationSession() {

    private val gson = Gson()

    private var revealed: Boolean = false
    private var users = ConcurrentHashMap<String, UserData>()
    private var leaderId: String = ""

    suspend fun onUserJoin(webSocketSession: WebSocketSession): UserData {
        val userData = UserData(id = UUID.randomUUID().toString(), webSocketSession = webSocketSession, vote = "")

        if (leaderId == "") {
            leaderId = userData.id
        }

        println("User ${userData.id} connected")

        users[userData.id] = userData

        broadcastUsersStatus()

        return userData
    }

    suspend fun onUserMessage(userData: UserData, rawMessage: String) {
        println("Incoming message : ${rawMessage}")

        val jsonObject: JsonObject = JsonParser.parseString(rawMessage).asJsonObject

        val messageType = jsonObject.get("messagetype").asString

        if (messageType == "selectvote") {
            println("Changing vote")

            val message = gson.fromJson(rawMessage, SelectVoteMessage::class.java)
            val oldValue = users[message.id]

            if (oldValue != null) {
                val newValue = oldValue.copy(vote = message.vote)

                users.replace(userData.id, newValue)

                broadcastUsersStatus()
            } else {
                println("Cannot change vote - user does not exist")
            }
        } else if (messageType == "sendreveal") {
            this.revealed = true

            broadcastUsersStatus()
        } else if (messageType == "sendreset") {
            this.revealed = false

            users.forEach { d ->
                users.replace(d.key, d.value.copy(vote = ""))
            }

            broadcastUsersStatus()
        }
    }

    fun hasLeaderLeft(): Boolean {
        return users.contains(leaderId)
    }

    suspend fun disconnectEverybody() {
        users.forEach { d ->
            try {
                d.value.webSocketSession.close()

            } catch(e: Throwable) {
                println("Could not send statuses of users - $e")
            }
        }
    }

    suspend fun onUserDisconnected(userData: UserData?, closeReason: Deferred<CloseReason?>) {
        println("onClose $closeReason")

        if (userData != null) {
            users.remove(userData.id)
            println("User ${userData.id} removed")
        }

        broadcastUsersStatus()
    }

    suspend fun onError(userData: UserData?, e: Throwable, closeReason: Deferred<CloseReason?>) {
        println("onError ${closeReason.await()}")

        e.printStackTrace()

        if (userData != null) {
            users.remove(userData.id)
            println("User ${userData.id} removed")
        }

        broadcastUsersStatus()
    }

    private suspend fun broadcastUsersStatus() {
        users.forEach { d ->
            try {
                val allVotesFilled = users.values.all { v -> v.vote.isNotBlank() }

                val userStatuses = users.values.map { u ->
                    if (allVotesFilled || revealed) {
                        UserStatus(u.id, u.vote)
                    } else if (u.vote.isBlank()) {
                        UserStatus(u.id, "")
                    } else {
                        UserStatus(u.id, "X")
                    }
                }

                val message = UserStatusMessage("userstatus", d.value.id, leaderId, userStatuses)

                d.value.webSocketSession.send(Frame.Text(gson.toJson(message)))
            } catch(e: Throwable) {
                println("Could not send statuses of users - $e")
            }
        }
    }

}