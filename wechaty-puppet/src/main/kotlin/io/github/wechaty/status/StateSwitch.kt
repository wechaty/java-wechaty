package io.github.wechaty.io.github.wechaty.status

import io.github.wechaty.StateEnum
import io.github.wechaty.eventEmitter.EventEmitter
import io.github.wechaty.eventEmitter.Listener
import io.github.wechaty.io.github.wechaty.schemas.EventEnum
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger

var COUNTER = AtomicInteger()
val nop: () -> Unit = {}
val resolver: () -> Unit = {}
class StateSwitch: EventEmitter(){

    @Volatile
    private var onoff:Boolean = false

    @Volatile
    private var pending:Boolean = false

    private lateinit var onPromise : suspend () -> Unit
    private lateinit var offPromise : suspend () -> Unit

    private lateinit var onResolver : () -> Unit
    private lateinit var offResolver : () -> Unit

    private val name :String = "#${COUNTER.addAndGet(1)}"

    private lateinit var onQueue: ArrayBlockingQueue<() -> Unit>
    private lateinit var offQueue: ArrayBlockingQueue<() -> Unit>

    init {
        log.info("StateSwitch, constructor(%s)", this.name)

        this.onoff = false
        this.pending = false
        onQueue = ArrayBlockingQueue(1)
        offQueue = ArrayBlockingQueue(1)
        this.offQueue.put(resolver)

//        runBlocking {
//            onPromise = {
//                onResolver = nop
//            }
//            onPromise()
//        }
    }

    // 传入on的状态只能是on或者说pending
    fun on(state: StateEnum): String  {

        log.debug("statusSwitch $name on ${state.name} <- ${this.on()}")

        if (state == StateEnum.OFF) {
            throw Exception("the parameter state shouldn't be off")
        }
        onoff = true
        pending = (state == StateEnum.PENDING)

        emit(EventEnum.ON, state.name)

//        if (this.offResolver === nop) {
//            runBlocking {
//                offPromise = {
//                    offResolver = nop
//                }
//                offPromise()
//            }
//        }

        if (this.offQueue.isEmpty()) {
            this.offQueue.put(resolver)
        }

        if (state == StateEnum.ON && this.onQueue.isEmpty()) {
            this.onQueue.put(resolver)
        }
        return this.on()

    }
    // get the current on state
    fun on(): String {
        val on =
            if (this.onoff == true)
                if (this.pending == true)
                    "pending"
                else
                    "true"
            else
                "false"
        log.info("StateSwitch, <%s> on() is %s", this.name, on)
        return on
    }

    fun off(state: StateEnum): String {
        log.info("StateSwitch, <%s> off(%s) <- (%s)", this.name, state, this.off())
        if (state == StateEnum.ON) {
            throw Exception("the parameter state shouldn't be on")
        }

        this.onoff = false
        this.pending = (state == StateEnum.PENDING)
        this.emit(StateEnum.OFF, state)

//        if (this.onResolver === nop) {
//            runBlocking {
//                onPromise = {
//                    onResolver = nop
//                }
//                onPromise()
//            }
//        }
        if (this.onQueue.isEmpty()) {
            this.onQueue.put(resolver)
        }
        if (state == StateEnum.OFF && this.offQueue.isEmpty()) {
            this.offQueue.put(resolver)
        }
        return this.off()
    }
    // get the current off state
    fun off(): String {
        val off =
            if (!this.onoff)
                if (this.pending)
                    "pending"
                else
                    "true"
            else
                "false"
        log.info("StateSwitch, <%s> off() is %s", this.name, off)
        return off
    }

    /**
     * @param state: 准备变为的状态
     * @param cross: 是否变换状态,默认可以
     * 好像可以去掉runblocking
     */
    fun ready(state: StateEnum = StateEnum.ON, cross: Boolean = true) {
        log.info("StateSwitch, <%s> ready(%s, %s)", name, state, cross)

        // 如果准备变换的状态为on
        if (state == StateEnum.ON) {
            // 如果当前状态为off,并且不允许变换状态
            if (onoff == false && cross == false) {
                throw Exception("ready(on) but the state is off. call ready(on, true) to force crossWait")
            }
            // 当前状态为on
            // 或者说当前状态为off, 但是允许变换状态
            // his.onPromise
//            coroutineScope {
//                val job = launch { onPromise }
//                job.join()
//            }
            CoroutineScope(Dispatchers.Default).launch {
                onQueue.take()
                println("on")
            }
        }
        // 如果准备变为off
      else if (state == StateEnum.OFF) {
            // 但是当前状态为 on, 并且不允许变换状态
            if (onoff == true && cross == false) {
                throw Exception("ready(off) but the state is on. call ready(off, true) to force crossWait")
            }
            // 当前状态为off,或者说当前状态为on, 但是允许变换状态
            // 执行状态改变时执行的函数
//            this.offPromise
//            coroutineScope {
//                val job = launch { offPromise }
//                job.join()
//            }
            CoroutineScope(Dispatchers.Default).launch {
                offQueue.take()
            }
        }
        // 错误状态
        else {
            throw Exception("should not go here. ${state} should be of type 'never'")
        }
        log.info("StateSwitch, <%s> ready(%s, %s) resolved.", name, state, cross)
    }

    fun addEventListener(type: StateEnum, listener: Listener) {
        super.addListener(type, listener)
    }

    fun removeEventListener(type: StateEnum, listener: Listener) {
        super.removeListener(type, listener)
    }

    companion object {
        private val log = LoggerFactory.getLogger(StateSwitch::class.java)
    }
}


fun main() {

    val stateSwitch = StateSwitch()
    println("刚开始创建时:" + stateSwitch.on())

    stateSwitch.on(StateEnum.PENDING)
    println("调用on(pending):" + stateSwitch.on())
    stateSwitch.ready(StateEnum.ON)
    println("调用ready(on)之后:" + stateSwitch.on())
    stateSwitch.on(StateEnum.ON)
    println("调用on(on):" + stateSwitch.on())
    // ======================================
//    stateSwitch.off(StateEnum.PENDING)
//    println(stateSwitch.off())
//
//    stateSwitch.ready(StateEnum.OFF)
//    println(stateSwitch.on())
//
//    stateSwitch.off(StateEnum.OFF)
//    println(stateSwitch.on())

}
