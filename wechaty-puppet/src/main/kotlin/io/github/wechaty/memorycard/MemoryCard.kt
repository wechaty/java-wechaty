package io.github.wechaty.memorycard

import org.slf4j.LoggerFactory
import java.util.concurrent.Future

class MemoryCard{

    public var name:String? = null
    protected var parent:MemoryCard? = null
    protected var payload:MemoryCardPayload ? = null
    protected var storage:StorageBackend? = null
    protected val multiplexNameList = mutableListOf<String>()

    private var options:MemoryCardOptions

    constructor(name: String?,options:MemoryCardOptions){
        if(name != null){
            options.name = name
        }
        this.options = options
        this.name = options.name

        (options.multiplex != null).let {
            this.parent = options.multiplex!!.parent
            this.payload = this.parent!!.payload
            this.multiplexNameList.addAll(parent!!.multiplexNameList)
            this.multiplexNameList.add(options.multiplex!!.name)
            this.storage = null
        }

        (options.multiplex == null).let {
            this.payload = null
            this.multiplexNameList.clear()


        }
    }

    private fun getStore():StorageBackend?{
        log.debug("getStorage() for storage type: %s'",this.options)
        TODO()
    }

    companion object{
        private val log = LoggerFactory.getLogger(MemoryCard::class.java)
    }


}

class MemoryCardPayload{
    val map:Map<String,Any> = HashMap()
}

data class Multiplex(
    val parent:MemoryCard,
    val name:String
)

class MemoryCardOptions{

    var name:String? = null
    var storageOptions:StorageBackendOptions? = null
    var multiplex:Multiplex? = null

}




