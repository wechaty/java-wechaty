package io.github.wechaty.io.github.wechaty.status

import io.github.wechaty.StateEnum
import io.github.wechaty.eventEmitter.EventEmitter
import io.github.wechaty.io.github.wechaty.schemas.EventEnum
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger

val COUNTER=AtomicInteger()

class StateSwitch:EventEmitter(){


    @Volatile
    private var onoff:Boolean = false

    @Volatile
    private var pending:Boolean = false


    private lateinit var onPromise : Future<Void>
    private lateinit var offPromise : Future<Void>

    private lateinit var onResolver : Function<Void>
    private lateinit var offResolver : Function<Void>


    private val name :String = "#${COUNTER.addAndGet(1)}"

    init {
        offPromise = CompletableFuture.completedFuture(null);
        onPromise = CompletableFuture.runAsync{
            TODO()
        }
    }

    fun on(state: StateEnum):StateEnum{
        val on = on()
        log.debug("statusSwitch $name on ${state.name} <- $on")

        onoff = true
        pending = (state == StateEnum.PENDING)

        emit(EventEnum.ON,state.name)
        TODO()
    }

    fun on():StateEnum{
        TODO()
    }

    companion object{
        private val log = LoggerFactory.getLogger(StateSwitch::class.java)
    }


}

