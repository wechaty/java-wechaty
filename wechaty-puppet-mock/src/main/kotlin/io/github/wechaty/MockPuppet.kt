package io.github.wechaty

import io.github.wechaty.filebox.FileBox
import io.github.wechaty.io.github.wechaty.schemas.EventEnum
import io.github.wechaty.schemas.*
import io.github.wechaty.utils.JsonUtils
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import kotlin.concurrent.scheduleAtFixedRate

class MockPuppet(puppetOptions: PuppetOptions) : Puppet(puppetOptions) {

    private val timer: Timer = Timer()

    override fun start(): Future<Void> {

        log.info("MockPuppet start()")

        //TODO("StateSwitch")
        /*
        if (this.state.on()) {
        log.warn('PuppetMock', 'start() is called on a ON puppet. await ready(on) and return.')
        await this.state.ready('on')
        return
        }

        this.state.on('pending')
        // await some tasks...
        this.state.on(true)
         */
        val eventScanPayload = EventScanPayload(ScanStatus.Cancel)
        eventScanPayload.qrcode = "https://github.com/wechaty/java-wechaty/wechaty-puppet-mock"
        emit(EventEnum.SCAN, eventScanPayload)


        val userPayload = MockData.getFakeContactPayload()
        cacheContactPayload.put(userPayload.id, userPayload)

        setId(userPayload.id)

        emit(EventEnum.LOGIN, EventLoginPayload(userPayload.id))

        timer.scheduleAtFixedRate(0, 5000) {
            val fromContactPayload = MockData.getFakeContactPayload()
            cacheContactPayload.put(fromContactPayload.id, fromContactPayload)
            val messagePayload = MockData.getMessagePayload(fromContactPayload.id, userPayload.id)

            cacheMessagePayload.put(messagePayload.id, messagePayload)
            log.info("MockPuppet start() schedule pretending received a new message:${messagePayload.id}")
            emit(EventEnum.MESSAGE, EventMessagePayload(messagePayload.id))
        }
        return CompletableFuture.completedFuture(null)
    }

    override fun stop(): Future<Void> {
        log.info("MockPuppet stop()")
        //TODO StateSwitch
        /*
        if (this.state.off()) {
          log.warn('PuppetMock', 'stop() is called on a OFF puppet. await ready(off) and return.')
          await this.state.ready('off')
          return
        }

        this.state.off('pending')
         */
        timer.cancel()

        //this.state.off(true)
        return CompletableFuture.completedFuture(null)
    }

    override fun logout(): Future<Void> {
        log.info("MockPuppet logout()")
        val id = getId() ?: throw Exception("logout before login?")

        emit(EventEnum.LOGOUT, EventLogoutPayload(id, "test"))
        setId(null)

        return CompletableFuture.completedFuture(null)
    }

    override fun ding(data: String?) {
        log.info("MockPuppet ding(${data ?: ""})")
        emit(EventEnum.DONG, EventDongPayload(data ?: ""))
    }

    override fun contactSelfName(name: String): Future<Void> {
        log.info("MockPuppet contactSelfName($name)")
        return CompletableFuture.completedFuture(null)
    }

    override fun contactSelfQRCode(): Future<String> {
        log.info("MockPuppet contactSelfQRCode()")
        return CompletableFuture.completedFuture(CHATIE_OFFICIAL_ACCOUNT_QRCODE)
    }

    override fun contactSelfSignature(signature: String): Future<Void> {
        log.info("MockPuppet contactSelfSignature($signature)")
        return CompletableFuture.completedFuture(null)
    }

    override fun tagContactAdd(tagId: String, contactId: String): Future<Void> {
        log.info("MockPuppet tagContactAdd($tagId,$contactId)")
        return CompletableFuture.completedFuture(null)
    }

    override fun tagContactDelete(tagId: String): Future<Void> {
        log.info("MockPuppet tagContactDelete($tagId)")
        return CompletableFuture.completedFuture(null)
    }

    override fun tagContactList(contactId: String): Future<List<String>> {
        log.info("MockPuppet tagContactList($contactId)")
        return CompletableFuture.completedFuture(listOf())
    }

    override fun tagContactList(): Future<List<String>> {
        log.info("MockPuppet tagContactList()")
        return CompletableFuture.completedFuture(listOf())
    }

    override fun tagContactRemove(tagId: String, contactId: String): Future<Void> {
        log.info("MockPuppet tagContactRemove($tagId,$contactId)")
        return CompletableFuture.completedFuture(null)
    }

    override fun contactAlias(contactId: String): Future<String> {
        log.info("MockPuppet contactAlias($contactId)")
        return CompletableFuture.completedFuture("mock alias")
    }

    override fun contactAlias(contactId: String, alias: String?): Future<Void> {
        log.info("MockPuppet getContactAvatar($contactId,$alias)")
        return CompletableFuture.completedFuture(null)
    }

    override fun getContactAvatar(contactId: String): Future<FileBox> {
        log.info("MockPuppet getContactAvatar($contactId)")
        return CompletableFuture.completedFuture(FileBox.fromFile("image/mock.png", "mock.png"))
    }

    override fun setContactAvatar(contactId: String, file: FileBox): Future<Void> {
        return CompletableFuture.completedFuture(null)
    }

    override fun contactList(): Future<List<String>> {
        log.info("MockPuppet contactList()")

        return CompletableFuture.completedFuture(listOf())
    }

    override fun contactRawPayload(contractId: String): Future<ContactPayload> {
        log.info("MockPuppet contactRawPayload($contractId)")
        val contactPayload = ContactPayload(contractId)
        contactPayload.name = "mock name"
        return CompletableFuture.completedFuture(contactPayload)
    }

    override fun contactRawPayloadParser(rawPayload: ContactPayload): Future<ContactPayload> {
        log.info("MockPuppet contactRawPayloadParser($rawPayload)")
        val contactPayload = ContactPayload(rawPayload.id)
        contactPayload.avatar = "mock-avatar-data"
        contactPayload.gender = ContactGender.Unknown
        contactPayload.name = rawPayload.name
        contactPayload.type = ContactType.Unknown
        return CompletableFuture.completedFuture(contactPayload)
    }

    override fun friendshipAccept(friendshipId: String): Future<Void> {
        log.info("MockPuppet friendshipAccept($friendshipId)")

        return CompletableFuture.completedFuture(null)
    }

    override fun friendshipAdd(contractId: String, hello: String): Future<Void> {
        log.info("MockPuppet friendshipAdd($contractId,$hello)")

        return CompletableFuture.completedFuture(null)
    }

    override fun friendshipSearchPhone(phone: String): Future<String?> {
        log.info("MockPuppet friendshipSearchPhone($phone)")

        return CompletableFuture.completedFuture(null)
    }

    override fun friendshipSearchWeixin(weixin: String): Future<String?> {
        log.info("MockPuppet friendshipSearchWeixin($weixin)")

        return CompletableFuture.completedFuture(null)
    }

    override fun friendshipRawPayload(friendshipId: String): Future<FriendshipPayload> {
        val friendshipPayload = FriendshipPayload()
        friendshipPayload.id = friendshipId
        return CompletableFuture.completedFuture(friendshipPayload)
    }

    override fun friendshipRawPayloadParser(rawPayload: FriendshipPayload): Future<FriendshipPayload> {
        return CompletableFuture.completedFuture(rawPayload)
    }

    override fun messageContact(messageId: String): Future<String> {
        log.info("MockPuppet messageContact($messageId)")
        return CompletableFuture.completedFuture("fake-id")
    }

    override fun messageFile(messageId: String): Future<FileBox> {
        return CompletableFuture.completedFuture(FileBox.fromBase64("cRH9qeL3XyVnaXJkppBuH20tf5JlcG9uFX1lL2IvdHRRRS9kMMQxOPLKNYIzQQ==", "mock-file$messageId.txt"))
    }

    override fun messageImage(messageId: String, imageType: ImageType): Future<FileBox> {
        log.info("MockPuppet messageImage($messageId,$imageType)")
        return CompletableFuture.completedFuture(FileBox.fromQRCode("fake-qrcode"))
    }

    override fun messageMiniProgram(messageId: String): Future<MiniProgramPayload> {
        log.info("MockPuppet messageMiniProgram($messageId)")
        val miniProgramPayload = MiniProgramPayload()
        miniProgramPayload.title = "mock title for $messageId"
        return CompletableFuture.completedFuture(miniProgramPayload)
    }

    override fun messageUrl(messageId: String): Future<UrlLinkPayload> {
        log.info("MockPuppet messageUrl($messageId)")
        return CompletableFuture.completedFuture(UrlLinkPayload("mock title for $messageId", "https://mock.url"))
    }

    override fun messageSendContact(conversationId: String, contactId: String): Future<String?> {
        log.info("MockPuppet messageSendContact($conversationId,$contactId)")

        return CompletableFuture.completedFuture(null)
    }

    override fun messageSendFile(conversationId: String, file: FileBox): Future<String?> {
        log.info("MockPuppet messageSendFile($conversationId,$file)")

        return CompletableFuture.completedFuture(null)
    }

    override fun messageSendMiniProgram(conversationId: String, miniProgramPayload: MiniProgramPayload): Future<String?> {
        log.info("MockPuppet messageSendMiniProgram($conversationId,${JsonUtils.write(miniProgramPayload)})")

        return CompletableFuture.completedFuture(null)
    }

    override fun messageSendText(conversationId: String, text: String, mentionList: List<String>?): Future<String?> {
        log.info("MockPuppet messageSendText($conversationId,$text,${JsonUtils.write(mentionList ?: "")})")

        return CompletableFuture.completedFuture(null)
    }

    override fun messageSendUrl(conversationId: String, urlLinkPayload: UrlLinkPayload): Future<String?> {
        log.info("MockPuppet messageSendUrl($conversationId,${JsonUtils.write(urlLinkPayload)})")

        return CompletableFuture.completedFuture(null)
    }

    override fun messageRecall(messageId: String): Future<Boolean> {
        log.info("MockPuppet messageRecall($messageId)")

        return CompletableFuture.completedFuture(false)
    }

    override fun messageRawPayload(messageId: String): Future<MessagePayload> {
        log.info("MockPuppet messageRawPayload($messageId)")
        val messagePayload = MessagePayload(messageId)
        messagePayload.fromId = "from_id"
        messagePayload.text = "mock message text"
        messagePayload.toId = "to_id"
        return CompletableFuture.completedFuture(messagePayload)
    }

    override fun messageRawPayloadParser(rawPayload: MessagePayload): Future<MessagePayload> {
        log.info("MockPuppet messageRawPayloadParser($rawPayload)")
        val messagePayload = MessagePayload(rawPayload.id)
        messagePayload.fromId = rawPayload.fromId
        messagePayload.mentionIdList = listOf()
        messagePayload.text = rawPayload.text
        messagePayload.timestamp = Date().time
        messagePayload.toId = rawPayload.toId
        messagePayload.type = MessageType.Text
        return CompletableFuture.completedFuture(messagePayload)
    }

    override fun roomInvitationAccept(roomInvitation: String): Future<Void> {
        log.info("MockPuppet roomInvitationAccept($roomInvitation)")

        return CompletableFuture.completedFuture(null)
    }

    override fun roomInvitationRawPayload(roomInvitationId: String): Future<RoomInvitationPayload> {
        log.info("MockPuppet roomInvitationRawPayload($roomInvitationId)")

        return CompletableFuture.completedFuture(null)
    }

    override fun roomInvitationRawPayloadParser(rawPayload: RoomInvitationPayload): Future<RoomInvitationPayload> {
        log.info("MockPuppet rromInvitationRawPayloadParser(${JsonUtils.write(rawPayload)})")
        return CompletableFuture.completedFuture(rawPayload)
    }

    override fun roomAdd(roomId: String, contactId: String): Future<Void> {
        log.info("MockPuppet roomAdd($roomId,$contactId)")
        return CompletableFuture.completedFuture(null)
    }

    override fun roomAvatar(roomId: String): Future<FileBox> {
        log.info("MockPuppet roomAvatar($roomId)")

        val roomPayload = this.roomPayload(roomId).get()
        if (roomPayload.avatar != null) {
            return CompletableFuture.completedFuture(FileBox.fromUrl(roomPayload.avatar!!, "room-avatar"))
        }
        log.warn("MockPuppet roomAvatar() avatar not found,use the chatie default.")
        return CompletableFuture.completedFuture(qrCodeForChatie())
    }

    override fun roomCreate(contactIdList: List<String>, topic: String?): Future<String> {
        log.info("MockPuppet roomCreate($contactIdList,$topic)")
        return CompletableFuture.completedFuture("mock_room_id")
    }

    override fun roomDel(roomId: String, contactId: String): Future<Void> {
        log.info("MockPuppet roomDel($roomId,$contactId)")
        return CompletableFuture.completedFuture(null)
    }

    override fun roomList(): Future<List<String>> {
        log.info("MockPuppet roomList()")

        return CompletableFuture.completedFuture(listOf())
    }

    override fun roomQRCode(roomId: String): Future<String> {
        log.info("MockPuppet roomQRCode($roomId)")
        return CompletableFuture.completedFuture("$roomId mock qrcode")
    }

    override fun roomQuit(roomId: String): Future<Void> {
        log.info("MockPuppet roomQuit($roomId)")
        return CompletableFuture.completedFuture(null)
    }

    override fun roomTopic(roomId: String): Future<String?>? {
        log.info("MockPuppet roomTopic($roomId)")
        return CompletableFuture.completedFuture("mock room topic")
    }

    override fun roomTopic(roomId: String, topic: String): Future<Void> {
        log.info("MockPuppet roomTopic($roomId,$topic)")
        return CompletableFuture.completedFuture(null)
    }

    override fun roomRawPayload(roomId: String): Future<RoomPayload> {
        log.info("MockPuppet roomRawPayload($roomId)")
        val roomPayload = RoomPayload(roomId)
        roomPayload.memberIdList = listOf()
        roomPayload.ownerId = "mock_room_owner_id"
        roomPayload.topic = "mock topic"
        return CompletableFuture.completedFuture(roomPayload)
    }

    override fun roomRawPayloadParser(roomPayload: RoomPayload): Future<RoomPayload> {
        log.info("MockPuppet roomRawPayloadParser(${JsonUtils.write(roomPayload)})")
        val payload = RoomPayload(roomPayload.id)
        payload.ownerId = roomPayload.id
        payload.adminIdList = listOf()
        payload.memberIdList = listOf()
        payload.topic = "mock topic"
        return CompletableFuture.completedFuture(payload)
    }

    override fun getRoomAnnounce(roomId: String): Future<String> {
        return CompletableFuture.completedFuture("mock announcement for $roomId")
    }

    override fun setRoomAnnounce(roomId: String, text: String): Future<Void> {
        log.info("MockPuppet setRoomAnnounce($roomId,$text)")
        return CompletableFuture.completedFuture(null)
    }

    override fun roomMemberList(roomId: String): Future<List<String>> {
        log.info("MockPuppet roomMemberList($roomId)")
        return CompletableFuture.completedFuture(listOf())
    }

    override fun roomMemberRawPayload(roomId: String, contactId: String): Future<RoomMemberPayload> {
        log.info("MockPuppet roomMemberRawPayload($roomId,$contactId)")
        val roomMemberPayload = RoomMemberPayload()
        roomMemberPayload.avatar = "mock-avatar-data"
        roomMemberPayload.id = "xx"
        roomMemberPayload.name = "mock-name"
        roomMemberPayload.roomAlias = "yy"

        return CompletableFuture.completedFuture(roomMemberPayload)
    }

    override fun roomMemberRawPayloadParser(rawPayload: RoomMemberPayload): Future<RoomMemberPayload> {
        log.info("MockPuppet roomMemberRawPayloadParser(${JsonUtils.write(rawPayload)})")
        return CompletableFuture.completedFuture(rawPayload)
    }

    companion object {
        private val log = LoggerFactory.getLogger(MockPuppet::class.java)
    }
}
