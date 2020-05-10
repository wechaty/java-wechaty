package io.github.wechaty.io.github.wechaty.status

import io.github.wechaty.io.github.wechaty.StateEnum
import io.vertx.core.Vertx
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Supplier

val COUNTER=AtomicInteger()

class StateSwitch{


    @Volatile
    private var onoff:Boolean = false

    @Volatile
    private var pending:Boolean = false


    private lateinit var onPromise : Future<Void>
    private lateinit var offPromise : Future<Void>

    private lateinit var onResolver : Function<Void>
    private lateinit var offResolver : Function<Void>


    private val name :String = "#${COUNTER.addAndGet(1)}"

    private val vertx:Vertx = Vertx.vertx()
    private val eb = vertx.eventBus()

    init {
        offPromise = CompletableFuture.completedFuture(null);
        onPromise = CompletableFuture.runAsync{
            onResolver = it
        }
    }

    fun on(state:StateEnum):StateEnum{
        val on = on()
        log.info("statusSwitch $name on ${state.name} <- $on")

        onoff = true
        pending = (state == StateEnum.PENDING)

        eb.publish("on",state.name)

    }

    fun on():StateEnum{

    }

    companion object{
        private val log = LoggerFactory.getLogger(StateSwitch::class.java)
    }


}

