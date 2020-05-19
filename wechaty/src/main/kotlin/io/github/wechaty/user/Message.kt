package io.github.wechaty.user

import io.github.wechaty.Accessory
import io.github.wechaty.Wechaty
import io.github.wechaty.filebox.FileBox
import io.github.wechaty.schemas.MessagePayload
import io.github.wechaty.schemas.MessageType
import io.github.wechaty.schemas.RoomMemberQueryFilter
import io.github.wechaty.type.Sayable
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.regex.Pattern

open class Message(wechaty: Wechaty) : Sayable, Accessory(wechaty){

    var id:String?=null

    constructor(wechaty: Wechaty,id: String):this(wechaty){
        this.id = id
    }

    private val AT_SEPRATOR_REGEX = "[\\u2005\\u0020]"
    private val puppet = wechaty.getPuppet()
    private var payload : MessagePayload? = null

    override fun say(something: Any, contact: Contact): Future<Any> {

        val from = from()

        val room = room()

        val conversationId: String

        conversationId = when {
            room != null -> {
                room.id!!
            }
            from != null -> {
                from.id!!
            }
            else -> {
                throw Exception("neither room nor fromId?")
            }
        }

        var msgId:String?

        return CompletableFuture.supplyAsync {
            when(something){

                is String ->{
                    msgId = puppet.messageSendText(conversationId, something).get()
                }
                is FileBox ->{
                    msgId = puppet.messageSendFile(conversationId,something).get()
                }

                is UrlLink ->{
                    msgId = puppet.messageSendUrl(conversationId,something.payload).get()
                }

                is MiniProgram->{
                    msgId = puppet.messageSendMiniProgram(conversationId,something.payload).get()
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

    fun room():Room?{
        if(payload == null){
            throw Exception("no payload")
        }

        val roomId = payload!!.roomId

        if (StringUtils.isEmpty(roomId)) {
            return null
        }
        return wechaty.room().load(roomId!!)
    }


    fun recall():Future<Boolean>{
        return CompletableFuture.supplyAsync {
            return@supplyAsync puppet.messageRecall(this.id!!).get()
        }
    }

    fun type(): MessageType{
        if(this.payload == null){
            throw Exception("no payload")
        }
        return this.payload?.type ?: MessageType.Unknown
    }

    fun self():Boolean{
        val selfId = puppet.selfId()
        val from = from()

        return StringUtils.equals(selfId,from?.id)
    }

    fun mentionList():List<Contact>{
        val room = this.room()

        if(room == null && type() != MessageType.Text){
            return listOf()
        }

        if(CollectionUtils.isNotEmpty(payload?.mentionIdList)){


            val list = payload!!.mentionIdList!!.map {
                val contact = wechaty.contact().load(it)
                contact.ready()
                contact
            }

            return list
        }

        val atList = this.text().split(AT_SEPRATOR_REGEX)
        if(atList.isEmpty()){
            return listOf()
        }

        val rawMentionList = atList.filter {
            "@" in it
        }.map {
            multipleAt(it)
        }

        val mentionNameList = rawMentionList.flatten().filter {
            StringUtils.isNotEmpty(it)
        }

        val roomMemberQueryFilter = RoomMemberQueryFilter()
        val flatten = mentionNameList.map {
            roomMemberQueryFilter.name = it
            room!!.memberAll(roomMemberQueryFilter)
        }.flatten()

        return flatten
    }

    fun content():String{
        return text()
    }


    fun ready():Future<Void>{

        return CompletableFuture.supplyAsync {

            if (isReady()) {
                return@supplyAsync null
            }

            this.payload = puppet.messagePayload(id!!).get()

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
                wechaty.contact().load(fromId!!).ready()
            }

            if (StringUtils.isNotBlank(toId)) {
                wechaty.contact().load(toId!!).ready()
            }

            return@supplyAsync null
        }
    }

    fun isReady():Boolean{
        return payload != null;
    }

    fun text():String{
        if(payload == null){
            throw Exception("no play")
        }

        return payload?.text ?:  ""
    }

    fun talker():Contact{
        return this.from()!!
    }

    fun toRecalled():Message?{

        if(this.type() != MessageType.Recalled){
            throw Exception("Cannot call  toRecalled() on message which id not recalled type")
        }

        val originalMessageId = text()

        if(StringUtils.isEmpty(originalMessageId)){
            throw Exception("Cannot find recalled Message")
        }

        return try {
            val message = wechaty.message().load(originalMessageId)
            message.ready().get()
            message
        } catch (e: Exception){
            log.warn("Can not retrieve the recalled message with id ${originalMessageId}")
            null
        }
    }

    fun mentionText():String{
        val text = text()
        val room = room()

        val mentionList = mentionList()
        if(room == null || CollectionUtils.isEmpty(mentionList)){
            return text
        }

        val toAliasName :(Contact) -> String = {
            val alias = room.alias(it)
            val name = it.name()
            if(StringUtils.isNotEmpty(alias)) alias!! else name
        }

        var textWithoutMention = text

        val mentionNameList = mentionList.map(toAliasName)

        mentionNameList.forEach{
            val escapedCur = escapeRegExp(it)
            val regex = Regex("@${escapedCur}(\\u2005|\\u0020|\$)")
            textWithoutMention = regex.replace(text,"")
        }
        return textWithoutMention.trim()

    }

    fun mentionSelf():Boolean{
        val selfId = puppet.selfId()
        val mentionList = this.mentionList()

        return mentionList.any {
            it.id == selfId
        }

    }


    override fun toString():String{
        TODO()
    }

    companion object{
        private val log = LoggerFactory.getLogger(Message::class.java)

        fun create(wechaty: Wechaty,id:String):Message{
            return Message(wechaty,id)
        }
    }


}

fun multipleAt(str:String):List<String>{
    val re = Regex("^.*?@")
    val str1 = re.replace(str, "@")

    var name = ""
    val nameList = mutableListOf<String>()

    str1.split("@")
            .filter {
                StringUtils.isNotEmpty(it)
            }
            .reversed()
            .forEach{mentionName ->
                name = "$mentionName@$name"
                nameList.add(name.dropLast(1))
            }
    return nameList
}

val SPECIAL_REGEX_CHARS: Pattern = Pattern.compile("[{}()\\[\\].+*?^$\\\\|]")

fun escapeRegExp(str: String): String? {
    return SPECIAL_REGEX_CHARS.matcher(str).replaceAll("\\\\$0")
}

fun main(){

    val str = "hello@a@b@c"

    val multipleAt = multipleAt(str)

    println(multipleAt)


}

