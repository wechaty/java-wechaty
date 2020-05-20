package io.github.wechaty.user.manager

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.github.wechaty.Accessory
import io.github.wechaty.Wechaty
import io.github.wechaty.schemas.ContactQueryFilter
import io.github.wechaty.schemas.RoomMemberQueryFilter
import io.github.wechaty.user.Contact
import io.github.wechaty.user.ContactSelf
import io.github.wechaty.user.Tag
import org.slf4j.LoggerFactory

class ContactManager(wechaty: Wechaty):Accessory(wechaty) {

    private val contactCache: Cache<String, Contact> = Caffeine.newBuilder().build()

    fun load(id:String):Contact{
        return contactCache.get(id){
            Contact(wechaty, id)
        }!!
    }

    fun loadSelf(id:String):ContactSelf{
        val contactSelf = ContactSelf(wechaty, id)
        contactCache.put(id,contactSelf)
        return contactSelf

    }




    fun find(queryFilter: ContactQueryFilter):Contact?{

        TODO()

    }

    fun findAll(queryFilter: ContactQueryFilter):List<Contact>{
        TODO()
    }

    fun tags():List<Tag>{
        val tagIdList = wechaty.getPuppet().tagContactList().get()
        return tagIdList.map {
            wechaty.tagManager.load(it)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(Contact::class.java)
    }


}
