package io.github.wechaty

import io.github.wechaty.io.github.wechaty.schemas.ScanStatus
import io.github.wechaty.user.Message
import io.github.wechaty.utils.LogUtils

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

interface ScanListener{
    fun handler(qrcode: String?, statusScanStatus: ScanStatus, data: String?)
//        LogUtils.log.warn("${this.javaClass.name} not set")

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