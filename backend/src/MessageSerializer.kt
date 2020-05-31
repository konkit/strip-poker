package tech.konkit

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser

class MessageSerializer {

    private val gson = Gson()

    fun deserializeInputMessage(rawMessage: String): InputMessage {
        val jsonObject: JsonObject = JsonParser.parseString(rawMessage).asJsonObject

        val messageType = jsonObject.get("messagetype").asString

        return when (messageType) {
            "selectvote" -> SelectVoteInputMessage(jsonObject.get("vote").asString)
            "sendreveal" -> SendRevealInputMessage()
            "sendreset" -> SendResetInputMessage()
            else -> throw Exception("Unrecognized ")
        }
    }

    fun serializeOutputMessage(outputMessage: OutputMessage): String {
        return gson.toJson(outputMessage)
    }
}



interface InputMessage

data class SelectVoteInputMessage(val vote: String) : InputMessage
class SendRevealInputMessage() : InputMessage
class SendResetInputMessage() : InputMessage


interface OutputMessage

data class UserStatusOutputMessage(val messagetype: String,
                                   val yourId: String,
                                   val yourVote: String,
                                   val leaderId: String,
                                   val revealed: Boolean,
                                   val users: List<UserStatus>) : OutputMessage

data class UserStatus(val id: String, val vote: String)