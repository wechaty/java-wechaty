package io.github.wechaty.user

import com.github.benmanes.caffeine.cache.Caffeine
import io.github.wechaty.Accessory
import io.github.wechaty.Puppet
import io.github.wechaty.Wechaty
import io.github.wechaty.io.github.wechaty.filebox.FileBox
import io.github.wechaty.schemas.ContactPayload
import io.github.wechaty.schemas.ContactQueryFilter
import io.github.wechaty.type.Sayable
import io.github.wechaty.utils.FutureUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

open class Contact(wechaty: Wechaty, var id:String? = null) : Sayable, Accessory(wechaty) {

    protected val puppet: Puppet = wechaty.getPuppet()
    protected var payload: ContactPayload? = null

    override fun say(something: Any, contact: Contact): Future<Any> {
        when (something) {

            is String ->{
                val messageSendText = puppet.messageSendText(id!!, something)
            }

        }
        return CompletableFuture.completedFuture(null);
    }

    fun say(something: Any): Future<Void> {
        when (something) {

            is String ->{
                val messageSendText = puppet.messageSendText(id!!, something)
            }

            is FileBox ->{
                val messageSendFile = puppet.messageSendFile(id!!, something).get()
            }

        }
        return CompletableFuture.completedFuture(null);
    }

    fun findAll(query:ContactQueryFilter):Future<List<Contact>>{
        val contactIdList = puppet.contactSearch(query, null).get()
        val contactList = contactIdList.map {
            load(it)
        }

        val futures = contactList.map {
            FutureUtils.toCompletable(it.ready())
        }



        return CompletableFuture.supplyAsync{
            FutureUtils.sequenceVoid(futures).get()
            return@supplyAsync contactList
        }

    }

    fun sync():Future<Void>{
        return ready(true)
    }

    fun ready(forceSyn :Boolean = false):Future<Void>{
        return CompletableFuture.supplyAsync {
            if (!forceSyn && isReady()) {
                return@supplyAsync null
            }
            try {
                if (forceSyn) {
                    puppet.contactPayloadDirty(id!!)
                }
                this.payload = puppet.contactPayload(id!!).get()
            }
            catch (e:Exception){
                log.error("ready() contactPayload {} error ",id,e)
                throw e
            }
            return@supplyAsync null
        }
    }

    fun isReady():Boolean{
        return (payload != null && StringUtils.isNotEmpty(payload!!.name))
    }

    fun load(id:String):Contact{
        if(this.id != null && this.id == id){
            return this
        }else{
            val contact = Contact(wechaty)
            contact.id = id
            wechaty.putContactToCache(id,contact)
            return contact
        }
    }

    open fun avatar():Future<FileBox>{
        TODO()
    }


    companion object {
        private val log = LoggerFactory.getLogger(Contact::class.java)
    }
}
