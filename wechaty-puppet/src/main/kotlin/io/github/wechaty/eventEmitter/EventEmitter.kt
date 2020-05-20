package io.github.wechaty.eventEmitter

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Lists
import com.google.common.collect.Multimaps
import io.github.wechaty.listener.PuppetDongListener
import io.github.wechaty.schemas.EventHeartbeatPayload
import org.apache.commons.collections4.CollectionUtils
import org.slf4j.LoggerFactory
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

open class EventEmitter : EventEmitterInterface {

    private var maxListeners: Int = 0;

    private val executor: Executor

    private val lock = ReentrantLock()

    constructor(executor: Executor) {
        this.executor = executor
    }

    constructor() {
        val i = Runtime.getRuntime().availableProcessors() * 2
        this.executor = Executors.newFixedThreadPool(i)
    }


    private val map = Multimaps.synchronizedListMultimap(ArrayListMultimap.create<String, Listener>())

    override fun addListener(eventName: String, vararg listeners: Listener) {

        listeners.forEach {
            map.put(eventName, it)
        }
    }

    override fun emit(eventName: String, vararg any: Any) {


        var tolist: List<Listener>? = null
        val list = map.get(eventName)
        if (CollectionUtils.isEmpty(list)) {
            log.warn("this eventName:${eventName} has no listener")
            return
        }
        tolist = list.toList()
        tolist.forEach {
            executor.execute {
                it.handler(*any)
            }
        }
    }

    override fun eventNames(): List<String> {
        val keySet = map.keySet()
        return Lists.newArrayList(keySet)
    }

    override fun getMaxListeners(): Int {
        return maxListeners
    }

    override fun listenerCount(eventName: String): Int {
        return map.get(eventName).size
    }

    override fun listeners(eventName: String): List<Listener> {
        return map.get(eventName)
    }

    override fun on(eventName: String, listener: Listener) {
        map.put(eventName, listener)
    }

    /**
     * can not work well on multithreading
     */
    override fun once(eventName: String, listener: Listener) {
        val wrapListener = object : Listener {
            override fun handler(vararg any: Any) {
                removeListener(eventName, this)
                listener.handler(*any)
            }
        }

        map.put(eventName, wrapListener)

    }

    override fun removeAllListeners(eventName: String): Boolean {
        map.removeAll(eventName)
        return true


    }

    override fun removeListener(eventName: String, listener: Listener): Boolean {
        return map.remove(eventName, listener)
    }

    override fun clean() {
        map.clear()
    }

    override fun setMaxListeners(max: Int) {
        maxListeners = max
    }

    override fun len(): Int {
        return map.size()
    }

    companion object {
        private val log = LoggerFactory.getLogger(EventEmitter::class.java)
    }


}

interface EventEmitterInterface {

    // AddListener is an alias for .On(eventName, listener).
    fun addListener(eventName: String, vararg listeners: Listener)

    // Emit fires a particular event,
    // Synchronously calls each of the listeners registered for the event named
    // eventName, in the order they were registered,
    // passing the supplied arguments to each.
    fun emit(eventName: String, vararg any: Any)

    // EventNames returns an array listing the events for which the emitter has registered listeners.
    // The values in the array will be strings.
    fun eventNames(): List<String>

    // GetMaxListeners returns the max listeners for this emitter
    // see SetMaxListeners
    fun getMaxListeners(): Int
    // ListenerCount returns the length of all registered listeners to a particular event

    fun listenerCount(eventName: String): Int

    // Listeners returns a copy of the array of listeners for the event named eventName.
    fun listeners(eventName: String): List<Listener>

    // On registers a particular listener for an event, func receiver parameter(s) is/are optional
    fun on(eventName: String, listener: Listener)

    // Once adds a one time listener function for the event named eventName.
    // The next time eventName is triggered, this listener is removed and then invoked.
    fun once(eventName: String, listener: Listener)

    // RemoveAllListeners removes all listeners, or those of the specified eventName.
    // Note that it will remove the event itself.
    // Returns an indicator if event and listeners were found before the remove.
    fun removeAllListeners(eventName: String): Boolean

    // RemoveListener removes given listener from the event named eventName.
    // Returns an indicator whether listener was removed
    fun removeListener(eventName: String, listener: Listener): Boolean

    // Clear removes all events and all listeners, restores Events to an empty value
    fun clean()

    // SetMaxListeners obviously this function allows the MaxListeners
    // to be decrease or increase. Set to zero for unlimited
    fun setMaxListeners(max: Int)

    // Len returns the length of all registered events
    fun len(): Int

}


interface Listener {

    fun handler(vararg any: Any)
}

fun main() {

    val eventEmitter = EventEmitter()

    eventEmitter.once("test", object : Listener {
        override fun handler(vararg any: Any) {

            val name = any[0]


            val eventHeartbeatPayload = name as EventHeartbeatPayload

            println(eventHeartbeatPayload)


        }

    })

    eventEmitter.emit("test", EventHeartbeatPayload("test"))
    eventEmitter.emit("test", EventHeartbeatPayload("test"))

    Thread.sleep(10000)

}
