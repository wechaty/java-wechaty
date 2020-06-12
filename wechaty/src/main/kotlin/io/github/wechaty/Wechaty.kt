package io.github.wechaty;


import io.github.wechaty.eventEmitter.Event
import io.github.wechaty.eventEmitter.EventEmitter
import io.github.wechaty.eventEmitter.Listener
import io.github.wechaty.io.github.wechaty.schemas.EventEnum
import io.github.wechaty.listener.*
//import io.github.wechaty.memorycard.MemoryCard
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
    private val globalPluginList: MutableList<WechatyPlugin> = mutableListOf()

    @Volatile
    private var readyState = StateEnum.OFF

    @Volatile
    private var status = StateEnum.OFF

//    private var memory:MemoryCard? = null

    val tagManager: TagManager = TagManager(this)
    val contactManager = ContactManager(this)
    val messageManager = MessageManager(this)
    val roomManager = RoomManager(this)
    val roomInvitationMessage = RoomInvitationManager(this)

    init {
//        this.memory = wechatyOptions.memory
        installGlobalPlugin()
    }


    fun start(await: Boolean = false):Wechaty {

        initPuppet()
        puppet.start().get()
        status = StateEnum.ON
        emit(EventEnum.START, "")

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
        return this
    }

    fun stop() {
        puppet.stop()
    }

    fun name(): String {
        return wechatyOptions.name
    }

    fun onLogin(listener: LoginListener):Wechaty{
        return on(EventEnum.LOGIN,listener)
    }

    fun onScan(listener: ScanListener):Wechaty{
        return on(EventEnum.SCAN,listener);
    }

    fun onRoomJoin(listener: RoomJoinListener):Wechaty {
        return on(EventEnum.ROOM_JOIN,listener)
    }

    fun onRoomLeave(listener: RoomLeaveListener):Wechaty {
        return on(EventEnum.ROOM_LEAVE,listener)
    }

    fun onRoomTopic(listener: RoomTopicListener):Wechaty {
        return on(EventEnum.ROOM_TOPIC,listener)
    }

    fun onMessage(listener: MessageListener):Wechaty{
        return on(EventEnum.MESSAGE,listener)
    }

    fun use(vararg plugins: WechatyPlugin):Wechaty{
        plugins.forEach {
            it(this)
        }
        return this
    }

    private fun installGlobalPlugin(){
        for (item in globalPluginList) {
            item(this)
        }
    }

    private fun on(event: Event,listener:LoginListener):Wechaty{
        super.on(event, object : Listener {
            override fun handler(vararg any: Any) {
                listener.handler(any[0] as ContactSelf)
            }
        })
        return this
    }

    private fun on(event: Event, listener: DongListener):Wechaty {
        return this
    }

    private fun on(event: Event, listener: ScanListener):Wechaty{
        super.on(event, object : Listener {
            override fun handler(vararg any: Any) {
                listener.handler(any[0] as String, any[1] as ScanStatus, any[2] as String)
            }
        })
        return this
    }

    private fun on(event: Event, listener: MessageListener):Wechaty {
        super.on(event, object : Listener {
            override fun handler(vararg any: Any) {
                listener.handler(any[0] as Message)
            }
        })
        return this
    }

    private fun on(eventName: Event, listener: RoomJoinListener):Wechaty {
        super.on(eventName, object : Listener {
            override fun handler(vararg any: Any) {
                listener.handler(any[0] as Room, any[1] as List<Contact>, any[2] as Contact, any[3] as Date)
            }
        })
        return this
    }

    private fun on(eventName: Event, listener: RoomLeaveListener):Wechaty {
        super.on(eventName, object : Listener {
            override fun handler(vararg any: Any) {
                listener.handler(any[0] as Room, any[1] as List<Contact>, any[2] as Contact, any[3] as Date)
            }
        })
        return this
    }

    private fun on(eventName: Event, listener: RoomTopicListener):Wechaty {
        super.on(eventName, object : Listener {
            override fun handler(vararg any: Any) {
                listener.handler(any[0] as Room, any[1] as String, any[2] as String, any[3] as Contact, any[4] as Date)
            }
        })
        return this
    }

    private fun initPuppet() {
//        this.puppet = GrpcPuppet(puppetOptions)
        this.puppet = PuppetManager.resolveInstance(wechatyOptions).get()
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


    private fun initPuppetEventBridge(puppet: Puppet) {

        val eventNameList = PUPPET_EVENT_DICT.keys

        eventNameList.forEach {

            when (it) {
                EventEnum.DONG -> {
                    puppet.on(it, object : PuppetDongListener {
                        override fun handler(payload: EventDongPayload) {
                            emit(EventEnum.DONG, payload.data)
                        }
                    })
                }

                EventEnum.ERROR -> {
                    puppet.on(it, object : PuppetErrorListener {
                        override fun handler(payload: EventErrorPayload) {
                            emit(EventEnum.ERROR, Exception(payload.data))
                        }
                    })
                }

                EventEnum.HEART_BEAT -> {
                    puppet.on(it, object : PuppetHeartbeatListener {
                        override fun handler(payload: EventHeartbeatPayload) {
                            emit(EventEnum.HEART_BEAT, payload.data)
                        }
                    })
                }

                EventEnum.FRIENDSHIP -> {
                    puppet.on(it, object : PuppetFriendshipListener {
                        override fun handler(payload: EventFriendshipPayload) {
                            val friendship = friendship().load(payload.friendshipId)
                            friendship.ready()
                            emit(EventEnum.FRIENDSHIP, friendship)
                        }
                    })
                }
                EventEnum.LOGIN -> {
                    puppet.on(it, object : PuppetLoginListener {
                        override fun handler(payload: EventLoginPayload) {
                            val contact = contactManager.loadSelf(payload.contactId)
                            contact.ready()
                            emit(EventEnum.LOGIN, contact)
                        }
                    })
                }

                EventEnum.LOGOUT -> {
                    puppet.on(it, object : PuppetLogoutListener {
                        override fun handler(payload: EventLogoutPayload) {
                            val contact = contactManager.loadSelf(payload.contactId)
                            contact.ready()
                            emit(EventEnum.LOGOUT, contact, payload.data)
                        }
                    })
                }

                EventEnum.MESSAGE -> {
                    puppet.on(it, object : PuppetMessageListener {
                        override fun handler(payload: EventMessagePayload) {
                            val msg = messageManager.load(payload.messageId)
                            msg.ready().get()
                            emit(EventEnum.MESSAGE, msg)

                            val room = msg.room()
                            room?.emit(EventEnum.MESSAGE, msg)
                        }
                    })
                }

                EventEnum.READY -> {
                    puppet.on(it, object : PuppetReadyListener {
                        override fun handler(payload: EventReadyPayload) {
                            emit(EventEnum.READY);
                            readyState = StateEnum.ON
                        }
                    })
                }

                EventEnum.ROOM_INVITE -> {
                    puppet.on(it, object : PuppetRoomInviteListener {
                        override fun handler(payload: EventRoomInvitePayload) {
                            val roomInvitation = roomInvitationMessage.load(payload.roomInvitationId)
                            emit(EventEnum.ROOM_INVITE, roomInvitation)
                        }
                    })
                }

                EventEnum.ROOM_JOIN -> {
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
                            emit(EventEnum.ROOM_JOIN, room, inviteeList, inviter, date)
                            room.emit(EventEnum.JOIN, inviteeList, inviter, date)
                        }
                    })
                }


                EventEnum.ROOM_LEAVE -> {
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

                            emit(EventEnum.ROOM_LEAVE, room, leaverList, remover, date)
                            room.emit(EventEnum.LEAVE, leaverList, remover, date)
                        }
                    })
                }

                EventEnum.ROOM_TOPIC -> {
                    puppet.on(it, object : PuppetRoomTopicListener {
                        override fun handler(payload: EventRoomTopicPayload) {
                            val room = roomManager.load(payload.roomId)
                            room.sync()

                            val changer = contactManager.loadSelf(payload.changerId)
                            changer.ready()
                            val date = Date(payload.timestamp * 1000)

                            emit(EventEnum.ROOM_TOPIC, room, payload.newTopic, payload.oldTopic, changer, date)
                            room.emit(EventEnum.TOPIC, payload.newTopic, payload.oldTopic, changer, date)
                        }
                    })
                }

                EventEnum.SCAN -> {
                    puppet.on(it, object : PuppetScanListener {
                        override fun handler(payload: EventScanPayload) {
                            emit(EventEnum.SCAN, payload.qrcode ?: "", payload.status, payload.data ?: "")
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



