package io.github.wechaty.user.manager

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.github.wechaty.Accessory
import io.github.wechaty.Wechaty
import io.github.wechaty.schemas.*
import io.github.wechaty.user.Contact
import io.github.wechaty.user.ContactSelf
import io.github.wechaty.user.Friendship
import io.github.wechaty.user.Tag
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.util.*

class FriendShipManager(wechaty: Wechaty):Accessory(wechaty) {

    private val friendshipCache: Cache<String, Friendship> = Caffeine.newBuilder().build()

    fun load(id:String): Friendship {
        return friendshipCache.get(id) {
            Friendship(wechaty, id)
        }!!
    }

    // 查找发送请求的好友
    fun search(queryFilter: FriendshipSearchCondition): Contact? {
        val friendshipId = wechaty.getPuppet().friendshipSearch(queryFilter).get();
        if(StringUtils.isEmpty(friendshipId)){
            return null
        }
        val contact = wechaty.contactManager.load(friendshipId!!)
        contact.ready()
        return contact
    }

    // 添加好友
    fun add(contact: Contact, hello:String) {
        log.debug("add contact: {} hello: {}", contact, hello)
        wechaty.getPuppet().friendshipAdd(contact.id, hello).get()
    }

    companion object {
        private val log = LoggerFactory.getLogger(FriendShipManager::class.java)
    }


}
