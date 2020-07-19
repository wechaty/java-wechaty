package io.github.wechaty.user

import io.github.wechaty.Accessory
import io.github.wechaty.Wechaty
import org.apache.commons.collections4.CollectionUtils
import org.slf4j.LoggerFactory
import java.util.*

class RoomInvitation(wechaty: Wechaty,val id:String) : Accessory(wechaty){

    fun accept(){
        wechaty.getPuppet().roomInvitationAccept(this.id)

        val inviter = inviter()
        val topic = topic()

        log.debug("accept with room{} & inviter {},read()",topic,inviter)
        inviter.ready()
    }

    fun roomTopic():String{
        return this.topic();
    }

    fun memberCount():Int{
        val payload = wechaty.getPuppet().roomInvitationPayload(this.id).get()
        return payload.memberCount ?: 0
    }

    fun memeberList():List<Contact>{
        val payload = wechaty.getPuppet().roomInvitationPayload(this.id).get()

        val memberIdList = payload.memberIdList

        if(CollectionUtils.isNotEmpty(memberIdList)){

            val contactList = memberIdList!!.map {
                wechaty.contactManager.load(it)
            }

            contactList.forEach{
                it.ready()
            }
            return contactList
        }

        return listOf()
    }

    fun date(): Date {
        val payload = wechaty.getPuppet().roomInvitationPayload(this.id).get()
        return Date(payload.timestamp!! * 1000)
    }

    fun age():Long{
        val recvDate = this.date()
        return System.currentTimeMillis() - recvDate.time;
    }

    fun inviter():Contact{
        val payload = wechaty.getPuppet().roomInvitationPayload(this.id).get()
        return wechaty.contactManager.load(payload.inviterId!!)
    }

    fun topic():String {
        val payload = wechaty.getPuppet().roomInvitationPayload(this.id).get()
        return payload.topic ?:""
    }

    companion object{
        private val log = LoggerFactory.getLogger(RoomInvitation::class.java)
    }



}
