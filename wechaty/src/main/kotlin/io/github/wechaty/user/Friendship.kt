package io.github.wechaty.user

import io.github.wechaty.Accessory
import io.github.wechaty.Wechaty
import io.github.wechaty.schemas.FriendshipPayload
import io.github.wechaty.schemas.FriendshipSearchCondition
import io.github.wechaty.schemas.FriendshipType
import io.github.wechaty.utils.JsonUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

class Friendship (wechaty: Wechaty,val id:String):Accessory(wechaty){


    private var payload:FriendshipPayload? = null

    fun search(queryFilter: FriendshipSearchCondition):Contact?{
        val contactId = wechaty.getPuppet().friendshipSearch(queryFilter).get();
        if(StringUtils.isEmpty(contactId)){
            return null
        }
        val contact = wechaty.contactManager.load(contactId!!)
        contact.ready()
        return contact
    }

    // 这个应该是静态方法吧
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
        wechaty.getPuppet().friendshipAccept(this.id).get()
        val contact = contact()
        contact.ready()
        contact.sync()
    }

    fun hello():String{
        if(payload==null){
            throw Exception("ne payload")
        }
        return this.payload?.hello ?: "";
    }

    fun type():FriendshipType{
        return this.payload?.type ?:FriendshipType.Unknown
    }

    fun toJson():String{
        if(payload==null){
            throw Exception("ne payload")
        }
        return JsonUtils.write(payload!!);
    }
    companion object{
        private val log = LoggerFactory.getLogger(Friendship::class.java)
    }

}

