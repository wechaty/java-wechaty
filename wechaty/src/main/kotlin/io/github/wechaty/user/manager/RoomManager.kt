package io.github.wechaty.user.manager

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.github.wechaty.Accessory
import io.github.wechaty.Wechaty
import io.github.wechaty.schemas.RoomMemberQueryFilter
import io.github.wechaty.schemas.RoomQueryFilter
import io.github.wechaty.user.Contact
import io.github.wechaty.user.Room
import org.slf4j.LoggerFactory
import java.util.logging.Logger

class RoomManager (wechaty: Wechaty):Accessory(wechaty){

    private val roomacache:Cache<String,Room> = Caffeine.newBuilder().build()

    fun create(contactList:List<Contact>,topic:String?):Room {
        if (contactList.size < 2) {
            throw Exception("contactList need at least 2 contact to create a new room")
        }

        val contactIdList = contactList.map {
            it.id
        }

        try {
            val roomId = wechaty.getPuppet().roomCreate(contactIdList, topic).get()
            val room = load(roomId)
            return room
        }catch (e:Exception){
            log.error("create() room error",e )
            throw e
        }
    }

    fun findAll(queryFilter: RoomQueryFilter):List<Room>{
        TODO()
    }

    fun find(queryFilter: RoomQueryFilter):Room{
        TODO()
    }

    fun load(id:String):Room{
        return roomacache.get(id){
            Room(wechaty,id)
        }!!
    }

    companion object{
        private val log = LoggerFactory.getLogger(RoomManager::class.java)
    }


}

