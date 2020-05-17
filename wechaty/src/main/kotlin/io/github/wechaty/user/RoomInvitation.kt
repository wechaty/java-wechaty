package io.github.wechaty.user

import io.github.wechaty.Accessory
import io.github.wechaty.Wechaty
import org.apache.commons.collections4.CollectionUtils
import org.slf4j.LoggerFactory
import java.util.*

class RoomInvitation(wechaty: Wechaty) : Accessory(wechaty){

    constructor(wechaty: Wechaty,id: String):this(wechaty){
        this.id=id
    }

    private var id:String? = null

    fun load(id:String):RoomInvitation{
        val roomInvitation = RoomInvitation(wechaty)
        roomInvitation.id = id
        return roomInvitation
    }

    fun accept(){
        wechaty.getPuppet().roomInvitationAccept(this.id!!)

        val inviter = inviter()
        val topic = topic()

        log.info("accept with room{} & inviter {},read()",topic,inviter)
        inviter.ready()
    }

    fun roomTopic():String{
        return this.topic();
    }

    fun memberCount():Int{
        val payload = wechaty.getPuppet().roomInvitationPayload(this.id!!).get()
        return payload.memberCount ?: 0
    }

    fun memeberList():List<Contact>{
        val payload = wechaty.getPuppet().roomInvitationPayload(this.id!!).get()

        val memberIdList = payload.memberIdList

        if(CollectionUtils.isNotEmpty(memberIdList)){

            val contactList = memberIdList!!.map {
                wechaty.contact().load(it)
            }

            contactList.forEach{
                it.ready()
            }
            return contactList
        }

        return listOf()
    }

    fun date(): Date {
        val payload = wechaty.getPuppet().roomInvitationPayload(this.id!!).get()
        return Date(payload.timestamp!! * 1000)
    }

    fun age():Long{
        val recvDate = this.date()
        return System.currentTimeMillis() - recvDate.time;
    }

    fun inviter():Contact{
        val payload = wechaty.getPuppet().roomInvitationPayload(this.id!!).get()
        return wechaty.contact().load(payload.inviterId!!)
    }

    fun topic():String {
        val payload = wechaty.getPuppet().roomInvitationPayload(this.id!!).get()
        return payload.topic ?:"";
    }

    companion object{
        private val log = LoggerFactory.getLogger(RoomInvitation::class.java)
    }



}