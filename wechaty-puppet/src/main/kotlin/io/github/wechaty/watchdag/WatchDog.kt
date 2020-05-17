package io.github.wechaty.io.github.wechaty.watchdag

import io.github.wechaty.eventEmitter.EventEmitter
import io.github.wechaty.eventEmitter.Listener
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit


class WatchDog(var defaultTimeOut:Long =  60*1000,val name:String = "Bark"):EventEmitter(){

    @Volatile
    private var lastFeed :Long = 0
    private var lastFood: WatchdogFood? = null
    private var timeOut = defaultTimeOut
    private val executorService = Executors.newSingleThreadScheduledExecutor()
    private var schedule:ScheduledFuture<*>? = null

    @Volatile
    private var timeId: Long = 0;



    init{
        startTimer(defaultTimeOut)
    }

    fun on(event:String,listener:WatchdogListener){
        super.on(event,object:Listener{
            override fun handler(vararg any: Any) {
                val watchdogFood = WatchdogFood(timeOut)
                log.info("sent reset message")
                feed(watchdogFood)
                listener.handler(watchdogFood)

            }

        })
    }

    private fun startTimer(timeout:Long){
        var localTimeout = timeout;
        if(localTimeout == 0L){
            localTimeout = defaultTimeOut;
        }
        schedule = executorService.schedule({
            val watchdogFood = WatchdogFood(timeout)
            log.info("sent reset message")
            emit("reset", watchdogFood)
        }, localTimeout, TimeUnit.MILLISECONDS)

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

        schedule?.cancel(true)

        this.lastFeed = System.currentTimeMillis();
        this.lastFood = food
        this.timeOut = food.timeout

        log.info("lastFeed is ${this.lastFeed}")

        startTimer(food.timeout)


        return left()
    }

    fun stopTimer(){
        schedule?.cancel(true)
    }

    fun sleep(){
        stopTimer()
    }

    companion object{

        private val log = LoggerFactory.getLogger(WatchdogFood::class.java)

    }




}

interface WatchdogListener {
    fun handler(event: WatchdogFood)
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

