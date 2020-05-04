package io.github.wechaty.io.github.wechaty.schemas

import com.fasterxml.jackson.annotation.JsonValue
import io.github.wechaty.schemas.MessageType

enum class ScanStatus(var code: Int) {
    Unknown(-1),
    Cancle(0),
    Waiting(1),
    Scanned(2),
    Cnnfirmed(3),
    Timeout(4);

    companion object {
        fun getByCode(code: Int): ScanStatus {
            val values = ScanStatus.values()
            for (value in values) {
                if (value.code == code) {
                    return value
                }
            }
            return ScanStatus.Unknown
        }
    }

}

data class EventFriendshipPayLoad(var friendshipId:String) {
    override fun toString(): String {
        return "EventFriendshipPayLoad(friendshipId='$friendshipId')"
    }
}

data class EventLoginPayload(var contactId:String) {
    override fun toString(): String {
        return "EventLoginPayload(contactId='$contactId')"
    }
}

data class EventLogoutPayload(
        var contactId:String,
        var data: String
) {
    override fun toString(): String {
        return "EventLogoutPayload(contactId='$contactId', data='$data')"
    }
}


data class EventMessagePayload(var messageId:String) {
    override fun toString(): String {
        return "EventMessagePayload(messageId='$messageId')"
    }
}

data class EventRoomInvitePayload(
        var roomInvitationId: String
) {
    override fun toString(): String {
        return "EventRoomInvitePayload(roomInvitationId='$roomInvitationId')"
    }
}

data class EventRoomJoinPayload(
        var inviteeIdList : List<String>,
        var inviterId : String,
        var roomId : String,
        var timestamp : Long
) {
    override fun toString(): String {
        return "EventRoomJoinPayload(inviteeIdList=$inviteeIdList, inviterId='$inviterId', roomId='$roomId', timestamp=$timestamp)"
    }
}

data class EventRoomLeavePayload(
        var removeeIdList: List<String>,
        var removerId:String,
        var roomId:String,
        var timestamp:Long
) {
    override fun toString(): String {
        return "EventRoomLeavePayload(removeeIdList=$removeeIdList, removerId='$removerId', roomId='$roomId', timestamp=$timestamp)"
    }
}

data class EventRoomTopicePayload(
        var changerId:String,
        var newTopic:String,
        var oldTopic:String,
        var roomId:String,
        var timstamp:Long
) {
    override fun toString(): String {
        return "EventRoomTopicePayload(changerId='$changerId', newTopic='$newTopic', oldTopic='$oldTopic', roomId='$roomId', timstamp=$timstamp)"
    }
}

data class EventScanPayload(var status: ScanStatus){
    var qrcode:String? = null
    var data:String? = null
    override fun toString(): String {
        return "EventScanPayload(status=$status, qrcode=$qrcode, data=$data)"
    }
}

data class EventDongPayload(
        var data:String
) {
    override fun toString(): String {
        return "EventDongPayload(data='$data')"
    }
}

data class EventErrorPayload(
        var data:String
) {
    override fun toString(): String {
        return "EventErrorPayload(data='$data')"
    }
}

data class EventReadyPayload(
        var data:String
) {
    override fun toString(): String {
        return "EventReadyPayload(data='$data')"
    }
}

data class EventResetPayload(
        var data :String
) {
    override fun toString(): String {
        return "EventResetPayload(data='$data')"
    }
}

data class EventHeartbeatPayload(
        var data:String
) {
    override fun toString(): String {
        return "EventHeartbeatPayload(data='$data')"
    }
}
