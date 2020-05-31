package io.github.wechaty.user

import io.github.wechaty.Accessory
import io.github.wechaty.Wechaty
import io.github.wechaty.schemas.FriendshipPayload
import io.github.wechaty.schemas.FriendshipSearchCondition
import io.github.wechaty.schemas.FriendshipType
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

class Friendship (wechaty: Wechaty):Accessory(wechaty){

    constructor(wechaty: Wechaty,id: String):this(wechaty){
        this.id = id
    }

    private var id:String? = null

    private var payload:FriendshipPayload? = null

    fun load(id:String):Friendship{
        this.id = id
        return this
    }

    fun search(queryFilter: FriendshipSearchCondition):Contact?{
        val contactId = wechaty.getPuppet().friendshipSearch(queryFilter).get();
        if(StringUtils.isEmpty(contactId)){
            return null
        }
        val contact = wechaty.contactManager.load(contactId!!)
        contact.ready()
        return contact
    }


    fun add(contact: Contact, hello:String){
        log.debug("add contact: {} hello: {}",contact,hello)
        wechaty.getPuppet().friendshipAdd(contact.id!!,hello).get()
    }

    fun isReady():Boolean{
        return  payload != null
    }

    fun ready(){
        if(isReady()){
            return
        }
        this.payload = wechaty.getPuppet().friendshipPayload(id!!).get()
        contact().ready()

    }

    fun contact():Contact{
        if(payload == null){
            throw Exception("no payload")
        }
        return wechaty.contactManager.load(payload!!.contactId!!)
    }

    fun accept(){
        if(payload == null){
            throw Exception("no payload")
        }

        if(payload!!.type != FriendshipType.Receive){
            throw Exception("accept() need type to be FriendshipType.Receive, but it got a ${payload!!.type}")
        }

        wechaty.getPuppet().friendshipAccept(this.id!!).get()

        val contact = contact()

        contact.ready()

        contact.sync()

    }


    companion object{
        private val log = LoggerFactory.getLogger(Friendship::class.java)
    }

}

