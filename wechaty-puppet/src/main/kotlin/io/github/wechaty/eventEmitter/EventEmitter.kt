package io.github.wechaty.eventEmitter

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Lists
import com.google.common.collect.Multimaps
import io.github.wechaty.schemas.EventHeartbeatPayload
import org.apache.commons.collections4.CollectionUtils
import org.slf4j.LoggerFactory
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock

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


    private val map = Multimaps.synchronizedListMultimap(ArrayListMultimap.create<Event, Listener>())

    override fun addListener(event:Event, vararg listeners: Listener) {
        listeners.forEach {
            map.put(event, it)
        }
    }

    override fun emit(event:Event, vararg any: Any) {
        val tolist: List<Listener>?
        val list = map.get(event)
        if (CollectionUtils.isEmpty(list)) {
            log.debug("this eventName:${event} has no listener")
            return
        }
        tolist = list.toList()
        tolist.forEach {
            executor.execute {
                it.handler(*any)
            }
        }
    }

    override fun eventNames(): List<Event> {
        val keySet = map.keySet()
        return Lists.newArrayList(keySet)
    }

    override fun getMaxListeners(): Int {
        return maxListeners
    }

    override fun listenerCount(event:Event): Int {
        return map.get(event).size
    }

    override fun listeners(event:Event): List<Listener> {
        return map.get(event)
    }

    override fun on(event:Event, listener: Listener) {
        map.put(event, listener)
    }

    /**
     * can not work well on multithreading
     */
    override fun once(event:Event, listener: Listener) {
        val wrapListener = object : Listener {
            override fun handler(vararg any: Any) {
                removeListener(event, this)
                listener.handler(*any)
            }
        }

        map.put(event, wrapListener)

    }

    override fun removeAllListeners(event:Event): Boolean {
        map.removeAll(event)
        return true


    }

    override fun removeListener(event:Event, listener: Listener): Boolean {
        return map.remove(event, listener)
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
    fun addListener(event:Event, vararg listeners: Listener)

    // Emit fires a particular event,
    // Synchronously calls each of the listeners registered for the event named
    // eventName, in the order they were registered,
    // passing the supplied arguments to each.
    fun emit(event:Event, vararg any: Any)

    // EventNames returns an array listing the events for which the emitter has registered listeners.
    // The values in the array will be strings.
    fun eventNames(): List<Event>

    // GetMaxListeners returns the max listeners for this emitter
    // see SetMaxListeners
    fun getMaxListeners(): Int
    // ListenerCount returns the length of all registered listeners to a particular event

    fun listenerCount(event:Event): Int

    // Listeners returns a copy of the array of listeners for the event named eventName.
    fun listeners(event:Event): List<Listener>

    // On registers a particular listener for an event, func receiver parameter(s) is/are optional
    fun on(event:Event, listener: Listener)

    // Once adds a one time listener function for the event named eventName.
    // The next time eventName is triggered, this listener is removed and then invoked.
    fun once(event:Event, listener: Listener)

    // RemoveAllListeners removes all listeners, or those of the specified eventName.
    // Note that it will remove the event itself.
    // Returns an indicator if event and listeners were found before the remove.
    fun removeAllListeners(event:Event): Boolean

    // RemoveListener removes given listener from the event named eventName.
    // Returns an indicator whether listener was removed
    fun removeListener(event:Event, listener: Listener): Boolean

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

interface Event
