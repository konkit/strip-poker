package tech.konkit

import java.util.concurrent.ConcurrentHashMap

class RoomManager() {
    private val rooms: ConcurrentHashMap<String, Room> = ConcurrentHashMap()

    fun createRoom(userId: UserId): String {
        val newRoomNumber = generateNewRoomNumber()
        val newSession = Room(leaderId = userId)

        rooms.put(newRoomNumber, newSession)

        return newRoomNumber
    }

    private fun generateNewRoomNumber() = (rooms.count() + 1).toString()

    fun getSessionByRoomNumber(roomNumber: String): Room? {
        return rooms.get(roomNumber)
    }
}