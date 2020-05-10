package io.github.wechaty.io.github.wechaty.watchdag

import io.github.wechaty.io.github.wechaty.schemas.*
import io.github.wechaty.io.github.wechaty.utils.GenericCodec
import io.vertx.core.Handler
import io.vertx.core.Vertx
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread

class WatchDog(var defaultTimeOut:Long =  60*1000,val name:String = "Bark") {

    private val vertx:Vertx = Vertx.vertx()
    private val eb = vertx.eventBus();

    @Volatile
    private var lastFeed :Long = 0
    private var lastFood: WatchdogFood? = null

    private var timeOut = defaultTimeOut

    @Volatile
    private var timeId: Long = 0;


    init{
        startTimer(defaultTimeOut)
        initEventCodec()
    }

    private fun initEventCodec() {
        eb.registerDefaultCodec(WatchdogFood::class.java, GenericCodec(WatchdogFood::class.java))
    }

    fun on(event:String,listener:WatchdogListener){
        val consumer = eb.consumer<WatchdogFood>(event)
        consumer.handler{
            log.info("reset")

            val body = it.body()
            val eventResetPayload = EventResetPayload(body.data.toString())
            val watchdogFood = WatchdogFood(this.timeOut)
            log.info("sent reset message")
            feed(watchdogFood)
            listener.handler(eventResetPayload)
        }

    }

    private fun startTimer(timeout:Long){

        var localTimeout = timeout;

        if(localTimeout == 0L){
            localTimeout = defaultTimeOut;
        }

        this.timeId = vertx.setTimer(localTimeout) {
            val watchdogFood = WatchdogFood(timeout)
            log.info("sent reset message")
            eb.publish("reset",watchdogFood)
        }
    }

    fun left():Long{

        if(lastFeed == 0L){
            return 0
        }

        return lastFeed.plus(timeOut).minus( System.currentTimeMillis() )
    }

    fun feed(food: WatchdogFood):Long{
        log.info("feed dog ${food}")
        if(food.timeout == 0L){
            food.timeout = defaultTimeOut
        }

        vertx.cancelTimer(timeId);

        this.lastFeed = System.currentTimeMillis();
        this.lastFood = food
        this.timeOut = food.timeout

        log.info("lastFeed is ${this.lastFeed}")

        startTimer(food.timeout)


        return left()
    }

    fun stopTimer(){
        vertx.cancelTimer(timeId)
    }

    fun sleep(){
        stopTimer()
    }

    companion object{

        private val log = LoggerFactory.getLogger(WatchdogFood::class.java)

    }




}

interface WatchdogListener {
    fun handler(event:EventResetPayload)
}


data class WatchdogFood(var timeout: Long) {

    var data:Any? = null
    override fun toString(): String {
        return "WatchdogFood(timeout=$timeout, data=$data)"
    }
}

fun main(){
    val watchDog = WatchDog()

    val watchdogFood = WatchdogFood(2000)

    watchDog.feed(watchdogFood)

    println(watchDog.left())

//    watchDog.on("reset"){
//        println("rest")
//    }


    Thread.sleep(1000000)
}

