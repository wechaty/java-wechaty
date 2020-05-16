package io.github.wechaty.io.github.wechaty.watchdag

import io.github.wechaty.eventEmitter.EventEmitter
import io.github.wechaty.eventEmitter.Listener
import io.github.wechaty.schemas.EventResetPayload
import org.slf4j.LoggerFactory
import java.util.*

class WatchDog(var defaultTimeOut:Long =  60*1000,val name:String = "Bark"):EventEmitter(){

    @Volatile
    private var lastFeed :Long = 0
    private var lastFood: WatchdogFood? = null

    private var timeOut = defaultTimeOut

    private var timer:Timer? =null

    @Volatile
    private var timeId: Long = 0;



    init{
        startTimer(defaultTimeOut)
    }

//    private fun initEventCodec() {
//        eb.registerDefaultCodec(WatchdogFood::class.java, GenericCodec(WatchdogFood::class.java))
//    }

    fun on(event:String,listener:WatchdogListener){
        super.on(event,object:Listener{
            override fun handler(vararg any: Any) {
                val payload = any[0] as EventResetPayload
                val watchdogFood = WatchdogFood(timeOut)
                log.info("sent reset message")
                feed(watchdogFood)
                listener.handler(payload)

            }

        })
    }

    private fun startTimer(timeout:Long){

        var localTimeout = timeout;

        if(localTimeout == 0L){
            localTimeout = defaultTimeOut;
        }


        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                val watchdogFood = WatchdogFood(timeout)
                log.info("sent reset message")
                emit("reset",watchdogFood)
            }

        },localTimeout)
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

        timer?.cancel()

        this.lastFeed = System.currentTimeMillis();
        this.lastFood = food
        this.timeOut = food.timeout

        log.info("lastFeed is ${this.lastFeed}")

        startTimer(food.timeout)


        return left()
    }

    fun stopTimer(){
        timer?.cancel()
    }

    fun sleep(){
        stopTimer()
    }

    companion object{

        private val log = LoggerFactory.getLogger(WatchdogFood::class.java)

    }




}

interface WatchdogListener {
    fun handler(event: EventResetPayload)
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

