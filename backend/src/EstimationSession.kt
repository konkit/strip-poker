package tech.konkit

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import kotlinx.coroutines.Deferred
import java.util.*


data class UserData(val id: String, val webSocketSession: WebSocketSession, val vote: String)

data class SelectVoteMessage(val messagetype: String, val id: String, val vote: String)

data class UserStatus(val id: String, val vote: String)
data class UserStatusMessage(val messagetype: String, val you: String, val users: List<UserStatus>)

class EstimationSession() {

    private val gson = Gson()

    private var users = mutableMapOf<String, UserData>()

    suspend fun onUserJoin(webSocketSession: WebSocketSession): UserData {
        val userData = UserData(id = UUID.randomUUID().toString(), webSocketSession = webSocketSession, vote = "")

        println("User ${userData.id} connected")

        users[userData.id] = userData

        users.forEach { d ->
            sendStateOfUsers(d.value)
        }

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

                users.forEach { d ->
                    sendStateOfUsers(d.value)
                }
            } else {
                println("Cannot change vote - user does not exist")
            }
        }
    }

    suspend fun onUserDisconnected(userData: UserData?, closeReason: Deferred<CloseReason?>) {
        println("onClose $closeReason")

        if (userData != null) {
            users.remove(userData.id)
            println("User ${userData.id} removed")
        }

        users.forEach { d -> sendStateOfUsers(d.value) }
    }

    suspend fun onError(userData: UserData?, e: Throwable, closeReason: Deferred<CloseReason?>) {
        println("onError ${closeReason.await()}")

        e.printStackTrace()

        if (userData != null) {
            users.remove(userData.id)
            println("User ${userData.id} removed")
        }

        users.forEach { d -> sendStateOfUsers(d.value) }
    }

    private suspend fun sendStateOfUsers(d: UserData) {
        val message = UserStatusMessage("userstatus", d.id, users.values.map { u -> UserStatus(u.id, u.vote) })

        d.webSocketSession.send(Frame.Text(gson.toJson(message)))
    }

}