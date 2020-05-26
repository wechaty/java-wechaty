package io.github.wechaty.user

import InviteListener
import JoinListener
import LeaveListener
import MessageListener
import TopicListener
import io.github.wechaty.Accessory
import io.github.wechaty.Puppet
import io.github.wechaty.Wechaty
import io.github.wechaty.eventEmitter.Listener
import io.github.wechaty.filebox.FileBox
import io.github.wechaty.schemas.RoomMemberQueryFilter
import io.github.wechaty.schemas.RoomPayload
import io.github.wechaty.type.Sayable
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class Room(wechaty: Wechaty, val id: String) : Accessory(wechaty), Sayable {

    private val puppet: Puppet = wechaty.getPuppet()
    private var payload: RoomPayload? = null

    fun sync(): Future<Void> {
        return ready(true)
    }

    override fun say(something: Any, contact: Contact): Future<Any> {

        var msgId: String? = null

        return CompletableFuture.supplyAsync {
            when (something) {

                is String -> {
                    msgId = puppet.messageSendText(id, something).get()

                }

            }

            if (StringUtils.isNotEmpty(msgId)) {
                val message = wechaty.messageManager.load(msgId!!)
                message.ready().get()
                return@supplyAsync message
            }

            return@supplyAsync null

        }
    }

    fun say(something: Any, vararg varList: List<Any>): Future<Any> {

        var msgId: String?

        return CompletableFuture.supplyAsync {
            when (something) {
                //TODO(array)

                is String -> {
                    var mentionList = listOf<Any>()
                    if (varList.isNotEmpty()) {
                        varList.forEach {
                            if (it !is Contact) {
                                throw Exception("mentionList must be contact when not using String array function call.")
                            }
                        }
                        //todo(varList ? List<List<Any>>)
                        mentionList = varList[0]
                    }

                    msgId = wechaty.getPuppet().messageSendText(id, something).get()
                }
                is FileBox -> {
                    msgId = wechaty.getPuppet().messageSendFile(id, something).get()
                }

                is UrlLink -> {
                    msgId = wechaty.getPuppet().messageSendUrl(id, something.payload).get()
                }

                is MiniProgram -> {
                    msgId = wechaty.getPuppet().messageSendMiniProgram(id, something.payload).get()
                }

                else -> {
                    throw Exception("unknown message")
                }

            }

            if (msgId != null) {
                val msg = wechaty.messageManager.load(msgId!!)
                return@supplyAsync msg
            }

            return@supplyAsync null
        }
    }

    fun ready(forceSync: Boolean = false): Future<Void> {
        return CompletableFuture.supplyAsync {
            if (!forceSync && isRead()) {
                return@supplyAsync null
            }

            if (forceSync) {
                puppet.roomPayloadDirty(id).get()
                puppet.roomMemberPayloadDirty(id).get()
            }

            this.payload = puppet.roomPayload(id).get()
            log.info("get room payload is {} by id {}", payload, id)
            if (payload == null) {
                throw Exception("no payload")
            }

            val memberIdList = puppet.roomMemberList(id).get()

            memberIdList.map {
                wechaty.contactManager.load(it)
            }.forEach {
                it.ready()
            }
            return@supplyAsync null
        }
    }

    fun on(eventName: String, listener: InviteListener) {
        super.on(eventName, object : Listener {
            override fun handler(vararg any: Any) {
                listener.handler(any[0] as Contact, any[1] as RoomInvitation)
            }
        })
    }

    fun on(eventName: String, listener: LeaveListener) {
        super.on(eventName, object : Listener {
            override fun handler(vararg any: Any) {
                listener.handler(any[0] as List<Contact>, any[1] as Contact, any[2] as Date)
            }
        })
    }

    fun on(eventName: String, listener: MessageListener) {
        super.on(eventName, object : Listener {
            override fun handler(vararg any: Any) {
                listener.handler(any[0] as Message, any[1] as Date)
            }
        })
    }

    fun on(eventName: String, listener: JoinListener) {
        super.on(eventName, object : Listener {
            override fun handler(vararg any: Any) {
                listener.handler(any[0] as List<Contact>, any[1] as Contact, any[2] as Date)
            }
        })
    }

    fun on(eventName: String, listener: TopicListener) {
        super.on(eventName, object : Listener {
            override fun handler(vararg any: Any) {
                listener.handler(any[0] as String, any[1] as String, any[2] as Contact, any[3] as Date)
            }
        })
    }

    fun add(contact: Contact): Future<Void> {
        return CompletableFuture.supplyAsync {
            puppet.roomAdd(this.id, contact.id).get()
            return@supplyAsync null
        }
    }

    fun del(contact: Contact): Future<Void> {
        return CompletableFuture.supplyAsync {
            puppet.roomDel(this.id, contact.id).get();
            return@supplyAsync null
        }
    }

    fun quit(): Future<Void> {
        return CompletableFuture.supplyAsync {
            puppet.roomQuit(this.id).get()
            return@supplyAsync null
        }
    }

    fun memberAll(query: RoomMemberQueryFilter?): List<Contact> {

        if (query == null) {
            return memberList()
        }

        val contactIdList = wechaty.getPuppet().roomMemberSearch(this.id, query).get()
        val contactList = contactIdList.map {
            wechaty.contactManager.load(id)
        }

        return contactList

    }

    fun memberList(): List<Contact> {

        val memberIdList = wechaty.getPuppet().roomMemberList(this.id).get()

        if (CollectionUtils.isEmpty(memberIdList)) {
            return listOf()
        }

        val contactList = memberIdList.map {
            wechaty.contactManager.load(id)
        }
        return contactList

    }

    fun alias(contact: Contact): String? {

        val roomMemberPayload = wechaty.getPuppet().roomMemberPayload(this.id, contact.id).get()

        return roomMemberPayload?.roomAlias


    }

    fun isRead(): Boolean {
        return payload != null
    }

    companion object {
        private val log = LoggerFactory.getLogger(Room::class.java)
    }
}

val ROOM_EVENT_DICT = mapOf(
    "invite" to "tbw",
    "join" to "tbw",
    "leave" to "tbw",
    "message" to "message that received in this room",
    "topic" to "tbw"
)
