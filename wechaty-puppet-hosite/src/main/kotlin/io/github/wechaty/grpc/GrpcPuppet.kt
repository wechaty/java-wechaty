package io.github.wechaty.grpc

import com.google.protobuf.StringValue
import io.github.wechaty.Puppet
import io.github.wechaty.grpc.puppet.*
import io.github.wechaty.io.github.wechaty.Status
import io.github.wechaty.io.github.wechaty.filebox.FileBox
import io.github.wechaty.io.github.wechaty.schemas.*
import io.github.wechaty.io.github.wechaty.utils.JsonUtils
import io.github.wechaty.schemas.*
import io.grpc.ManagedChannel
import io.grpc.stub.StreamObserver
import io.vertx.ext.web.client.WebClient
import io.vertx.grpc.VertxChannelBuilder
import io.vertx.kotlin.core.json.json
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Future


class GrpcPuppet(puppetOptions: PuppetOptions) : Puppet(puppetOptions) {

    private var channel: ManagedChannel? = null;

    private val GRPC_PROT = 8788

    val CHATIE_ENDPOINT = "https://api.chatie.io/v0/hosties/"

    val eb = vertx.eventBus();

    var grpcClient: PuppetGrpc.PuppetBlockingStub? = null
    var grpcAsyncClient: PuppetGrpc.PuppetStub? = null

    val finishLatch = CountDownLatch(1)

    private fun discoverHostieIpa(): Future<String> {

        val completedFuture: CompletableFuture<String> = CompletableFuture()

        val token = puppetOptions!!.token

        val client = WebClient.create(vertx)

        client.getAbs(CHATIE_ENDPOINT + token).send { ar->
            if(ar.succeeded()){

                val result = ar.result()
                val bodyAsJsonObject = result.bodyAsJsonObject()

                val ip = bodyAsJsonObject.getString("ip")

                completedFuture.complete(ip)

            }

            else{
                log.error("get ip error",ar.cause())
                throw Exception("get ip error",ar.cause());
            }
        }

        return completedFuture

    }


    override fun start(): Future<Void> {

        val future = CompletableFuture<Void>()

        if (state == Status.ON) {
            log.warn("start() is called on a ON puppet. await ready(on) and return.")
            state = Status.ON
            return future
        }
        state = Status.PENDING

        state = try {
            startGrpcClient().get()
            startGrpcStream()
            Status.ON
        } catch (e: Exception) {
            log.error("start() rejection:", e)
            Status.OFF
        }

        return CompletableFuture.completedFuture(null)


    }

    override fun stop(): Future<Void> {
        val future = CompletableFuture<Void>()
        log.info("stop()")
        if (state == Status.OFF) {
            log.warn("stop() is called on a OFF puppet. await ready(off) and return.")
            return future
        }

        try {
            if (logonoff()) {
                emit("logout", json {
                    "contactId" to selfId()
                    "data" to "PuppetHostie stop()"
                })

                this.setId(null)
            }

            if (channel != null) {
                try {
                    val stopRequest = Base.StopRequest.newBuilder().build()
                    grpcClient?.stop(stopRequest)
                } catch (e: Exception) {
                    log.error("stop() this.grpcClient.stop() rejection:", e)
                }
            } else {
                log.warn("stop() this.grpcClient not exist")
            }
            stopGrpcClient().get()

        } catch (e: Exception) {
            log.warn("stop() rejection: ", e)
        } finally {
            state = Status.OFF
        }

        return future
    }

    private fun startGrpcClient(): Future<Void> {
        var endPoint = puppetOptions?.endPoint
        if (StringUtils.isEmpty(endPoint)) {
            endPoint = discoverHostieIpa().get()

        }

        if (StringUtils.isEmpty(endPoint) || StringUtils.equals(endPoint, "0.0.0.0")) {
            throw Exception()
        }

        channel = VertxChannelBuilder.forAddress(vertx, endPoint, GRPC_PROT).usePlaintext().build()

        grpcClient = PuppetGrpc.newBlockingStub(channel)
        grpcAsyncClient = PuppetGrpc.newStub(channel)

        return CompletableFuture.completedFuture(null)
    }

    private fun startGrpcStream() {
        val request = Event.EventRequest.newBuilder().build()
        val streamObserver = object : StreamObserver<Event.EventResponse> {
            override fun onNext(event: Event.EventResponse?) {
                if (event != null) {
                    onGrpcStreamEvent(event)
                }
            }

            override fun onError(e: Throwable?) {
                log.error("error", e)
                val reason = "startGrpcStream() eventStream.on(error) ${e?.message}"
                eb.publish("reset", json { "data" to reason }.toString())

            }

            override fun onCompleted() {
                finishLatch.countDown()
            }
        }

        grpcAsyncClient?.event(request, streamObserver)
        val startRequest = Base.StartRequest.newBuilder().build()
        grpcClient?.start(startRequest)
    }


    fun stopGrpcClient(): Future<Void> {
        channel?.shutdown()
        channel = null;
        return CompletableFuture.completedFuture(null)

    }

    override fun end(): Future<Void> {
        TODO("Not yet implemented")
    }

    override fun logout(): Future<Void> {

        if (getId() == null) {
            throw Exception("logout before login?")
        }

        try {
            val request = Base.LogoutRequest.newBuilder().build()
            grpcClient?.logout(request)
        } catch (e: Exception) {
            log.error("logout() rejection: %s", e)
        } finally {
            emit("logout", json {
                "contactId" to getId();
            })
        }
        return CompletableFuture.completedFuture(null)
    }

    override fun ding(data: String?) {
        val request = Base.DingRequest.newBuilder()
                .setData(data)
                .build()

        CompletableFuture.runAsync {
            try {
                grpcClient!!.ding(request)
            } catch (e: Exception) {
                log.error("error", e)
            }
        }
    }

    override fun contactSelfName(name: String): Future<Void> {
        val request = Contact.ContactSelfNameRequest.newBuilder()
                .setName(name)
                .build()
        return CompletableFuture.supplyAsync {
            grpcClient!!.contactSelfName(request)
            null
        }
    }

    override fun contactSelfQRCode(): Future<String> {

        val request = Contact.ContactSelfQRCodeRequest.newBuilder().build()

        return CompletableFuture.supplyAsync {
            val contactSelfQRCode = grpcClient!!.contactSelfQRCode(request)
            contactSelfQRCode.qrcode
        }
    }

    override fun contactSelfSignature(signature: String): Future<Void> {

        val request = Contact.ContactSelfSignatureRequest.newBuilder()
                .setSignature(signature)
                .build()

        return CompletableFuture.supplyAsync {
            grpcClient!!.contactSelfSignature(request)
            null
        }
    }

    override fun tagContactAdd(tagId: String, contactId: String): Future<Void> {
        TODO("Not yet implemented")
    }

    override fun tagContactDelete(tagId: String): Future<Void> {
        TODO("Not yet implemented")
    }

    override fun tagContactList(contactId: String): Future<List<String>> {
        return CompletableFuture.supplyAsync() {
            val request = Contact.ContactListRequest.newBuilder().build()
            val contactList = grpcClient!!.contactList(request)
            contactList.idsList
        }
    }

    override fun tagContactList(): Future<List<String>> {
        TODO("Not yet implemented")
    }

    override fun tagContactRemove(tagId: String?, contactId: String): Future<Void> {
        TODO("Not yet implemented")
    }

    override fun contactAlias(contactId: String): Future<String> {

        return CompletableFuture.supplyAsync {
            val request = Contact.ContactAliasRequest.newBuilder()
                    .setId(contactId)
                    .build()
            val response = grpcClient!!.contactAlias(request)
            val alias = response.alias
            return@supplyAsync alias.value
        }
    }

    override fun contactAlias(contactId: String, alias: String?): Future<Void> {

        val stringValue = StringValue.newBuilder().setValue(alias).build()
        val request = Contact.ContactAliasRequest.newBuilder()
                .setId(contactId)
                .setAlias(stringValue)
                .build()

        return CompletableFuture.supplyAsync() {
            grpcClient!!.contactAlias(request)
            null
        }

    }

    override fun contactAvatar(contactId: String): Future<FileBox> {
        TODO("Not yet implemented")
    }

    override fun contactAvatar(contactId: String, file: FileBox): Future<Void> {
        TODO("Not yet implemented")
    }

    override fun contactList(): Future<List<String>> {

        val request = Contact.ContactListRequest.newBuilder().build()

        return CompletableFuture.supplyAsync {
            val response = grpcClient!!.contactList(request)
            response.idsList
        }
    }

    override fun contactRawPayload(contractId: String): Future<ContactPayload> {

        val request = Contact.ContactPayloadRequest.newBuilder()
                .setId(contractId)
                .build()

        return CompletableFuture.supplyAsync {
            val response = grpcClient!!.contactPayload(request)
            val payload = ContactPayload()
            payload.address = response.address
            payload.alias = response.alias
            payload.avatar = response.avatar
            payload.city = response.city
            payload.friend = response.friend
            payload.gender = ContractGender.getByCode(response.gender.number)
            payload.id = response.id
            payload.name = response.name
            payload.province = response.province
            payload.signature = response.signature
            payload.star = response.star
            payload.type = ContractType.getByCode(response.type.number)
            payload.weixin = response.weixin
            payload
        }

    }

    override fun contactRawPayloadParser(rawPayload: ContactPayload): Future<ContactPayload> {
        return CompletableFuture.completedFuture(rawPayload)
    }

    override fun friendshipAccept(friendshipId: String): Future<Void> {
        TODO("Not yet implemented")
    }

    override fun friendshipAdd(contractId: String, hello: String?): Future<Void> {
        TODO("Not yet implemented")
    }

    override fun friendshipSearchPhone(phone: String): Future<String?> {
        TODO("Not yet implemented")
    }

    override fun friendshipSearchWeixin(weixin: String): Future<String?> {
        TODO("Not yet implemented")
    }

    override fun friendshipRwaPayload(friendshipId: String): Future<FriendshipPayload> {

        val request = Friendship.FriendshipPayloadRequest.newBuilder()
                .setId(friendshipId)
                .build()

        return CompletableFuture.supplyAsync {
            val response = grpcClient!!.friendshipPayload(request)
            val payload = FriendshipPayloadReceive()

            payload.scene = FriendshipSceneType.valueOf(response.scene.name)
            payload.stranger = response.stranger
            payload.ticket = response.ticket
            payload.type = FriendshipType.valueOf(response.type.name)

            payload
        }

    }

    override fun friendshipRawPayloadParser(rawPayload: FriendshipPayload): Future<FriendshipPayload> {
        return CompletableFuture.completedFuture(rawPayload)
    }

    override fun messageContact(messageId: String): Future<String> {
        val request = Message.MessageContactRequest.newBuilder()
                .setId(messageId)
                .build()
        return CompletableFuture.supplyAsync {
            val response = grpcClient!!.messageContact(request)
            response.id
        }
    }

    override fun messageFile(messageId: String): Future<FileBox> {
        TODO("Not yet implemented")
    }

    override fun messageImage(messageId: String): Future<FileBox> {



        TODO("Not yet implemented")
    }

    override fun messageMiniProgram(messageId: String): Future<MiniProgramPayload> {

        val request = Message.MessageMiniProgramRequest.newBuilder()
                .setId(messageId)
                .build()

        return CompletableFuture.supplyAsync {
            val response = grpcClient!!.messageMiniProgram(request)
            val miniProgram = response.miniProgram
            JsonUtils.readValue<MiniProgramPayload>(miniProgram)
        }

    }

    override fun messageUrl(messageId: String): Future<UrlLinkPayload> {

        val request = Message.MessageUrlRequest.newBuilder()
                .setId(messageId)
                .build()

        return CompletableFuture.supplyAsync {

            val response = grpcClient!!.messageUrl(request)
            val urlLink = response.urlLink
            JsonUtils.readValue<UrlLinkPayload>(urlLink)

        }

    }

    override fun messageSendContact(conversationId: String, contactId: String): Future<String?> {

        val request = Message.MessageSendContactRequest.newBuilder()
                .setContactId(contactId)
                .setConversationId(conversationId)
                .build()


        return CompletableFuture.supplyAsync {
            val response = grpcClient!!.messageSendContact(request)
            response.id.value
        }
    }

    override fun messageSendFile(conversationId: String, file: FileBox): Future<String?> {

        val request = Message.MessageSendFileRequest.newBuilder()
                .setConversationId(conversationId)
                .setFilebox(file.toJsonString())
                .build()

        return CompletableFuture.supplyAsync {
            val response = grpcClient!!.messageSendFile(request)
            response.id.value
        }

    }

    override fun messageSendMiniProgram(conversationId: String, miniProgramPayload: MiniProgramPayload): Future<String?> {

        val request = Message.MessageSendMiniProgramRequest.newBuilder()
                .setConversationId(conversationId)
                .setMiniProgram(JsonUtils.write(miniProgramPayload))
                .build()

        return CompletableFuture.supplyAsync {
            val response = grpcClient!!.messageSendMiniProgram(request)
            response.id.value
        }

    }

    override fun messageSendText(conversationId: String, text: String, mentionList: List<String>?): Future<String?> {
        val request = Message.MessageSendTextRequest.newBuilder()
                .setConversationId(conversationId)
                .setText(text)
                .build()

        return CompletableFuture.supplyAsync {
            val response = grpcClient!!.messageSendText(request)
            val stringValue = response.id
            stringValue.value
        }
    }

    override fun messageSendUrl(conversationId: String, urlLinkPayload: UrlLinkPayload): Future<String?> {

        val request = Message.MessageSendUrlRequest.newBuilder()
                .setConversationId(conversationId)
                .setUrlLink(JsonUtils.write(urlLinkPayload))
                .build()

        return CompletableFuture.supplyAsync {
            val response = grpcClient!!.messageSendUrl(request)
            response.id.value
        }

    }


//    override fun messageSendText(
//            conversationId: String,
//            text: String
//            mentionList: List<String>? = null)
//    ): Future<String?> {
//
//        val future = CompletableFuture<String>()
//
//        val request = Message.MessageSendTextRequest.newBuilder()
//                .setConversationId(conversationId)
//                .setText(text)
//                .build()
//
//        val response = grpcClient!!.messageSendText(request)
//
//        val id = response.id
//
//        if (id != null) {
//            future.complete(id.value)
//        }
//
//        return future;
//    }

    override fun messageRecall(messageId: String): Future<Boolean> {
        val request = Message.MessageRecallRequest.newBuilder()
                .setId(messageId)
                .build()

        return CompletableFuture.supplyAsync {
            val response = grpcClient!!.messageRecall(request)
            response.success
        }
    }

    override fun messageRawPayload(messageId: String): Future<MessagePayload> {

        val request = Message.MessagePayloadRequest.newBuilder().setId(messageId).build()

        return CompletableFuture.supplyAsync {
            val response = grpcClient!!.messagePayload(request)
            val payload = MessagePayload(messageId)
            payload.filename = response.filename
            payload.fromId = response.fromId
            payload.text = response.text
            payload.mentionIdList = response.mentionIdsList
            payload.roomId = response.roomId
            payload.timestamp = response.timestamp
            payload.type = MessageType.getByCode(response.type.number)
            payload.toId = response.toId
            payload
        }

    }

    override fun messageRawPayloadParser(rawPayload: MessagePayload): Future<MessagePayload> {
        return CompletableFuture.completedFuture(rawPayload)
    }

    override fun roomInvitationAccept(roomInvitation: String): Future<Void> {

        val request = RoomInvitation.RoomInvitationAcceptRequest.newBuilder()
                .setId(roomInvitation)
                .build()

        return CompletableFuture.supplyAsync {
            grpcClient!!.roomInvitationAccept(request)
            null
        }
    }

    override fun roomInvitationRawPayload(roomInvitationId: String): Future<RoomInvitationPayload> {

        val request = RoomInvitation.RoomInvitationPayloadRequest.newBuilder()
                .setId(roomInvitationId)
                .build()

        return CompletableFuture.supplyAsync {
            val response = grpcClient!!.roomInvitationPayload(request)

            val payload = RoomInvitationPayload()

            payload.avatar = response.avatar
            payload.id = response.id
            payload.invitation = response.invitation
            payload.inviterId = response.inviterId
            payload.memberCount = response.memberCount
            payload.memberIdList = response.memberIdsList
            payload.receiverId = response.receiverId
            payload.timestamp = response.timestamp
            payload.topic = response.topic

            payload
        }

    }

    override fun roomInvitationRawPayloadParser(rawPayload: RoomInvitationPayload): Future<RoomInvitationPayload> {
        return CompletableFuture.completedFuture(rawPayload)
    }

    override fun roomAdd(roomId: String, contactId: String): Future<Void> {
        TODO("Not yet implemented")
    }

    override fun roomAvatar(roomId: String): Future<FileBox> {
        TODO("Not yet implemented")
    }

    override fun roomCreate(contactIdList: List<String>, topic: String): Future<String> {

        val request = Room.RoomCreateRequest.newBuilder()
                .setTopic(topic)
                .addAllContactIds(contactIdList)
                .build()

        return CompletableFuture.supplyAsync {
            val response = grpcClient!!.roomCreate(request)
            response.id
        }


    }

    override fun roomDel(roomId: String, contactId: String): Future<Void> {

        val request = Room.RoomDelRequest.newBuilder()
                .setId(roomId)
                .setContactId(contactId)
                .build()

        return CompletableFuture.supplyAsync {
            grpcClient!!.roomDel(request)
            null
        }

    }

    override fun roomList(): Future<List<String>> {

        val request = Room.RoomListRequest.newBuilder().build()

        return CompletableFuture.supplyAsync {
            val response = grpcClient!!.roomList(request)
            response.idsList
        }

    }

    override fun roomQRCode(roomId: String): Future<String> {
        TODO("Not yet implemented")
    }

    override fun roomQuit(roomId: String): Future<Void> {
        TODO("Not yet implemented")
    }

    override fun roomTopic(roomId: String): Future<String?>? {

        val request = Room.RoomTopicRequest.newBuilder()
                .setId(roomId)
                .build()

        return CompletableFuture.supplyAsync {
            val response = grpcClient!!.roomTopic(request)
            response.topic.value
        }


    }

    override fun roomTopic(roomId: String, topic: String): Future<Void> {

        val value = StringValue.newBuilder().setValue(topic)

        val request = Room.RoomTopicRequest.newBuilder()
                .setId(roomId)
                .setTopic(value)
                .build()

        return CompletableFuture.supplyAsync {
            grpcClient!!.roomTopic(request)
            null
        }
    }

    override fun roomRawPayload(roomId: String): Future<RoomPayload> {

        val request = Room.RoomPayloadRequest.newBuilder()
                .setId(roomId)
                .build()

        return CompletableFuture.supplyAsync {
            val response = grpcClient!!.roomPayload(request)
            val payload = RoomPayload()

            payload.adminIdList = response.adminIdsList
            payload.avatar = response.avatar
            payload.id = response.id
            payload.memberIdList = response.memberIdsList
            payload.ownerId = response.ownerId
            payload.topic = response.topic
            payload
        }

    }

    override fun roomRawPayloadParser(roomPayload: RoomPayload): Future<RoomPayload> {
        return CompletableFuture.completedFuture(roomPayload)
    }

    override fun roomAnnounce(roomId: String): Future<String> {
        TODO("Not yet implemented")
    }

    override fun roomAnnounce(roomId: String, text: String): Future<Void> {
        TODO("Not yet implemented")
    }

    override fun roomMemberList(roomId: String): Future<List<String>> {

        val request = RoomMember.RoomMemberListRequest.newBuilder()
                .setId(roomId)
                .build()

        return CompletableFuture.supplyAsync {
            val response = grpcClient!!.roomMemberList(request)
            response.memberIdsList
        }


    }

    override fun roomMemberRawPayload(roomId: String, contactId: String): Future<RoomMemberPayload> {

        val request = RoomMember.RoomMemberPayloadRequest.newBuilder()
                .setId(roomId)
                .setMemberId(contactId)
                .build()

        return CompletableFuture.supplyAsync {
            val response = grpcClient!!.roomMemberPayload(request)
            val payload = RoomMemberPayload()

            payload.avatar = response.avatar
            payload.id = response.id
            payload.inviterId = response.inviterId
            payload.name = response.name
            payload.roomAlias = response.roomAlias
            payload
        }

    }

    override fun roomMemberRawPayloadParser(rawPayload: RoomMemberPayload): Future<RoomMemberPayload> {
        return CompletableFuture.completedFuture(rawPayload)
    }

    private fun onGrpcStreamEvent(event: Event.EventResponse) {

        try {

            val type = event.type
            val payload = event.payload

            log.info("PuppetHostie $type payload $payload")
//            println("PuppetHostie $type payload $payload")

            if (type == Event.EventType.EVENT_TYPE_HEARTBEAT) {
                eb.publish("heartbeat", json {
                    "data" to "onGrpcStreamEvent"
                })
            }

            when (type) {
                Event.EventType.EVENT_TYPE_DONG -> eb.publish("dong", JsonUtils.readValue<EventDongPayload>(payload))

                Event.EventType.EVENT_TYPE_LOGIN -> {
                    val loginPayload = JsonUtils.readValue<EventLoginPayload>(payload)
                    setId(loginPayload.contactId)
                    eb.publish("login", loginPayload)
                }

                Event.EventType.EVENT_TYPE_HEARTBEAT ->{
                    val heartbeatPayload = JsonUtils.readValue<EventHeartbeatPayload>(payload)
                    eb.publish("heartbeat",heartbeatPayload)
                }

                Event.EventType.EVENT_TYPE_SCAN -> {
                    val eventScanPayload = JsonUtils.readValue<EventScanPayload>(payload)
                    log.info("scan pay load is {}", eventScanPayload)
                    eb.publish("scan", eventScanPayload)
                }

                Event.EventType.EVENT_TYPE_MESSAGE -> {
                    val eventMessagePayload = JsonUtils.readValue<EventMessagePayload>(payload)
                    eb.publish("message", eventMessagePayload)
                }

                else -> {
                    log.info("PuppetHostie $type payload $payload")
                }


            }
        } catch (e: Exception) {
            log.error("error", e)
        }

    }
    companion object{
        private val log = LoggerFactory.getLogger(GrpcPuppet::class.java)
    }

}

