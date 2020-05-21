package io.github.wechaty.user.manager

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.github.wechaty.Accessory
import io.github.wechaty.Wechaty
import io.github.wechaty.user.Contact
import io.github.wechaty.user.Tag
import org.slf4j.LoggerFactory

class TagManager(wechaty: Wechaty):Accessory(wechaty){

    private val tagCache: Cache<String, Tag> = Caffeine.newBuilder().build()

    fun load(id:String):Tag{
        return tagCache.get(id){
            Tag(wechaty, id)
        }!!
    }

    fun get(id:String):Tag{
        return load(id)
    }

    fun delete(tag:Tag,target: Contact){
        wechaty.getPuppet().tagContactDelete(tag.id)
    }

    companion object{
        private val log = LoggerFactory.getLogger(TagManager::class.java)
    }

}
