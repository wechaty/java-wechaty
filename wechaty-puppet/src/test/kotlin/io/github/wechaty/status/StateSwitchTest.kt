package io.github.wechaty.status

import io.github.wechaty.StateEnum
import io.github.wechaty.eventEmitter.Listener
import org.junit.Test
import org.junit.Assert.*;
class StateSwitchTest {

    @Test
    fun testOn() {
        val stateSwitch = StateSwitch()
        assertEquals("default is not on", "false", stateSwitch.on())

        stateSwitch.on(StateEnum.PENDING)
        assertEquals("should be state pending", "pending", stateSwitch.on())
        stateSwitch.on(StateEnum.ON)
        assertEquals("should be state true", "true", stateSwitch.on())
        assertEquals("should not off", "false", stateSwitch.off())
        stateSwitch.off(StateEnum.OFF)
        assertEquals("should not on after off()", "false", stateSwitch.on())

    }

    @Test
    fun testOff() {
        val stateSwitch = StateSwitch()
        stateSwitch.off(StateEnum.PENDING)
        println(stateSwitch.off())

        stateSwitch.ready(StateEnum.OFF)
        println(stateSwitch.on())

        stateSwitch.off(StateEnum.OFF)
        println(stateSwitch.on())
    }

    @Test
    fun name() {

    }
    @Test
    fun ready() {
        val stateSwitch = StateSwitch()
        stateSwitch.on(StateEnum.PENDING)
        println("调用on(pending):" + stateSwitch.on())
        println("调用on(pending):" + stateSwitch.off())
        stateSwitch.ready(StateEnum.ON)
        println("调用ready(on)之后:" + stateSwitch.on())
        stateSwitch.on(StateEnum.ON)
        println("调用on(on):" + stateSwitch.on())
    }

    @Test
    fun add() {
        val stateSwitch = StateSwitch()
    }
}
