package io.github.wechaty;

//import io.github.io.github.user.Room

import io.github.wechaty.eventEmitter.EventEmitter
import io.github.wechaty.eventEmitter.Listener
import io.github.wechaty.grpc.GrpcPuppet
import io.github.wechaty.listener.*
import io.github.wechaty.schemas.*
import io.github.wechaty.user.*
import io.github.wechaty.user.manager.*
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.locks.ReentrantLock


class Wechaty private constructor(private var wechatyOptions: WechatyOptions) : EventEmitter() {

    private val LOCK = ReentrantLock()
    private val STOP = LOCK.newCondition()


    private lateinit var puppet: Puppet
    private val puppetOptions: PuppetOptions = wechatyOptions.puppetOptions!!

    @Volatile
    private var readyState = StateEnum.OFF

    @Volatile
    private var status = StateEnum.OFF

    val tagManager: TagManager = TagManager(this)
    val contactManager = ContactManager(this)
    val messageManager = MessageManager(this)
    val roomManager = RoomManager(this)
    val roomInvitationMessage = RoomInvitationManager(this)


    fun start(await: Boolean = false) {

        initPuppet()
        puppet.start().get()
        status = StateEnum.ON
        emit("start", "")

        if (await) {
            addHook()
            log.info("start Wechaty")
            try {
                LOCK.lock();
                STOP.await();
            } catch (e: InterruptedException) {
                log.warn(" service   stopped, interrupted by other thread!", e);
            } finally {
                LOCK.unlock()
            }
        }

    }

    fun stop() {
        puppet.stop()
    }

    fun name(): String {
        return wechatyOptions.name
    }

    fun on(event: String, listener: DongListener) {

    }

    fun on(event: String, listener: ScanListener) {
        super.on(event, object : Listener {
            override fun handler(vararg any: Any) {
                listener.handler(any[0] as String, any[1] as ScanStatus, any[2] as String)
            }
        })
    }

    fun on(event: String, listener: MessageListener) {
        super.on(event, object : Listener {
            override fun handler(vararg any: Any) {
                listener.handler(any[0] as Message)
            }
        })
    }

    fun on(eventName: String, listener: RoomJoinListener) {
        super.on(eventName, object : Listener {
            override fun handler(vararg any: Any) {
                listener.handler(any[0] as Room, any[1] as List<Contact>, any[2] as Contact, any[3] as Date)
            }
        })
    }

    fun on(eventName: String, listener: RoomLeaveListener) {
        super.on(eventName, object : Listener {
            override fun handler(vararg any: Any) {
                listener.handler(any[0] as Room, any[1] as List<Contact>, any[2] as Contact, any[3] as Date)
            }
        })
    }

    fun on(eventName: String, listener: RoomTopicListener) {
        super.on(eventName, object : Listener {
            override fun handler(vararg any: Any) {
                listener.handler(any[0] as Room, any[1] as String, any[2] as String, any[3] as Contact, any[4] as Date)
            }
        })
    }

    private fun initPuppet() {
        this.puppet = GrpcPuppet(puppetOptions)
        initPuppetEventBridge(puppet)
    }

    fun friendship(): Friendship {
        return Friendship(this);
    }


    fun getPuppet(): Puppet {
        return puppet
    }

    fun userSelf(): ContactSelf {
        val userId = puppet.selfId()
        val user = this.contactManager.loadSelf(userId!!)
        return user
    }


    protected fun initPuppetEventBridge(puppet: Puppet) {

        val eventNameList = PUPPET_EVENT_DICT.keys

        eventNameList.forEach {

            when (it) {
                "dong" -> {
                    puppet.on(it, object : PuppetDongListener {
                        override fun handler(payload: EventDongPayload) {
                            emit("dong", payload.data)
                        }
                    })
                }

                "error" -> {
                    puppet.on(it, object : PuppetErrorListener {
                        override fun handler(payload: EventErrorPayload) {
                            emit("error", Exception(payload.data))
                        }
                    })
                }

                "heartbeat" -> {
                    puppet.on(it, object : PuppetHeartbeatListener {
                        override fun handler(payload: EventHeartbeatPayload) {
                            emit("heartbeat", payload.data)
                        }
                    })
                }

                "friendship" -> {
                    puppet.on(it, object : PuppetFriendshipListener {
                        override fun handler(payload: EventFriendshipPayload) {
                            val friendship = friendship().load(payload.friendshipId)
                            friendship.ready()
                            emit("friendship", friendship)
                        }
                    })
                }
                "login" -> {
                    puppet.on(it, object : PuppetLoginListener {
                        override fun handler(payload: EventLoginPayload) {
                            val contact = contactManager.loadSelf(payload.contactId)
                            contact.ready()
                            emit("login", contact)
                        }
                    })
                }

                "logout" -> {
                    puppet.on(it, object : PuppetLogoutListener {
                        override fun handler(payload: EventLogoutPayload) {
                            val contact = contactManager.loadSelf(payload.contactId)
                            contact.ready()
                            emit("logout", contact, payload.data)
                        }
                    })
                }

                "message" -> {
                    puppet.on(it, object : PuppetMessageListener {
                        override fun handler(payload: EventMessagePayload) {
                            val msg = messageManager.load(payload.messageId)
                            msg.ready().get()
                            emit("message", msg)

                            val room = msg.room()
                            room?.emit("message", msg)
                        }
                    })
                }

                "ready" -> {
                    puppet.on(it, object : PuppetReadyListener {
                        override fun handler(payload: EventReadyPayload) {
                            emit("ready");
                            readyState = StateEnum.ON
                        }
                    })
                }

                "room-invite" -> {
                    puppet.on(it, object : PuppetRoomInviteListener {
                        override fun handler(payload: EventRoomInvitePayload) {
                            val roomInvitation = roomInvitationMessage.load(payload.roomInvitationId)
                            emit("room-invite", roomInvitation)
                        }
                    })
                }

                "room-join" -> {
                    puppet.on(it, object : PuppetRoomJoinListener {
                        override fun handler(payload: EventRoomJoinPayload) {
                            val room = roomManager.load(payload.roomId)
                            room.sync().get()

                            val inviteeList = payload.inviteeIdList.map { id ->
                                val contact = contactManager.loadSelf(id)
                                contact.ready()
                                return@map contact
                            }

                            val inviter = contactManager.loadSelf(payload.inviterId)
                            inviter.ready()

                            val date = Date(payload.timestamp * 1000)
                            emit("room-join", room, inviteeList, inviter, date)
                            room.emit("join", inviteeList, inviter, date)
                        }
                    })
                }


                "room-leave" -> {
                    puppet.on(it, object : PuppetRoomLeaveListener {
                        override fun handler(payload: EventRoomLeavePayload) {
                            val room = roomManager.load(payload.roomId)
                            room.sync()

                            val leaverList = payload.removeeIdList.map { id ->
                                val contact = contactManager.loadSelf(id)
                                contact.ready()
                                return@map contact
                            }

                            val remover = contactManager.loadSelf(payload.removerId)
                            remover.ready()
                            val date = Date(payload.timestamp * 1000)

                            emit("room-leave", room, leaverList, remover, date)
                            room.emit("leave", leaverList, remover, date)
                        }
                    })
                }

                "room-topic" -> {
                    puppet.on(it, object : PuppetRoomTopicListener {
                        override fun handler(payload: EventRoomTopicPayload) {
                            val room = roomManager.load(payload.roomId)
                            room.sync()

                            val changer = contactManager.loadSelf(payload.changerId)
                            changer.ready()
                            val date = Date(payload.timstamp * 1000)

                            emit("room-topic", room, payload.newTopic, payload.oldTopic, changer, date)
                            room.emit("topic", payload.newTopic, payload.oldTopic, changer, date)
                        }
                    })
                }

                "scan" -> {
                    puppet.on(it, object : PuppetScanListener {
                        override fun handler(payload: EventScanPayload) {
                            emit("scan", payload.qrcode ?: "", payload.status, payload.data ?: "")
                        }
                    })
                }
            }
        }
    }

    companion object Factory {
        @JvmStatic
        fun instance(token: String): Wechaty {
            val puppetOptions = PuppetOptions()
            puppetOptions.token = token
            val wechatyOptions = WechatyOptions()
            wechatyOptions.puppetOptions = puppetOptions

            return Wechaty(wechatyOptions)
        }

        @JvmStatic
        fun instance(wechatyOptions: WechatyOptions): Wechaty {
            return Wechaty(wechatyOptions)
        }

        private val log = LoggerFactory.getLogger(Wechaty::class.java)
    }

    private fun addHook() {
        Runtime.getRuntime().addShutdownHook(Thread(Runnable {
            try {
                //调用 wechaty 的 stop 实现优雅的退出
                stop()
            } catch (e: java.lang.Exception) {
                log.error("StartMain stop exception ", e)
            }
            log.info("wechaty stop")
            try {
                LOCK.lock()
                STOP.signal()
            } finally {
                LOCK.unlock()
            }
        }, "StartMain-shutdown-hook"))
    }
}



