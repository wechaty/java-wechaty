package io.github.wechaty.user

import io.github.wechaty.Wechaty
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import io.github.wechaty.utils.MockitoHelper

/**
 * @author renxiaoya
 * @date 2020-05-29
 */
class RoomTest {

    lateinit var mockWechaty: Wechaty

    lateinit var room: Room

    @Before
    fun setUp() {
        //TODO(create a mock puppet for unit test)
        //these can not fill the inner field in wechaty.To resolve it,I may define a mock puppet for unit test
        mockWechaty = Mockito.mock(Wechaty::class.java)
        room = Mockito.mock(Room::class.java)

    }

    @After
    fun tearDown() {

    }

    @Test
    // ignore temporary because it doesn't work for now
    @Ignore
    fun say() {
        `when`(room.alias(MockitoHelper.anyObject())).thenReturn("test-alias")
        val msgId = "test_msgId"
        `when`(mockWechaty.getPuppet().messageSendText(ArgumentMatchers.anyString()
            , ArgumentMatchers.anyString()
            , ArgumentMatchers.anyList()).get()).thenReturn(msgId)
        val msg = Mockito.mock(Message::class.java)
        `when`(mockWechaty.messageManager.load(msgId)).thenReturn(msg)

        val text = "test-text"
        val contact1 = Contact(mockWechaty, "contact1")
        val contact2 = Contact(mockWechaty, "contact2")
        val contact3 = Contact(mockWechaty, "contact3")

        verify(room.say(text, listOf(contact1, contact2, contact3)))

    }
}
