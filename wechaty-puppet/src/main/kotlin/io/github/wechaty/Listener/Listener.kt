package io.github.wechaty.io.github.wechaty.Listener

import io.github.wechaty.io.github.wechaty.schemas.ScanStatus

@FunctionalInterface
interface PuppetDongListener{
    fun handler(data:String?)
}

@FunctionalInterface
interface PuppetErrorListener{
    fun handler(error:String)
}

@FunctionalInterface
interface PuppetFriendshipListener{
    fun handler(friendshipId:String)
}

@FunctionalInterface
interface PuppetHeartbeatListener{
    fun handlder(data:Any)
}

@FunctionalInterface
interface PuppetScanListener{
    fun handler(qrcode: String?, statusScanStatus: ScanStatus, data: String?)
}

@FunctionalInterface
interface PuppetLoginListener{
    fun handler(contactId:String)
}

@FunctionalInterface
interface PuppetLogoutListener{
    fun handler(contactId: String,reason:String?)
}

@FunctionalInterface
interface PuppetResetListerner{
    fun handler(reason:String)
}

@FunctionalInterface
interface PuppetRoomJoinListerner{
    fun handler(roomId:String,inviteeIdList:List<String>,inviterId:String,timestamp:Long)
}

@FunctionalInterface
interface PuppetRoomLeaveListerner{
    fun handler(roomId: String,leaverIdList:List<String>,removerId:String,timestamp:Long)
}

@FunctionalInterface
interface PuppetRoomTopicListener{
    fun handler(roomId: String,newTopic:String,oldTopic:String,changerId:String,timestamp:Long)
}

@FunctionalInterface
interface PuppetRoomInviteListener{
    fun handler(roomInvitationId:String)
}

@FunctionalInterface
interface PuppetReadyListener{
    fun handler()
}
@FunctionalInterface
interface PuppetMessageListener{
    fun handler(messageId:String)
}



