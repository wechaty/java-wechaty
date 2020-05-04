package io.github.wechaty;

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.github.wechaty.grpc.GrpcPuppet
import io.github.wechaty.io.github.wechaty.Listener.*
import io.github.wechaty.io.github.wechaty.Status
import io.github.wechaty.io.github.wechaty.schemas.*
import io.github.wechaty.io.github.wechaty.utils.GenericCodec
import io.github.wechaty.io.github.wechaty.utils.JsonUtils
import io.github.wechaty.schemas.PuppetOptions
import io.github.wechaty.user.Contact
import io.github.wechaty.user.ContactSelf
import io.github.wechaty.user.Message
import io.github.wechaty.user.Room
//import io.github.wechaty.user.Room
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.json
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class Wechaty private constructor(private var wechatyOptions: WechatyOptions) {

    private lateinit var puppet: Puppet
    private var vertx: Vertx = Vertx.vertx()
    private var wechatyEb: EventBus
    private val puppetOptions: PuppetOptions = wechatyOptions.puppetOptions!!

    @Volatile
    private var readyState = Status.OFF

    @Volatile
    private var status = Status.OFF

    private val contactCache: Cache<String, Contact> = Caffeine.newBuilder().build()
    private val messageCache: Cache<String, Message> = Caffeine.newBuilder().build()
    private val roomCache: Cache<String, Room> = Caffeine.newBuilder().build()
//    private val roomCache:Cache<String,Room> = Caffeine.newBuilder().build()


    public fun start(): Future<Void> {

        log.info("start Wechaty")

        return CompletableFuture.supplyAsync {
            initPuppet()
            puppet.start().get()
            status = Status.ON
            wechatyEb.publish("start", "")
            return@supplyAsync null
        }
    }

    public fun name(): String {
        return wechatyOptions.name
    }

    fun on(event: String, listener: DongListener) {

    }

    fun on(event: String, listener: ScanListener) {
        val consumer = wechatyEb.consumer<String>(event)
        consumer.handler {
            val body = it.body()

            log.info("scan body is {}",body)

            val jsonObject = JsonObject(body)
            val code = jsonObject.getInteger("scanStatus")
            listener.handler(jsonObject.getString("qrcode"),ScanStatus.getByCode(code),jsonObject.getString("data"))

        }
    }

    fun on(event: String, listener: MessageListener) {
        val consumer = wechatyEb.consumer<Message>(event)
        consumer.handler() {
            val body = it.body()
            listener.handler(body)
        }
    }

    fun message(): Message {
        return Message(this)
    }

    fun contact(): Contact {
        return Contact(this)
    }

    fun getConactFromCache(id: String): Contact? {
        return contactCache.getIfPresent(id);
    }

    fun putContactToCache(id: String, contact: Contact) {
        contactCache.put(id, contact)
    }

    fun delContactFromCache(id: String) {
        contactCache.invalidate(id)
    }

    fun contactSelf(): ContactSelf {
        return ContactSelf(this)
    }

    fun room():Room {
        return Room(this)
    }

    fun getRoomFromCache(id:String):Room?{
        return roomCache.getIfPresent(id)
    }

    fun putRoomToCache(id:String,room:Room){
        roomCache.put(id,room)
    }

    fun delRoomFromCache(id:String){
        roomCache.invalidate(id)
    }

    private fun initPuppet() {
        this.puppet = GrpcPuppet(puppetOptions)
        initPuppetEventBridge(puppet)
    }


    init {
        this.wechatyEb = vertx.eventBus()
        initEventCodec()
    }

    public fun getPuppet(): Puppet {
        return puppet
    }


    protected fun initPuppetEventBridge(puppet: Puppet) {

        val eventNameList = PUPPET_EVENT_DICT.keys

        eventNameList.forEach {

            when (it) {
                "dong" -> {

                }

                "scan" ->{
                    puppet.on("scan",object : PuppetScanListener{
                        override fun handler(qrcode: String?, scanStatus: ScanStatus, data: String?) {

                            val scanJson = JsonUtils.write(mapOf(
                                "qrcode" to qrcode,
                                "scanStatus" to scanStatus.code,
                                "data" to data
                            ))

                            log.info("scan json is {}",scanJson)

                            wechatyEb.publish("scan",scanJson)

                        }
                    })
                }

                "login" -> {
                    puppet.on("login", object : PuppetLoginListener {
                        override fun handler(contactId: String) {
                            val contact = contactSelf().load(contactId)
                            contact.ready()
                            wechatyEb.publish("login", contact)
                        }
                    })
                }

                "ready" -> {
                    puppet.on("ready", object : PuppetReadyListener {
                        override fun handler() {
                            wechatyEb.publish("ready", "");
                            readyState = Status.ON
                        }
                    })
                }

                "message" -> {

                    puppet.on("message", object : PuppetMessageListener {
                        override fun handler(messageId: String) {
                            CompletableFuture.runAsync {
                                val msg = message().load(messageId)
                                msg.ready().get()
                                wechatyEb.publish("message", msg)
                            }
                        }
                    })
                }
            }

        }
    }

    private fun initEventCodec() {
        wechatyEb.registerDefaultCodec(Message::class.java, GenericCodec(Message::class.java))
        wechatyEb.registerDefaultCodec(Contact::class.java, GenericCodec(Contact::class.java))
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


}



