package io.github.wechaty.user

import io.github.wechaty.Accessory
import io.github.wechaty.Puppet
import io.github.wechaty.Wechaty
import io.github.wechaty.filebox.FileBox
import io.github.wechaty.schemas.ContactPayload
import io.github.wechaty.schemas.ContactQueryFilter
import io.github.wechaty.type.Sayable
import io.github.wechaty.utils.FutureUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

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

        var msgId: String?

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
                puppet.contactPayloadDirty(id!!)
            }
            this.payload = puppet.contactPayload(id!!).get()
        } catch (e: Exception) {
            log.error("ready() contactPayload {} error ", id, e)
            throw e
        }


    }

    fun isReady(): Boolean {
        return (payload != null && StringUtils.isNotEmpty(payload!!.name))
    }

    fun name():String{
        return payload?.name ?: ""
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

    fun getAlias():String?{
        return payload?.alias ?:null
    }

    open fun avatar(): Future<FileBox> {
        TODO()
    }


    companion object {
        private val log = LoggerFactory.getLogger(Contact::class.java)
    }
}
