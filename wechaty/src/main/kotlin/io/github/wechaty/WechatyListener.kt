package io.github.wechaty

import io.github.wechaty.schemas.ScanStatus
import io.github.wechaty.user.Message

@FunctionalInterface
interface DongListener{
    fun handler(data:String?)
}

@FunctionalInterface
interface ErrorListener{
    fun handler(error:String)
}

@FunctionalInterface
interface FriendshipListener{
    fun handler(friendshipId:String)
}

@FunctionalInterface
interface HeartbeatListener{
    fun handlder(data:Any)
}

@FunctionalInterface
interface ScanListener{
    fun handler(qrcode: String?, statusScanStatus: ScanStatus, data: String?)

}

@FunctionalInterface
interface LoginListener{
    fun handler(contactId:String)
}

@FunctionalInterface
interface LogoutListener{
    fun handler(contactId: String,reason:String?)
}

@FunctionalInterface
interface ResetListerner{
    fun handler(reason:String)
}

@FunctionalInterface
interface RoomJoinListerner{
    fun handler(roomId:String,inviteeIdList:List<String>,inviterId:String,timestamp:Long)
}

@FunctionalInterface
interface RoomLeaveListerner{
    fun handler(roomId: String,leaverIdList:List<String>,removerId:String,timestamp:Long)
}

@FunctionalInterface
interface RoomTopicListener{
    fun handler(roomId: String,newTopic:String,oldTopic:String,changerId:String,timestamp:Long)
}

@FunctionalInterface
interface RoomInviteListener{
    fun handler(roomInvitationId:String)
}

@FunctionalInterface
interface ReadyListener{
    fun handler()
}

@FunctionalInterface
interface MessageListener{
    fun handler(message:Message)

}