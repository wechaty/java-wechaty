package io.github.wechaty.user

import io.github.wechaty.Accessory
import io.github.wechaty.Wechaty
import io.github.wechaty.filebox.FileBox
import io.github.wechaty.schemas.MessagePayload
import io.github.wechaty.type.Sayable
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class Message(wechaty: Wechaty) : Sayable, Accessory(wechaty){

    var id:String?=null

    constructor(wechaty: Wechaty,id: String):this(wechaty){
        this.id = id
    }

    private val puppte = wechaty.getPuppet()
    protected var payload : MessagePayload? = null

    override fun say(something: Any, contact: Contact): Future<Any> {

        val from = from()

        val room = room()

        var conversationId: String

        if(room != null){
            conversationId = room.id!!
        }else if(from != null){
            conversationId = from.id!!
        }else {
            throw Exception("neither room nor fromId?")
        }

        var msgId:String? = null

        return CompletableFuture.supplyAsync {
            when(something){

                is String ->{
                    msgId = puppte.messageSendText(conversationId, something).get()
                }

                is FileBox ->{

                    msgId = puppte.messageSendFile(conversationId,something).get()

                }

                else ->{
                    throw Exception("unknown message")
                }

            }

            if(msgId != null){
                val msg = wechaty.message().load(msgId!!)
                msg.load(msgId!!)
                return@supplyAsync msg
            }

            return@supplyAsync null
        }
    }

    fun load(id:String):Message {
        return Message(wechaty,id)
    }

    fun from():Contact?{
        if(payload == null){
            throw Exception("no payload")
        }
        val fromId = payload!!.fromId ?: return null
        return wechaty.contact().load(fromId)
    }

    fun to():Contact?{
        if(payload == null){
            throw Exception("no payload")
        }
        val toId = payload!!.toId ?: return null
        return wechaty.contact().load(toId)
    }

    fun room():Contact?{
        if(payload == null){
            throw Exception("no payload")
        }

        val roomId = payload!!.roomId

        if (StringUtils.isEmpty(roomId)) {
            return null
        }

        return wechaty.contact().load(roomId!!)
    }


    fun recall():Future<Boolean>{
        return CompletableFuture.supplyAsync {
            return@supplyAsync puppte.messageRecall(this.id!!).get()
        }
    }

    fun ready():Future<Void>{

        return CompletableFuture.supplyAsync {

            if (isReady()) {
                return@supplyAsync null
            }

            this.payload = puppte.messagePayload(id!!).get()

            log.info("message payload is {}",payload)

            if (payload == null) {
                throw Exception("no playload")
            }

            val fromId = payload!!.fromId
            val roomId = payload!!.roomId
            val toId = payload!!.toId

            if (StringUtils.isNotBlank(roomId)) {
                wechaty.room().load(roomId!!).ready().get()
            }

            if (StringUtils.isNotBlank(fromId)) {
                wechaty.contact().load(fromId!!).ready().get()
            }

            if (StringUtils.isNotBlank(toId)) {
                wechaty.contact().load(toId!!).ready().get()
            }

            return@supplyAsync null
        }
    }

    fun isReady():Boolean{
        return payload != null;
    }

    fun text():String?{
        if(payload == null){
            throw Exception("no play")
        }

        return payload?.text ?:  ""
    }

    companion object{
        val log = LoggerFactory.getLogger(Message::class.java)

        fun create(id:String){

        }
    }


}
