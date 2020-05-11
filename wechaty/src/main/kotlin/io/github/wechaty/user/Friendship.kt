package io.github.wechaty.user

import io.github.wechaty.Accessory
import io.github.wechaty.Wechaty
import io.github.wechaty.schemas.FriendshipSearchCondition
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

class Friendship (wechaty: Wechaty):Accessory(wechaty){

    private var id:String? = null

    fun load(id:String):String{
        return id
    }

    fun search(queryFilter: FriendshipSearchCondition):Contact?{
        val contactId = wechaty.getPuppet().friendshipSearch(queryFilter).get();
        if(StringUtils.isEmpty(contactId)){
            return null
        }
        val contact = wechaty.contact().load(contactId!!)
        contact.ready().get()
        return contact
    }


    companion object{
        private val log = LoggerFactory.getLogger(Friendship::class.java)
    }

}

