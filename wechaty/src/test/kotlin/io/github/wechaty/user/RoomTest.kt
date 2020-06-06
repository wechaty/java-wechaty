package io.github.wechaty.user

import io.github.wechaty.MockPuppet
import io.github.wechaty.Puppet
import io.github.wechaty.Wechaty
import io.github.wechaty.WechatyOptions
import io.github.wechaty.schemas.PuppetOptions
import io.github.wechaty.utils.MockitoHelper
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*

/**
 * @author renxiaoya
 * @date 2020-05-29
 */
const val EXPECTED_ROOM_ID = "roomId"
const val EXPECTED_ROOM_TOPIC = "test-topic"
const val EXPECTED_CONTACT_1_ID = "contact1"
const val EXPECTED_CONTACT_1_ALIAS = "little1"
const val EXPECTED_CONTACT_2_ID = "contact2"
const val EXPECTED_CONTACT_2_ALIAS = "big2"
class RoomTest {

    lateinit var wechaty: Wechaty

    lateinit var room: Room

    lateinit var puppet: Puppet

    @Before
    fun setUp() {
        puppet = MockPuppet(PuppetOptions())
        val wechatyOptions = WechatyOptions()
        wechatyOptions.name = "MockWechaty"
        wechatyOptions.puppet = "wechaty-puppet-mock"
        wechatyOptions.puppetOptions = PuppetOptions()
        wechaty = Wechaty.instance(wechatyOptions)
//        wechaty = spy(wechaty)
        wechaty.start()

        room = wechaty.roomManager.load(EXPECTED_ROOM_ID)
        room.sync()
    }

    @After
    fun tearDown() {
        wechaty.stop()
    }

    @Test
    // ignore temporary because it doesn't work for now
    @Ignore
    fun say() {
        val contact1 = wechaty.contactManager.load(EXPECTED_CONTACT_1_ID)
        val contact2 = wechaty.contactManager.load(EXPECTED_CONTACT_2_ID)
        contact1.sync()
        contact2.sync()

//        `when`(room.alias(MockitoHelper.anyObject())).thenReturn("test-alias")
//        val msgId = "test_msgId"
//        `when`(wechaty.getPuppet().messageSendText("test-conversation-id"
//            , "test-text"
//            , listOf("test-mentioned")).get()).thenReturn(msgId)
//        val msg = Mockito.mock(Message::class.java)
//        `when`(wechaty.messageManager.load(msgId)).thenReturn(msg)
//
        val text = "test-text"
//        val contact3 = Contact(wechaty, "contact3")


        room.say(text, listOf(contact1, contact2))

    }
}
