package io.github.wechaty

import com.github.javafaker.Faker
import io.github.wechaty.filebox.FileBox
import io.github.wechaty.schemas.*
import java.util.*

/**
 * Data for mock
 * @author renxiaoya
 * @date 2020-06-01
 **/
class MockData {

    companion object {
        private val faker = Faker()

        fun getFakeContactPayload(): ContactPayload {
            val contactPayload = ContactPayload(UUID.randomUUID().toString())
            contactPayload.address = faker.address().streetAddress()
            contactPayload.avatar = faker.avatar().toString()
            contactPayload.city = faker.address().city()
            contactPayload.friend = true
            contactPayload.gender = ContractGender.Male
            contactPayload.name = faker.name().firstName()
            contactPayload.province = faker.address().state()
            contactPayload.signature = faker.lorem().sentence()
            contactPayload.star = false
            contactPayload.type = ContactType.Personal
            return contactPayload
        }

        fun getFakeImageFileBox(): FileBox {
            return FileBox.fromUrl(faker.avatar().image(), null)
        }

        fun getMessagePayload(fromId: String, toId: String): MessagePayload {
            val messagePayload = getFakeMessagePayload()
            messagePayload.fromId = fromId
            messagePayload.toId = toId
            return messagePayload
        }

        fun getFakeMessagePayload(): MessagePayload {
            val messagePayload = MessagePayload(UUID.randomUUID().toString())
            messagePayload.fromId = UUID.randomUUID().toString()
            messagePayload.mentionIdList = listOf()
            messagePayload.text = faker.lorem().sentence()
            messagePayload.timestamp = Date().time
            messagePayload.toId = UUID.randomUUID().toString()
            messagePayload.type = MessageType.Text
            messagePayload.roomId = "${UUID.randomUUID().toString()}@chatroom"
            messagePayload.filename = faker.file().fileName()
            return messagePayload
        }
    }

}
