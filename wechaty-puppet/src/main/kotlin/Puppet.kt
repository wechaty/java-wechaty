package io.github.wechaty

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.google.common.base.Preconditions
import com.google.common.collect.Lists
import com.google.common.util.concurrent.RateLimiter
import io.github.wechaty.io.github.wechaty.Listener.*
import io.github.wechaty.io.github.wechaty.StateEnum
import io.github.wechaty.io.github.wechaty.filebox.FileBox
import io.github.wechaty.io.github.wechaty.schemas.*
import io.github.wechaty.io.github.wechaty.throwUnsupportedError
import io.github.wechaty.io.github.wechaty.utils.GenericCodec
import io.github.wechaty.io.github.wechaty.watchdag.WatchDog
import io.github.wechaty.io.github.wechaty.watchdag.WatchdogFood
import io.github.wechaty.io.github.wechaty.watchdag.WatchdogListener
import io.github.wechaty.schemas.*
import io.github.wechaty.utils.FutureUtils
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicLong


val PUPPET_COUNT = AtomicLong()

abstract class Puppet {

    @Volatile
    protected var state = StateEnum.OFF

    private val HEARTBEAT_COUNTER = AtomicLong()
    private val HOSTIE_KEEPALIVE_TIMEOUT = 15 * 1000L
    private val DEFAULT_WATCHDOG_TIMEOUT = 60L

    private lateinit var cacheContactPayload: Cache<String, ContactPayload>
    private lateinit var cacheFriendshipPayload: Cache<String, FriendshipPayload>
    private lateinit var cacheMessagePayload: Cache<String, MessagePayload>
    private lateinit var cacheRoomPayload: Cache<String, RoomPayload>
    private lateinit var cacheRoomMemberPayload: Cache<String, RoomMemberPayload>
    private lateinit var cacheRoomInvitationPayload: Cache<String, RoomInvitationPayload>
    protected var vertx: Vertx
    private var eb: EventBus
    private val count = AtomicLong()
    private var id: String? = null
    protected var puppetOptions: PuppetOptions? = null

    private var heartbeatTimerId:Long = 0
    private val watchDog:WatchDog

    /**
     *
     */
//    constructor() {
//        this.puppetOptions = PuppetOptions()
//        vertx = Vertx.vertx()
//        eb = vertx.eventBus()
//        eb.registerDefaultCodec(EventScanPayload::class.java, GenericCodec(EventScanPayload::class.java))
//        eb.registerDefaultCodec(EventMessagePayload::class.java, GenericCodec(EventMessagePayload::class.java))
//        initCache()
//    }

    constructor(puppetOptions: PuppetOptions) {

        count.addAndGet(1)
        this.puppetOptions = puppetOptions

        val timeOut = puppetOptions.timeout ?:DEFAULT_WATCHDOG_TIMEOUT
        watchDog = WatchDog(1000 * timeOut,"puppet")

        vertx = Vertx.vertx()
        eb = vertx.eventBus()



        on("heartbeat",object :PuppetHeartbeatListener{
            override fun handler(data: String) {
                log.info("heartbeat -> ${data}")
                val watchdogFood = WatchdogFood(1000 * timeOut)
                watchdogFood.data = data
                watchDog.feed(watchdogFood);
            }
        })

        this.watchDog.on("reset",object : WatchdogListener{
            override fun handler(event: EventResetPayload) {
                eb.publish("reset",event)
            }
        })

        // 一秒只有一次
        val rateLimiter = RateLimiter.create(1.0)

        on("reset",object :PuppetResetListener{
            override fun handler(reason: String) {
                log.info("get a reset message")
                if(rateLimiter.tryAcquire()){
                    reset(reason)
                }
            }
        })

        initEventCodec()
        initCache()
        initHeart()

    }

    private fun initEventCodec() {
        eb.registerDefaultCodec(EventScanPayload::class.java, GenericCodec(EventScanPayload::class.java))
        eb.registerDefaultCodec(EventDongPayload::class.java, GenericCodec(EventDongPayload::class.java))
        eb.registerDefaultCodec(EventMessagePayload::class.java, GenericCodec(EventMessagePayload::class.java))
        eb.registerDefaultCodec(EventLoginPayload::class.java, GenericCodec(EventLoginPayload::class.java))
        eb.registerDefaultCodec(EventHeartbeatPayload::class.java, GenericCodec(EventHeartbeatPayload::class.java))
        eb.registerDefaultCodec(EventResetPayload::class.java, GenericCodec(EventResetPayload::class.java))
    }

//    constructor(token:String) {
//        this.puppetOptions = PuppetOptions()
//        puppetOptions!!.token = token
//        vertx = Vertx.vertx()
//        eb = vertx.eventBus()
//        eb.registerDefaultCodec(EventScanPayload::class.java, GenericCodec(EventScanPayload::class.java))
//        eb.registerDefaultCodec(EventMessagePayload::class.java, GenericCodec(EventMessagePayload::class.java))
//        initCache()
//    }

    protected fun getId(): String? {
        return id;
    }

    private fun initCache() {
        cacheContactPayload = Caffeine.newBuilder().build()
        cacheFriendshipPayload = Caffeine.newBuilder().build()
        cacheRoomPayload = Caffeine.newBuilder().build()
        cacheRoomMemberPayload = Caffeine.newBuilder().build()
        cacheRoomInvitationPayload = Caffeine.newBuilder().build()
        cacheMessagePayload = Caffeine.newBuilder().build()
    }

    private fun initHeart(){

//        heartbeadTimerId = vertx.setPeriodic(HOSTIE_KEEPALIVE_TIMEOUT,{ ->
//            val incrementAndGet = HEARTBEAT_COUNTER.incrementAndGet()
//            ding("`recover CPR #${incrementAndGet}")
//        })

        heartbeatTimerId = vertx.setPeriodic(HOSTIE_KEEPALIVE_TIMEOUT) { id ->
            log.info("timer")
            if(state == StateEnum.ON) {
                val incrementAndGet = HEARTBEAT_COUNTER.incrementAndGet()
                log.info("HEARTBEAT_COUNTER #{}", incrementAndGet)
                ding("`recover CPR #${incrementAndGet}")
            }
        }

    }

    fun emit(event: String, vararg args: Any) {
        eb.publish(event, args)
    }

    //dong
    fun on(event: String, listener: PuppetDongListener) {
        val consumer = eb.consumer<EventDongPayload>(event)
        consumer.handler {
            val body = it.body()
            listener.handler(body.data)
        }
    }

    fun on(event: String, listener: PuppetScanListener) {

        val consumer = eb.consumer<EventScanPayload>(event)
        consumer.handler {
            val body = it.body()

            val data = body.data
            val qrcode = body.qrcode
            val status = body.status

            listener.handler(qrcode, status, data)
        }

    }

    fun on(event: String, listener: PuppetLoginListener) {
        val consumer = eb.consumer<EventLoginPayload>(event)
        consumer.handler() {
            val body = it.body()
            val contactId = body.contactId
            listener.handler(contactId)
        }
    }

    fun on(event: String, listener: PuppetReadyListener) {
        val consumer = eb.consumer<String>(event)
        consumer.handler() {
            listener.handler()
        }
    }

    fun on(event: String, listener: PuppetMessageListener) {
        val consumer = eb.consumer<EventMessagePayload>(event)
        consumer.handler() {
            val body = it.body()
            listener.handler(body.messageId)
        }
    }

    fun on(event:String,listener: PuppetHeartbeatListener){
        val consumer = eb.consumer<EventHeartbeatPayload>(event)
        consumer.handler() {
            val body = it.body()
            listener.handler(body.data)
        }
    }

    fun on(event:String,listener: PuppetResetListener){
        val consumer = eb.consumer<EventResetPayload>(event)
        consumer.handler() {
            val body = it.body()
            listener.handler(body.data)
        }

    }


//    fun on(event: String, listener: ): Puppet {
//        eb.consumer(
//            event
//        ) { t: Message<Any?> ->
//            val friendshipId = t.body() as String?
//            listener.execute(friendshipId)
//        }
//        return this
//    }

    abstract fun start(): Future<Void>
    abstract fun stop(): Future<Void>
    open fun unref(){

    }

    protected fun reset(reason: String): Future<Void> {

        val future = CompletableFuture<Void>()

        if (state == StateEnum.OFF) {
            log.info("Puppet reset state is off")
            future.complete(null)
            return future
        }

        stop().get()
        start().get()

        return future;
    }


    protected fun login(userId: String): Future<Void> {
        log.info("Puppet login in ({})", userId)
        return CompletableFuture.runAsync {
            if (StringUtils.isNotBlank(userId)) {
                throw RuntimeException("must logout first before login again!")
            }
            id = userId
            emit("login", userId)
        }
    }

    abstract fun logout(): Future<Void>

    fun selfId(): String? {
        return id
    }

    public fun logonoff(): Boolean {
        return id == null
    }

    fun setId(id: String?) {
        this.id = id
    }


    /**
     * 抽象方法
     * Misc
     */
    abstract fun ding(data: String?)

    /**
     * contactSelf
     */
    abstract fun contactSelfName(name: String): Future<Void>
    abstract fun contactSelfQRCode(): Future<String>
    abstract fun contactSelfSignature(signature: String): Future<Void>

    /**
     *
     * Tag
     * tagContactAdd - add a tag for a Contact. Create it first if it not exist.
     * tagContactRemove - remove a tag from the Contact
     * tagContactDelete - delete a tag from Wechat
     * tagContactList(id) - get tags from a specific Contact
     * tagContactList() - get tags from all Contacts
     *
     */
    abstract fun tagContactAdd(tagId: String, contactId: String): Future<Void>
    abstract fun tagContactDelete(tagId: String): Future<Void>
    abstract fun tagContactList(contactId: String): Future<List<String>>
    abstract fun tagContactList(): Future<List<String>>
    abstract fun tagContactRemove(tagId: String, contactId: String): Future<Void>

    /**
     *
     * Contact
     *
     */
    abstract fun contactAlias(contactId: String): Future<String>
    abstract fun contactAlias(contactId: String, alias: String?): Future<Void>
    abstract fun getContactAvatar(contactId: String): Future<FileBox>
    abstract fun setContactAvatar(contactId: String, file: FileBox): Future<Void>
    abstract fun contactList(): Future<List<String>>
    protected abstract fun contactRawPayload(contractId: String): Future<ContactPayload>
    protected abstract fun contactRawPayloadParser(rawPayload: ContactPayload): Future<ContactPayload>

    open fun contactRoomList(contactId: String): Future<List<String?>>? {
        log.info("contractId is {}", contactId)
        val roomList = roomList().get()
        val roomPayloadFuture: List<CompletableFuture<RoomPayload>> = roomList
                .map { roomId: String ->
                    roomPayload(
                            roomId
                    )
                }
                .map(FutureUtils::toCompletable)
        val resultRoomIdList =
                FutureUtils.sequence(roomPayloadFuture)
        val roomPayloadList = resultRoomIdList.get()
        val result =
                roomPayloadList.filter { t: RoomPayload ->
                    val memberIdList = t.memberIdList
                    memberIdList.contains(contactId)
                }.map(RoomPayload::id)
        return CompletableFuture.completedFuture(result)
    }

    fun contactPayloadDirty(contactId: String): Future<Void?> {
        cacheRoomPayload.invalidate(contactId)
        return CompletableFuture.completedFuture(null)
    }

    fun contactSearch(query: ContactQueryFilter?, searchIdList: List<String>?): Future<List<String>> {

        log.info("query {},{} ", query, searchIdList)

        return CompletableFuture.supplyAsync {

            var list = searchIdList

            if (CollectionUtils.isEmpty(searchIdList)) {
                list = contactList().get()
            }

            if (query == null) {
                return@supplyAsync list
            }

            return@supplyAsync list!!.filter {
                val payload = contactPayload(it).get()
                return@filter StringUtils.equals(query.name, payload.name)
            }
        }
    }

    protected fun contactPayloadCache(contactId: String): ContactPayload? {

        val contactPayload = cacheContactPayload.getIfPresent(contactId)

        log.info("contactPayload is []", contactPayload)
        return contactPayload
    }

    public fun contactPayload(contactId: String): Future<ContactPayload> {

        val future = CompletableFuture<ContactPayload>()

        val contactPayload = contactPayloadCache(contactId)

        if (contactPayload != null) {
            future.complete(contactPayload)
            return future
        }

        val contactRawPayload = contactRawPayload(contactId).get()
        val payload = contactRawPayloadParser(contactRawPayload).get()

        cacheContactPayload.put(contactId, payload)
        future.complete(payload)
        return future


    }


    /**
     *
     * Friendship
     *
     */
    abstract fun friendshipAccept(friendshipId: String): Future<Void>
    abstract fun friendshipAdd(contractId: String, hello: String): Future<Void>
    abstract fun friendshipSearchPhone(phone: String): Future<String?>
    abstract fun friendshipSearchWeixin(weixin: String): Future<String?>
    abstract fun friendshipRawPayload(friendshipId: String): Future<FriendshipPayload>
    abstract fun friendshipRawPayloadParser(rawPayload: FriendshipPayload): Future<FriendshipPayload>
    fun friendshipSearch(condition: FriendshipSearchCondition): Future<String?> {
        log.info("friendshipSearch{}", condition)
        Preconditions.checkNotNull(condition)

        return if (StringUtils.isNotEmpty(condition.phone)) {
            friendshipSearchPhone(condition.phone!!)
        } else {
            friendshipSearchWeixin(condition.weixin!!)
        }
    }

    protected fun friendshipPayloadCache(friendshipId: String): FriendshipPayload? {
        log.info("friendshipId is {}", friendshipId)
        return cacheFriendshipPayload.getIfPresent(friendshipId)
    }

    protected fun friendshipPayloadDirty(frienshipId: String): Future<Void> {
        cacheFriendshipPayload.invalidate(frienshipId)
        return CompletableFuture.completedFuture(null);
    }

    fun friendshipPayload(friendshipId: String): Future<FriendshipPayload> {

        val future = CompletableFuture<FriendshipPayload>()

        val cachedPayload = friendshipPayloadCache(friendshipId)

        if (cachedPayload != null) {
            future.complete(cachedPayload)
            return future
        }

        val rawPayload = friendshipRawPayload(friendshipId).get()
        val payload = friendshipRawPayloadParser(rawPayload).get()

        cacheFriendshipPayload.put(friendshipId, payload)
        future.complete(payload)
        return future

    }

    public fun friendshipPayload(friendshipId: String, newPayload: FriendshipPayload?): Future<Void> {

        val future = CompletableFuture<Void>()

        if (newPayload != null) {
            cacheFriendshipPayload.put(friendshipId, newPayload)
            future.complete(null)
        }

        return future

    }


    /**
     * Message
     */

    abstract fun messageContact(messageId: String): Future<String>
    abstract fun messageFile(messageId: String): Future<FileBox>
    abstract fun messageImage(messageId: String,imageType:ImageType): Future<FileBox>
    abstract fun messageMiniProgram(messageId: String): Future<MiniProgramPayload>
    abstract fun messageUrl(messageId: String): Future<UrlLinkPayload>

    abstract fun messageSendContact(conversationId: String, contactId: String): Future<String?>
    abstract fun messageSendFile(conversationId: String, file: FileBox): Future<String?>

    abstract fun messageSendMiniProgram(conversationId: String, miniProgramPayload: MiniProgramPayload): Future<String?>
    abstract fun messageSendText(conversationId: String, text: String, mentionList: List<String>? = null): Future<String?>
    abstract fun messageSendUrl(conversationId: String, urlLinkPayload: UrlLinkPayload): Future<String?>

    abstract fun messageRecall(messageId: String): Future<Boolean>

    abstract fun messageRawPayload(messageId: String): Future<MessagePayload>
    abstract fun messageRawPayloadParser(rawPayload: MessagePayload): Future<MessagePayload>

    protected fun messagePaylaodCache(messageId: String): MessagePayload? {
        return cacheMessagePayload.getIfPresent(messageId)
    }

    fun messagePayload(messageId: String): Future<MessagePayload> {
        return CompletableFuture.supplyAsync {
            return@supplyAsync cacheMessagePayload.get(messageId) { t: String ->
                val get = messageRawPayload(t).get()
                return@get messageRawPayloadParser(get).get();
            }
        }
    }

    protected fun messagePayloadDirty(messageId: String): Future<Void> {
        cacheMessagePayload.invalidate(messageId);
        return CompletableFuture.completedFuture(null);
    }

    public fun messageList(): List<String> {
        val keys = cacheMessagePayload.asMap().keys
        return Lists.newArrayList(keys)
    }

    public fun messageSearch(query: MessageQueryFilter): Future<List<String>?> {
        TODO("TODO")
    }

    protected fun messageQueryFilterFactory(query: MessageQueryFilter) {
        TODO()
    }

    public fun messageForward(conversationId: String, messageId: String): Future<String?> {

        val payload = messagePayload(messageId).get()

        var newMsgId: String? = null

        when (payload.type) {

            MessageType.Attachment,
            MessageType.Audio,
            MessageType.Image,
            MessageType.Video -> {
                val file = messageFile(messageId).get()
                newMsgId = messageSendFile(conversationId, file).get()
            }

            MessageType.Text -> {
                if (payload.text != null) {
                    newMsgId = messageSendText(conversationId, payload.text!!).get()
                }
            }

            MessageType.Url -> {
                val url = messageUrl(messageId).get()
                newMsgId = messageSendUrl(conversationId, url).get()
            }

            MessageType.MiniProgram -> {
                val urlLinkPayload = messageUrl(messageId).get()
                newMsgId = messageSendUrl(conversationId, urlLinkPayload).get()
            }

            MessageType.Contact -> {
                val message = messageContact(messageId).get()
                newMsgId = messageSendContact(conversationId, message).get()
            }

            MessageType.ChatHistory,
            MessageType.Location,
            MessageType.Emoticon,
            MessageType.Transfer,
            MessageType.RedEnvelope,
            MessageType.Recalled -> {
                throwUnsupportedError()
            }

            else -> {
                throw Exception("Unsupported forward message type: ${payload.type}")
            }
        }

        return CompletableFuture.completedFuture(newMsgId)
    }

    /**
     * Room Invitation
     *
     */

    protected fun roomInvitationPayloadCache(roomInvitationId: String): RoomInvitationPayload? {
        val cachePayload = cacheRoomInvitationPayload.getIfPresent(roomInvitationId)
        return cachePayload
    }

    public abstract fun roomInvitationAccept(roomInvitation: String): Future<Void>

    protected abstract fun roomInvitationRawPayload(roomInvitationId: String): Future<RoomInvitationPayload>
    protected abstract fun roomInvitationRawPayloadParser(rawPayload: RoomInvitationPayload): Future<RoomInvitationPayload>

    public fun roomInvitationPayload(roomInvitationId: String): Future<RoomInvitationPayload> {

        val future = CompletableFuture<RoomInvitationPayload>()
        val cachePayload = roomInvitationPayloadCache(roomInvitationId)

        if (cachePayload != null) {
            future.complete(cachePayload)
        }

        val rawPayload = roomInvitationRawPayload(roomInvitationId).get()
        val payload = roomInvitationRawPayloadParser(rawPayload).get()
        future.complete(payload)
        return future
    }

    /**
     *
     * Room
     *
     */
    abstract fun roomAdd(roomId: String, contactId: String): Future<Void>
    abstract fun roomAvatar(roomId: String): Future<FileBox>
    abstract fun roomCreate(contactIdList: List<String>, topic: String): Future<String>

    abstract fun roomDel(roomId: String, contactId: String): Future<Void>

    abstract fun roomList(): Future<List<String>>
    abstract fun roomQRCode(roomId: String): Future<String>
    abstract fun roomQuit(roomId: String): Future<Void>
    abstract fun roomTopic(roomId: String): Future<String?>?
    abstract fun roomTopic(roomId: String, topic: String): Future<Void>
    abstract fun roomRawPayload(roomId: String): Future<RoomPayload>
    abstract fun roomRawPayloadParser(roomPayload: RoomPayload): Future<RoomPayload>


    /**
     * RoomMember
     */

    abstract fun getRoomAnnounce(roomId: String): Future<String>
    abstract fun setRoomAnnounce(roomId: String, text: String): Future<Void>
    abstract fun roomMemberList(roomId: String): Future<List<String>>

    public fun roomMemberSearch(roomId: String, query: RoomMemberQueryFilter): Future<List<String>> {
        TODO()
    }

    public fun roomReach(query: RoomQueryFilter?): Future<List<String>> {
        TODO()
    }

    public fun roomValidate(roomId: String): Future<Boolean> {
        return CompletableFuture.completedFuture(true)
    }

    protected fun roomPayloadCache(roomId: String): RoomPayload? {
        return cacheRoomPayload.getIfPresent(roomId)
    }


    fun roomPayload(roomId: String): Future<RoomPayload> {
        return CompletableFuture.supplyAsync {
            return@supplyAsync cacheRoomPayload.get(roomId) { t: String ->
                val get = roomRawPayload(t).get()
                return@get roomRawPayloadParser(get).get()
            }
        }
    }

    public fun roomPayloadDirty(roomId: String): Future<Void> {
        cacheRoomPayload.invalidate(roomId)
        return CompletableFuture.completedFuture(null)
    }


    fun roomSearch(query: RoomQueryFilter): Future<List<String>> {
        return CompletableFuture.supplyAsync {
            val allRoomList = roomList().get()
            var roomPayloads = allRoomList.mapNotNull { x -> roomPayload(x).get() }

            if (StringUtils.isNotBlank(query.id)) {
                roomPayloads = roomPayloads.filter { t ->
                    StringUtils.equals(t.id, query.id)
                }
            }

            if (StringUtils.isNotBlank(query.topic)) {
                roomPayloads = roomPayloads.filter { t ->
                    log.info("t.topic is {} and topic is {}", t.topic, query.topic)
                    val equals = StringUtils.equals(t.topic, query.topic)
                    log.info("equals is {}", equals)
                    equals
                }
                log.info("roomPayloads is {}", roomPayloads)
            }

            if (CollectionUtils.isNotEmpty(roomPayloads)) {
                return@supplyAsync roomPayloads.map { t -> t.id }
            }

            return@supplyAsync (ArrayList<String>());
        }
    }

    /**
     * Concat roomId & contactId to one string
     */

    private fun cacheKeyRoomMember(roomId: String, contactId: String): String {
        return "$contactId@@@$roomId"
    }

    protected abstract fun roomMemberRawPayload(roomId: String, contactId: String): Future<RoomMemberPayload>

    protected abstract fun roomMemberRawPayloadParser(rawPayload: RoomMemberPayload): Future<RoomMemberPayload>

    public fun roomMemberPayloadDirty(roomId: String): Future<Void> {
        val contactIdList = roomMemberList(roomId).get()

        contactIdList.forEach {
            val cacheKey = cacheKeyRoomMember(roomId, it)
            cacheRoomPayload.invalidate(cacheKey)
        }
        return CompletableFuture.completedFuture(null)
    }

    public fun roomMemberPayload(roomId: String, memberId: String): Future<RoomMemberPayload?> {

        val key = cacheKeyRoomMember(roomId, memberId)
        return CompletableFuture.supplyAsync {
            return@supplyAsync cacheRoomMemberPayload.get(key) {
                val rawPayload = roomMemberRawPayload(roomId, memberId).get()
                roomMemberRawPayloadParser(rawPayload).get()
            }
        }
    }

    public fun getEventBus(): EventBus {
        return eb
    }

    companion object {
        private val log = LoggerFactory.getLogger(Puppet::class.java)
    }

}