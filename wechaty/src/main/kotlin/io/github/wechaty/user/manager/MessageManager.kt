package io.github.wechaty.user.manager

import io.github.wechaty.Accessory
import io.github.wechaty.Wechaty
import io.github.wechaty.schemas.MessageQueryFilter
import io.github.wechaty.user.Contact
import io.github.wechaty.user.Message
import org.apache.commons.collections4.CollectionUtils
import org.slf4j.LoggerFactory
import java.util.*

class MessageManager (wechaty: Wechaty):Accessory(wechaty){

    fun find(query: MessageQueryFilter): Message?{
        val messageList = findAll(query)

        if(CollectionUtils.isEmpty(messageList)){
            return null
        }

        if(messageList.size > 1){
            log.warn("findAll() got more than one({}) result",messageList.size)
        }

        return messageList[0]
    }

    fun findAll(query: MessageQueryFilter):List<Message>{
        log.debug("findAll({})",query)
        return try {
            val messageIdList = wechaty.getPuppet().messageSearch(query).get()
            val messageList = messageIdList.map {
                load(it)
            }
            return messageList.mapNotNull {
                try {
                    it.ready()
                    it
                } catch (e: Exception) {
                    log.warn("findAll() message.ready() rejection: {}", e)
                    null
                }
            }
        }catch (e:Exception){
            log.warn("findAll() rejected: {}", e)
            listOf()
        }
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
