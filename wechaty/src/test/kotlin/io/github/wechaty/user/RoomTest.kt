package io.github.wechaty.user

import io.github.wechaty.MockPuppet
import io.github.wechaty.Puppet
import io.github.wechaty.Wechaty
import io.github.wechaty.WechatyOptions
import io.github.wechaty.schemas.PuppetOptions
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.spy

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
        wechatyOptions.puppet = "io.github.wechaty.MockPuppet"
        wechatyOptions.puppetOptions = PuppetOptions()
        wechaty = Wechaty.instance(wechatyOptions)
        wechaty.start()

        room = wechaty.roomManager.load(EXPECTED_ROOM_ID)
        room.sync()
    }

    @After
    fun tearDown() {
        wechaty.stop()
    }

    @Test
    fun sayStringWithMentionList() {
        val contact1 = wechaty.contactManager.load(EXPECTED_CONTACT_1_ID)
        val contact2 = wechaty.contactManager.load(EXPECTED_CONTACT_2_ID)
        contact1.sync()
        contact2.sync()

        val spyRoom: Room = spy(room)
        `when`(spyRoom.alias(contact1)).thenReturn("test-contact1-alias")
        `when`(spyRoom.alias(contact2)).thenReturn("test-contact2-alias")

        val text = "test-text"
        val resMsg = spyRoom.say(text, listOf(contact1, contact2)).get()

        Assert.assertEquals((resMsg as Message).id, "mock-msg-$EXPECTED_ROOM_ID")
    }
}
