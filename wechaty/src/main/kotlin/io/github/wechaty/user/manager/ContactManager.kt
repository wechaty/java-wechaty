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
import org.apache.commons.collections4.CollectionUtils
import org.slf4j.LoggerFactory
import java.util.*

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

        val findAll = findAll(queryFilter)

        if(CollectionUtils.isEmpty(findAll)){
            return null
        }

        return findAll[0]
    }

    fun findAll(queryFilter: ContactQueryFilter):List<Contact>{
        val contactIdList = wechaty.getPuppet().contactSearch(queryFilter).get()

        val contactList = contactIdList.map {
            load(it)
        }.filter { Objects.nonNull(it) }

        return contactList

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
