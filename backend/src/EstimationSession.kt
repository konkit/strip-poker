package tech.konkit

import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import kotlinx.coroutines.Deferred
import java.util.concurrent.ConcurrentHashMap

class EstimationSession() {

    private val serializer = MessageSerializer()

    private var revealed: Boolean = false
    private var users = ConcurrentHashMap<UserId, UserData>()
    private var leaderId: UserId = UserId.empty

    suspend fun onUserJoin(userId: UserId, webSocketSession: WebSocketSession): UserId {
        val userData = UserData(webSocketSession = webSocketSession, vote = "")

        if (leaderId == UserId.empty) {
            leaderId = userId
        }

        users[userId] = userData

        broadcastUsersStatus()

        return userId
    }

    suspend fun onUserMessage(userId: UserId, rawMessage: String) {
        val inputMessage = serializer.deserializeInputMessage(rawMessage)

        when (inputMessage) {
            is SelectVoteInputMessage -> handleVoteSelection(inputMessage, userId)
            is SendRevealInputMessage -> handleVoteReveal(userId)
            is SendResetInputMessage -> handleReset(userId)
        }
    }

    suspend fun onUserDisconnected(userId: UserId?, closeReason: Deferred<CloseReason?>) {
        if (userId != null) {
            users.remove(userId)
        }

        broadcastUsersStatus()
    }

    private suspend fun handleVoteSelection(inputMessage: SelectVoteInputMessage, userId: UserId) {
        val oldValue = users[userId]

        if (oldValue != null) {
            if (!revealed) {
                val newValue = oldValue.copy(vote = inputMessage.vote)

                users.replace(userId, newValue)

                val allVotesFilled = users.values.all { v -> v.vote.isNotBlank() }
                if (allVotesFilled) {
                    revealed = true
                }

                broadcastUsersStatus()
            }
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

    private suspend fun broadcastUsersStatus() {
        val userStatuses = users.map { (userId, userData) ->
            if (revealed) {
                UserStatus(userId.id, userData.vote)
            } else if (userData.vote.isBlank()) {
                UserStatus(userId.id, "")
            } else {
                UserStatus(userId.id, "X")
            }
        }

        users.forEach { (userId, userData) ->
            try {
                val message =
                    UserStatusOutputMessage("userstatus", userId.id, userData.vote, leaderId.id, revealed, userStatuses)

                userData.webSocketSession.send(Frame.Text(serializer.serializeOutputMessage(message)))
            } catch (e: Throwable) {
                println("Could not send user statuses to user ${userId} - $e")
            }
        }
    }

}

data class UserId(val id: String) {
    override fun toString(): String {
        return id
    }

    companion object {
        val empty = UserId("")
    }
}

data class UserData(val webSocketSession: WebSocketSession, val vote: String)