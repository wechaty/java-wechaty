package io.github.wechaty;

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.github.wechaty.grpc.GrpcPuppet
import io.github.wechaty.io.github.wechaty.Listener.*
import io.github.wechaty.io.github.wechaty.Status
import io.github.wechaty.io.github.wechaty.schemas.*
import io.github.wechaty.io.github.wechaty.utils.GenericCodec
import io.github.wechaty.schemas.PuppetOptions
import io.github.wechaty.user.Contact
import io.github.wechaty.user.ContactSelf
import io.github.wechaty.user.Message
//import io.github.wechaty.user.Room
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class Wechaty private constructor(private var wechatyOptions: WechatyOptions) {

    private lateinit var puppet: Puppet
    private var vertx: Vertx = Vertx.vertx()
    private var wechatyEb: EventBus
    private val puppetOptions:PuppetOptions = wechatyOptions.puppetOptions!!

    @Volatile
    private var readyState = Status.OFF

    @Volatile
    private var status = Status.OFF

    private val contactCache: Cache<String, Contact> = Caffeine.newBuilder().build()
    private val messageCache: Cache<String, Message> = Caffeine.newBuilder().build()
//    private val roomCache:Cache<String,Room> = Caffeine.newBuilder().build()


    public fun start() :Future<Void>{
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

    }

    fun on(event: String, listener: MessageListener) {
        val consumer = wechatyEb.consumer<Message>(event)
        consumer.handler() {
            val body = it.body()
            listener.handler(body)
        }
    }

    fun room() {

    }

    fun message(): Message {
        return Message(this)
    }

    fun contact(): Contact {
        return Contact(this)
    }

    fun contactSelf(): ContactSelf {
        return ContactSelf(this)
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

    private fun initPuppet(){
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
                            val msg = message().load(messageId)
                            msg.ready().get()
                            wechatyEb.publish("message", msg)
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



