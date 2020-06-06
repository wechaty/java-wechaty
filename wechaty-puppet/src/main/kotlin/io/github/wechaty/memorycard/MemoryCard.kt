//package io.github.wechaty.memorycard
//
//import org.slf4j.LoggerFactory
//import java.util.concurrent.Future
//
//class MemoryCard{
//
//    private var name:String? = null
//    protected var parent:MemoryCard? = null
//    protected var payload:MemoryCardPayload ? = null
//    protected var storage:StorageBackend? = null
//    protected val multiplexNameList = mutableListOf<String>()
//
//    private var options:MemoryCardOptions
//
//    constructor(name: String?=null,options:MemoryCardOptions? = null){
//
//        val _optiones:MemoryCardOptions = options ?: MemoryCardOptions()
//
//        if(name != null){
//            if(options != null) {
//                _optiones.name = name
//            }
//        }
//        this.options = _optiones
//        this.name = _optiones.name
//
//        (_optiones.multiplex != null).let {
//            this.parent = _optiones.multiplex!!.parent
//            this.payload = this.parent!!.payload
//            this.multiplexNameList.addAll(parent!!.multiplexNameList)
//            this.multiplexNameList.add(_optiones.multiplex!!.name)
//            this.storage = null
//        }
//
//        (_optiones.multiplex == null).let {
//            this.payload = null
//            this.multiplexNameList.clear()
//        }
//    }
//
//    private fun getStore():StorageBackend?{
//        log.debug("getStorage() for storage type: %s'",this.options)
//
//        return StorageBackend.getStorage(
//            this.options.name!!,
//            this.options.storageOptions
//        )
//    }
//
//    fun load(){
//        this.payload = this.storage!!.load()
//    }
//
//    fun save(){
//        this.storage!!.save(this.payload!!)
//    }
//
//    companion object{
//        private val log = LoggerFactory.getLogger(MemoryCard::class.java)
//
//        fun multiplex(memory:MemoryCard,name:String):MemoryCard{
//            return MemoryCard()
//        }
//    }
//
//
//}
//
//class MemoryCardPayload{
//    val map:Map<String,Any> = HashMap()
//}
//
//data class Multiplex(
//    val parent:MemoryCard,
//    val name:String
//)
//
//class MemoryCardOptions{
//
//    var name:String? = null
//    var storageOptions:StorageBackendOptions? = null
//    var multiplex:Multiplex? = null
//
//}
//
//
//
//
