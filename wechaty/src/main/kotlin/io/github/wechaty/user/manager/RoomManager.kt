package io.github.wechaty.user.manager

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.github.wechaty.Accessory
import io.github.wechaty.Wechaty
import io.github.wechaty.schemas.RoomQueryFilter
import io.github.wechaty.user.Contact
import io.github.wechaty.user.Room
import org.apache.commons.collections4.CollectionUtils
import org.slf4j.LoggerFactory
import java.util.*

class RoomManager(wechaty: Wechaty) : Accessory(wechaty) {

    private val roomaCache: Cache<String, Room> = Caffeine.newBuilder().build()

    fun create(contactList: List<Contact>, topic: String?): Room {
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
        } catch (e: Exception) {
            log.error("create() room error", e)
            throw e
        }
    }

    fun findAll(query: RoomQueryFilter): List<Room> {

        log.debug("findAll {}", query)

        return try {
            val roomIdList = wechaty.getPuppet().roomSearch(query).get()
            roomIdList.map {
                load(it)
            }.mapNotNull {
                try {
                    it.ready().get()
                    it
                } catch (e: Exception) {
                    log.error("findAll() room.ready() rejection {}", e)
                    null
                }
            }
        } catch (e: Exception) {
            log.error("findAll() rejected: {}", e)
            listOf()
        }

    }

    fun find(query: RoomQueryFilter): Room? {
        val roomList = findAll(query)
        if (CollectionUtils.isEmpty(roomList)) {
            return null
        }

        if (roomList.size > 1) {
            log.warn("find got more then one{} result", roomList.size)
        }

        roomList.forEach {
            val valid = wechaty.getPuppet().roomValidate(it.id).get()
            if (valid) {
                log.debug("find() confirm room{} with id={} is valid result, return it.", it, it.id)
                return it
            } else {
                log.debug("find() confirm room{} with id={} is INVALID result, try next", it, it.id)
            }
        }

        return null

    }

    fun load(id: String): Room {
        return roomaCache.get(id) {
            Room(wechaty, id)
        }!!
    }

    companion object {
        private val log = LoggerFactory.getLogger(RoomManager::class.java)
    }


}

