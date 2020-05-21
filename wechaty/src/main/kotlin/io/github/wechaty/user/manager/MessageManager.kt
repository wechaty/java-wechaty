package io.github.wechaty.user.manager

import io.github.wechaty.Accessory
import io.github.wechaty.Wechaty
import io.github.wechaty.schemas.MessageQueryFilter
import io.github.wechaty.user.Contact
import io.github.wechaty.user.Message
import org.slf4j.LoggerFactory

class MessageManager (wechaty: Wechaty):Accessory(wechaty){

    fun find(queryFilter: MessageQueryFilter): Message?{
        TODO()
    }

    fun findAll(queryFilter: MessageQueryFilter):List<Message>{
        TODO()
    }

    fun load(id:String):Message{
        return Message(wechaty,id)
    }

    fun create(id:String):Message{
        return load(id)
    }

    companion object {
        private val log = LoggerFactory.getLogger(Contact::class.java)
    }


}
