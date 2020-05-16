package io.github.wechaty.listener

import io.github.wechaty.schemas.*

@FunctionalInterface
interface PuppetDongListener{
    fun handler(payload: EventDongPayload)

//    override fun handler0(vararg any: Any) {
//        handler(any[0] as EventDongPayload)
//    }
}

@FunctionalInterface
interface PuppetErrorListener{
    fun handler(payload: EventErrorPayload)
//    override fun handler0(vararg any: Any) {
//        handler(any[0] as EventErrorPayload)
//    }

}

@FunctionalInterface
interface PuppetFriendshipListener{
    fun handler(payload: EventFriendshipPayload)
//    override fun handler0(vararg any: Any) {
//        handler(any[0] as EventFriendshipPayload)
//    }
}

@FunctionalInterface
interface PuppetLoginListener{
    fun handler(payload: EventLoginPayload)
//    override fun handler0(vararg any: Any) {
//        handler(any[0] as EventLoginPayload)
//    }
}

@FunctionalInterface
interface PuppetLogoutListener{
    fun handler(payload: EventLogoutPayload)
//    override fun handler0(vararg any: Any) {
//        handler(any[0] as EventLogoutPayload)
//    }
}

@FunctionalInterface
interface PuppetMessageListener{
    fun handler(payload: EventMessagePayload)
//    override fun handler0(vararg any: Any) {
//        handler(any[0] as EventMessagePayload)
//    }
}

@FunctionalInterface
interface PuppetResetListener{
    fun handler(payload: EventResetPayload)
//    override fun handler0(vararg any: Any) {
//        handler(any[0] as EventResetPayload)
//    }
}

@FunctionalInterface
interface PuppetRoomJoinListerner{
    fun handler(payload: EventRoomJoinPayload)
//    override fun handler0(vararg any: Any) {
//        handler(any[0] as EventRoomJoinPayload)
//    }
}

@FunctionalInterface
interface PuppetRoomLeaveListerner{
    fun handler(payload: EventRoomLeavePayload)
//    override fun handler0(vararg any: Any) {
//        handler(any[0] as EventRoomLeavePayload)
//    }
}

@FunctionalInterface
interface PuppetRoomTopicListener{
    fun handler(payload: EventRoomTopicPayload)
//    override fun handler0(vararg any: Any) {
//        handler(any[0] as EventRoomTopicPayload)
//    }
}

@FunctionalInterface
interface PuppetRoomInviteListener{
    fun handler(payload: EventRoomInvitePayload)
//    override fun handler0(vararg any: Any) {
//        handler(any[0] as EventRoomInvitePayload)
//    }
}

@FunctionalInterface
interface PuppetScanListener{
    fun handler(payload: EventScanPayload)
//    override fun handler0(vararg any: Any) {
//        handler(any[0] as EventScanPayload)
//    }

}


@FunctionalInterface
interface PuppetReadyListener{
    fun handler(payload: EventReadyPayload)
//    override fun handler0(vararg any: Any) {
//        handler(any[0] as EventReadyPayload)
//    }
}

@FunctionalInterface
interface PuppetHeartbeatListener{
    fun handler(payload: EventHeartbeatPayload)
//    override fun handler0(vararg any: Any) {
//        handler(any[0] as EventHeartbeatPayload)
//    }
}




