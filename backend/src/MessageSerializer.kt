package tech.konkit

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser

class MessageSerializer {

    private val gson = Gson()

    fun deserializeInputMessage(rawMessage: String): InputMessage {
        val jsonObject: JsonObject = JsonParser.parseString(rawMessage).asJsonObject

        val messageType = jsonObject.get("messagetype").asString

        if (messageType == "selectvote") {
            println("Changing vote")

            return SelectVoteInputMessage(jsonObject.get("id").asString, jsonObject.get("vote").asString)
        } else if (messageType == "sendreveal") {
            return SendRevealInputMessage(jsonObject.get("id").asString)
        } else if (messageType == "sendreset") {
            return SendResetInputMessage(jsonObject.get("id").asString)
        } else {
            throw Exception("Unrecognized ")
        }
    }

    fun serializeOutputMessage(outputMessage: OutputMessage): String {
        return gson.toJson(outputMessage)
    }
}



interface InputMessage

data class SelectVoteInputMessage(val id: String, val vote: String) : InputMessage
data class SendRevealInputMessage(val id: String) : InputMessage
data class SendResetInputMessage(val id: String) : InputMessage


interface OutputMessage

data class UserStatusOutputMessage(val messagetype: String,
                                   val yourId: String,
                                   val leaderId: String,
                                   val users: List<UserStatus>) : OutputMessage

data class UserStatus(val id: String, val vote: String)