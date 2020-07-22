package io.github.wechaty.status

import io.github.wechaty.StateEnum
import io.github.wechaty.eventEmitter.Listener
import io.github.wechaty.io.github.wechaty.status.StateSwitchListener
import org.junit.Test
import org.junit.Assert.*;
class StateSwitchTest {

    @Test
    fun testOn() {
        val stateSwitch = StateSwitch()
        assertEquals("default is not on", StateEnum.OFF, stateSwitch.on())

        stateSwitch.on(StateEnum.PENDING)
        assertEquals("should be state pending", StateEnum.PENDING, stateSwitch.on())
        stateSwitch.on(StateEnum.ON)
        assertEquals("should be state true", StateEnum.ON, stateSwitch.on())
        assertEquals("should not off", StateEnum.ON, stateSwitch.off())
        stateSwitch.off(StateEnum.OFF)
        assertEquals("should not on after off()", StateEnum.OFF, stateSwitch.off())

    }

    @Test
    fun testOff() {
        val stateSwitch = StateSwitch()

        assertEquals("default is OFF", StateEnum.OFF, stateSwitch.off())
        stateSwitch.off(StateEnum.PENDING)
        assertEquals("should be state PENDING", StateEnum.PENDING, stateSwitch.off())

        stateSwitch.off(StateEnum.OFF)
        assertEquals("should be state OFF", StateEnum.OFF, stateSwitch.off())
        assertEquals("should not ON", StateEnum.OFF, stateSwitch.on())

        stateSwitch.on(StateEnum.ON)
        assertEquals("should not OFF after on()", StateEnum.ON, stateSwitch.on())
    }

    @Test
    fun testPending() {
        val stateSwitch = StateSwitch()
        assertEquals("default is not PENDING", StateEnum.OFF, stateSwitch.off())
        stateSwitch.on(StateEnum.PENDING)
        assertEquals("should in PENDING state", StateEnum.PENDING, stateSwitch.on())

        stateSwitch.on(StateEnum.ON)
        assertEquals("should not in pending state", StateEnum.ON, stateSwitch.on())

        stateSwitch.off(StateEnum.PENDING)
        assertEquals("should in PENDING state", StateEnum.PENDING, stateSwitch.off())
    }

    @Test
    fun testName() {
        var CLIENT_NAME = "StateSwitchTest"
        val stateSwitch = StateSwitch(CLIENT_NAME)
        assertEquals("should get the same client name as init", CLIENT_NAME, stateSwitch.name)
    }

    @Test
    fun testVersion() {
        val stateSwitch = StateSwitch()
        println(stateSwitch.name)
        assertNotNull("should get version", stateSwitch.version())
    }


    @Test
    fun ready() {
        val stateSwitch = StateSwitch()
        println("刚开始:" + stateSwitch.off())
        stateSwitch.on(StateEnum.PENDING)
        println("调用on(pending):" + stateSwitch.on())
        stateSwitch.ready(StateEnum.ON)
        println("调用ready(on)之后:" + stateSwitch.on())
        stateSwitch.on(StateEnum.ON)
        println("调用on(on):" + stateSwitch.on())

        stateSwitch.off(StateEnum.PENDING)
        println("调用off(pending):" + stateSwitch.off())
        stateSwitch.off(StateEnum.OFF)
        println("调用off(off):" + stateSwitch.off())
    }

    @Test
    fun addEventListener() {
        val stateSwitch = StateSwitch()
        stateSwitch.addEventListener(StateEnum.ON, object : StateSwitchListener {
            override fun handler(state: StateEnum) {
                println(state.name)
            }
        });

        stateSwitch.removeEventListener(StateEnum.OFF, object : StateSwitchListener {
            override fun handler(state: StateEnum) {
                println(state.name)
            }
        })
    }
}
