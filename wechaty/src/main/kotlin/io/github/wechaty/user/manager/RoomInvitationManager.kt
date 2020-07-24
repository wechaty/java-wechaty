package io.github.wechaty.user.manager

import io.github.wechaty.Accessory
import io.github.wechaty.Wechaty
import io.github.wechaty.user.Contact
import io.github.wechaty.user.RoomInvitation
import org.slf4j.LoggerFactory

class RoomInvitationManager (wechaty: Wechaty):Accessory(wechaty){


    fun load(id:String): RoomInvitation {
        return RoomInvitation(wechaty,id)
    }

    companion object {
        private val log = LoggerFactory.getLogger(Contact::class.java)
    }

}
