package io.github.wechaty.user

import io.github.wechaty.MockPuppet
import io.github.wechaty.Puppet
import io.github.wechaty.Wechaty
import io.github.wechaty.WechatyOptions
import io.github.wechaty.schemas.PuppetOptions
import io.github.wechaty.user.manager.MessageManager
import io.github.wechaty.utils.MockitoHelper
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.Spy
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

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

        val spyWechaty: Wechaty = spy(wechaty)
        val spyRoom:Room = spy(room)
        val spyPuppet:Puppet = spy(puppet)
        `when`(spyRoom.alias(contact1)).thenReturn("test-contact1-alias")
        `when`(spyRoom.alias(contact2)).thenReturn("test-contact2-alias")

        val msgId = "test_msgId"
        `when`(spyWechaty.getPuppet()).thenReturn(spyPuppet)
        `when`(spyPuppet.messageSendText("test-conversation-id"
            , "contact1 contact2"
            , listOf("test-mentioned"))).thenReturn(CompletableFuture.completedFuture(msgId))
        val msg = mock(Message::class.java)
        val spyMessageManager:MessageManager = spy(wechaty.messageManager)
        `when`(spyMessageManager.load(msgId)).thenReturn(msg)

        val text = "test-text"
        spyRoom.say(text, listOf(contact1, contact2))

    }
}
