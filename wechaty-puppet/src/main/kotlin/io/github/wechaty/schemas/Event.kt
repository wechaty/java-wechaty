package io.github.wechaty.io.github.wechaty.schemas

import com.fasterxml.jackson.annotation.JsonValue

enum class ScanStatus(var code: Int) {
    Unknown(-1),
    Cancle(0),
    Waiting(1),
    Scanned(2),
    Cnnfirmed(3),
    Timeout(4);

    @JsonValue
    fun getStatus():Int{
        return code
    }


}

data class EventFriendshipPayLoad(var friendshipId:String)

data class EventLoginPayload(var contactId:String)

data class EventLogoutPayload(
        var contactId:String,
        var data: String
)


data class EventMessagePayload(var messageId:String)

data class EventRoomInvitePayload(
        var roomInvitationId: String
)

data class EventRoomJoinPayload(
        var inviteeIdList : List<String>,
        var inviterId : String,
        var roomId : String,
        var timestamp : Long
)

data class EventRoomLeavePayload(
        var removeeIdList: List<String>,
        var removerId:String,
        var roomId:String,
        var timestamp:Long
)

data class EventRoomTopicePayload(
        var changerId:String,
        var newTopic:String,
        var oldTopic:String,
        var roomId:String,
        var timstamp:Long
)

data class EventScanPayload(var status: ScanStatus){
    var qrcode:String? = null
    var data:String? = null
}

data class EventDongPayload(
        var data:String
)

data class EventErrorPayload(
        var data:String
)

data class EventReadyPayload(
        var data:String
)

data class EventResetPayload(
        var data :String
)

data class EventHeartbeatPayload(
        var data:String
)
