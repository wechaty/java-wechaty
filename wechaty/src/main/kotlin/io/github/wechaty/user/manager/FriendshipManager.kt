package io.github.wechaty.user.manager

import io.github.wechaty.Accessory
import io.github.wechaty.Wechaty
import io.github.wechaty.schemas.FriendshipPayload
import io.github.wechaty.schemas.FriendshipSearchCondition
import io.github.wechaty.user.Contact
import io.github.wechaty.user.Friendship
import io.github.wechaty.utils.JsonUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class FriendshipManager (wechaty: Wechaty): Accessory(wechaty){

    fun load(id:String): Friendship {
        return Friendship(wechaty,id)
    }

    fun search(queryFilter: FriendshipSearchCondition): Contact?{
        log.debug("query filter {}",queryFilter)
        val contactId = wechaty.getPuppet().friendshipSearch(queryFilter).get();
        if(StringUtils.isEmpty(contactId)){
            return null
        }
        val contact = wechaty.contactManager.load(contactId!!)
        contact.ready()
        return contact
    }

    fun add(contact: Contact,hello:String){
        log.debug("add {},{}",contact,hello)
        wechaty.getPuppet().friendshipAdd(contact.id,hello).get()
    }

    fun del(contact: Contact){
        log.debug("del {}",contact)
        throw Exception("to be implemented")
    }

    fun fromJSON(payload:String):Friendship{
        val readValue = JsonUtils.readValue<FriendshipPayload>(payload)
        return fromJSON(readValue)
    }

    fun fromJSON(friendshipPayload: FriendshipPayload):Friendship{
        wechaty.getPuppet().friendshipPayload(friendshipPayload.id!!,friendshipPayload).get()
        return load(friendshipPayload.id!!)
    }

    companion object{
        private val log: Logger = LoggerFactory.getLogger(FriendshipManager::class.java)
    }

}
