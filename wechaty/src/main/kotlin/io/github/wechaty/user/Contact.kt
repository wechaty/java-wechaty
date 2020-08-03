package io.github.wechaty.user

import io.github.wechaty.Accessory
import io.github.wechaty.Puppet
import io.github.wechaty.Wechaty
import io.github.wechaty.filebox.FileBox
import io.github.wechaty.schemas.ContactGender
import io.github.wechaty.schemas.ContactPayload
import io.github.wechaty.schemas.ContactQueryFilter
import io.github.wechaty.schemas.ContactType
import io.github.wechaty.type.Sayable
import io.github.wechaty.utils.FutureUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import kotlin.math.E

open class Contact(wechaty: Wechaty,val id:String) : Sayable, Accessory(wechaty) {

    protected val puppet: Puppet = wechaty.getPuppet()
    protected var payload: ContactPayload? = null

    override fun say(something: Any, contact: Contact): Future<Any> {
        when (something) {

            is String -> {
                val messageSendText = puppet.messageSendText(id, something)
            }

        }
        return CompletableFuture.completedFuture(null);
    }

    fun say(something: Any): Message? {

        val msgId: String?

        when (something) {

            is String -> {
                msgId = puppet.messageSendText(id, something).get()
            }
            is Contact -> {
                msgId = puppet.messageSendContact(id, something.id).get()
            }
            is FileBox -> {
                msgId = puppet.messageSendFile(id, something).get()
            }
            is UrlLink -> {
                msgId = puppet.messageSendUrl(id, something.payload).get()
            }
            is MiniProgram -> {
                msgId = puppet.messageSendMiniProgram(id, something.payload).get()
            }
            else -> {
                throw Exception("unsupported arg:$something")
            }
        }

        if (msgId != null) {

            val message = wechaty.messageManager.load(msgId)
            message.ready()
            return message
        }

        return null;

    }

    fun sync() {
        return ready(true)
    }

    fun ready(forceSyn: Boolean = false) {
        if (!forceSyn && isReady()) {
            return
        }
        try {
            if (forceSyn) {
                puppet.contactPayloadDirty(id)
            }
            this.payload = puppet.contactPayload(id).get()
        } catch (e: Exception) {
            log.error("ready() contactPayload {} error ", id, e)
            throw e
        }


    }

    fun isReady(): Boolean {
        return (payload != null && StringUtils.isNotEmpty(payload!!.name))
    }

    fun friend(): Boolean {
        return payload?.friend ?: false
    }

    fun name():String {
        return payload?.name ?: ""
    }

    fun type(): ContactType {
        return payload?.type ?: ContactType.Unknown
    }

    fun gender(): ContactGender {
        return payload?.gender ?: ContactGender.Unknown
    }

    fun address(): String {
        return this.payload?.address ?: ""
    }
    fun province(): String {
        return this.payload?.province ?: ""
    }

    fun signature(): String {
        return this.payload?.signature ?: ""
    }

    fun city(): String {
        return this.payload?.city ?: ""
    }

    fun star(): Boolean {
        return this.payload?.star ?: false
    }

    fun weixin(): String {
        return this.payload?.weixin ?: ""
    }

    fun self(): Boolean {
        val userId = this.puppet.selfId()

        if (userId == null) {
            return false
        }
        return this.id === userId
    }

    fun setAlias(newAlias:String){
        if(payload == null){
            throw Exception("no payload")
        }
        try {
            puppet.contactAlias(id, newAlias).get()
            puppet.contactPayloadDirty(id)
            payload = puppet.contactPayload(id).get()
        }catch (e:Exception){
            log.error("alias({}) rejected: {}", newAlias, e.message)
            throw e
        }

    }

    fun getAlias():String? {
        return payload?.alias
    }

    fun stranger():Boolean?{
        return if(friend() == null){
            null
        }else{
            !friend()!!
        }
    }

    fun friend():Boolean?{
        return payload?.friend
    }

    fun type():ContactType{
        return payload?.type ?: throw Exception("no payload")
    }

    fun gender():ContactGender{
        return payload?.gender ?: ContactGender.Unknown
    }

    fun province():String?{
        return payload?.province
    }

    fun city():String?{
        return payload?.city
    }

    open fun avatar(): FileBox {
        try {
            return wechaty.getPuppet().getContactAvatar(this.id).get()
        } catch (e: Exception) {
            log.error("error",e)
            TODO()
        }
    }

    fun tags():List<Tag>{
        val tagIdList = wechaty.getPuppet().tagContactList(this.id).get()
        return try {
            tagIdList.map {
                wechaty.tagManager.load(it)
            }
        } catch (e: Exception) {
            log.error("error",e)
            listOf()
        }
    }

    fun self():Boolean{
        val userId = puppet.selfId()
        if(StringUtils.isEmpty(userId)){
            return false
        }
        return StringUtils.equals(id,userId)
    }


    companion object {
        private val log = LoggerFactory.getLogger(Contact::class.java)
    }
}
