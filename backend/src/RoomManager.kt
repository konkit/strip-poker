package tech.konkit

import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class RoomManager() {
    private val rooms: ConcurrentHashMap<String, Room> = ConcurrentHashMap()

    fun createRoom(userId: UserId): String {
        val newRoomNumber = generateNewRoomNumber()
        val newSession = Room(leaderId = userId)

        rooms.put(newRoomNumber, newSession)

        return newRoomNumber
    }

    private fun generateNewRoomNumber() = "${Instant.now().toEpochMilli()}${Random.nextInt(0, 9999)}"

    fun getSessionByRoomNumber(roomNumber: String): Room? {
        return rooms.get(roomNumber)
    }
}