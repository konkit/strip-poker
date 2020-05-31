package tech.konkit

import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import kotlinx.coroutines.Deferred
import java.util.*
import java.util.concurrent.ConcurrentHashMap

data class UserId(val id: String) {
    override fun toString(): String {
        return id.toString()
    }

    companion object {
        val empty = UserId("")

        fun generate() = UserId(UUID.randomUUID().toString())
    }
}

data class UserData(val webSocketSession: WebSocketSession, val vote: String)

class EstimationSession() {

    private val serializer = MessageSerializer()

    private var revealed: Boolean = false
    private var users = ConcurrentHashMap<UserId, UserData>()
    private var leaderId: UserId = UserId.empty

    suspend fun onUserJoin(webSocketSession: WebSocketSession): UserId {
        val userId = UserId.generate()
        val userData = UserData(webSocketSession = webSocketSession, vote = "")

        if (leaderId == UserId.empty) {
            leaderId = userId
        }

        println("User ${userId} connected")

        users[userId] = userData

        broadcastUsersStatus()

        return userId
    }

    suspend fun onUserMessage(userId: UserId, rawMessage: String) {
        println("Incoming message : ${rawMessage}")

        val inputMessage = serializer.deserializeInputMessage(rawMessage)

        when (inputMessage) {
            is SelectVoteInputMessage -> handleVoteSelection(inputMessage, userId)
            is SendRevealInputMessage -> handleVoteReveal(userId)
            is SendResetInputMessage -> handleReset(userId)
        }
    }

    private suspend fun handleVoteSelection(inputMessage: SelectVoteInputMessage, userId: UserId) {
        val oldValue = users[userId]

        if (oldValue != null) {
            val newValue = oldValue.copy(vote = inputMessage.vote)

            users.replace(userId, newValue)

            broadcastUsersStatus()
        } else {
            println("Cannot change vote - user does not exist")
        }
    }

    private suspend fun handleReset(userId: UserId) {
        if (leaderId == userId) {
            this.revealed = false

            users.forEach { d ->
                users.replace(d.key, d.value.copy(vote = ""))
            }
        }

        broadcastUsersStatus()
    }

    private suspend fun handleVoteReveal(userId: UserId) {
        if (leaderId == userId) {
            this.revealed = true
        }

        broadcastUsersStatus()
    }

    suspend fun onUserDisconnected(userId: UserId?, closeReason: Deferred<CloseReason?>) {
        println("onClose $closeReason")

        if (userId != null) {
            users.remove(userId)
            println("User ${userId} removed")
        }

        broadcastUsersStatus()
    }

    suspend fun onError(userId: UserId?, e: Throwable, closeReason: Deferred<CloseReason?>) {
        println("onError $closeReason")

        e.printStackTrace()

        if (userId != null) {
            users.remove(userId)
            println("User ${userId} removed")
        }

        broadcastUsersStatus()
    }

    fun hasLeaderLeft(): Boolean {
        return users.contains(leaderId)
    }

    suspend fun disconnectEverybody() {
        users.forEach { d ->
            try {
                d.value.webSocketSession.close()

            } catch (e: Throwable) {
                println("Could not send statuses of users - $e")
            }
        }
    }

    private suspend fun broadcastUsersStatus() {
        users.forEach { (userId, userData) ->
            try {
                val allVotesFilled = users.values.all { v -> v.vote.isNotBlank() }

                val userStatuses = users.map { u ->
                    if (allVotesFilled || revealed) {
                        UserStatus(u.key.id, u.value.vote)
                    } else if (u.value.vote.isBlank()) {
                        UserStatus(u.key.id, "")
                    } else {
                        UserStatus(u.key.id, "X")
                    }
                }

                val message = UserStatusOutputMessage("userstatus", userId.id, leaderId.id, userStatuses)

                userData.webSocketSession.send(Frame.Text(serializer.serializeOutputMessage(message)))
            } catch (e: Throwable) {
                println("Could not send user statuses to user ${userId} - $e")
            }
        }
    }

}