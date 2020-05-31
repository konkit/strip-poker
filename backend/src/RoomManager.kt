package tech.konkit

import java.util.concurrent.ConcurrentHashMap

class RoomManager() {
    private val sessions: ConcurrentHashMap<String, EstimationSession> = ConcurrentHashMap()

    fun createRoom(): String {
        val newRoomNumber = (sessions.count() + 1).toString()
        val newSession = EstimationSession()

        sessions.put(newRoomNumber, newSession)

        return newRoomNumber
    }

    fun getSessionByRoomNumber(roomNumber: String): EstimationSession? {
        return sessions.get(roomNumber)
    }
}