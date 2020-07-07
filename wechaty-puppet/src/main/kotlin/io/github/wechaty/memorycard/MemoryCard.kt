package io.github.wechaty.memorycard

import io.github.wechaty.utils.JsonUtils
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

const val NAMESPACE_MULTIPLEX_SEPRATOR = "\r"
const val NAMESPACE_KEY_SEPRATOR       = "\n"

val NAMESPACE_MULTIPLEX_SEPRATOR_REGEX = Regex(NAMESPACE_MULTIPLEX_SEPRATOR)
val NAMESPACE_KEY_SEPRATOR_REGEX       = Regex(NAMESPACE_KEY_SEPRATOR)

class MemoryCard {

    private var name:String? = null
    protected var parent:MemoryCard? = null
    protected var payload:MemoryCardPayload ? = null
    protected var storage:StorageBackend? = null
    protected val multiplexNameList = mutableListOf<String>()

    private var options:MemoryCardOptions

    constructor(name: String?=null,options:MemoryCardOptions? = null){

        val _optiones:MemoryCardOptions = options ?: MemoryCardOptions()

        if(name != null){
            if(options != null) {
                _optiones.name = name
            }
        }
        this.options = _optiones
        this.name = _optiones.name

        (_optiones.multiplex != null).let {
            this.parent = _optiones.multiplex!!.parent
            this.payload = this.parent!!.payload
            this.multiplexNameList.addAll(parent!!.multiplexNameList)
            this.multiplexNameList.add(_optiones.multiplex!!.name)
            this.storage = null
        }

        (_optiones.multiplex == null).let {
            this.payload = null
            this.multiplexNameList.clear()
        }
    }

    private fun getStore():StorageBackend?{
        log.debug("getStorage() for storage type: %s'",this.options)

        return StorageBackend.getStorage(
            this.options.name!!,
            this.options.storageOptions
        )
    }

    fun load() {
        this.payload = this.storage!!.load()
    }

    fun save() {
        this.storage!!.save(this.payload!!)
    }

    fun destory(): Future<Void> {
        log.info("MemoryCard, destroy() storage: %s", this.storage ?: "N/A")
        if (this.isMultiplex()) {
            throw Exception("can not destroy on a multiplexed memory")
        }

        if (this.payload != null) {
            this.destory()
            this.storage = null
        }
        this.payload = null
        return CompletableFuture.completedFuture(null)
    }


    fun size(): Future<Int> {
        log.info("MemoryCard, <%s> size", this.multiplexPath())
        if (this.payload == null) {
            throw Exception("no payload, please call load() first.")
        }

        var count: Int
        if (this.isMultiplex()) {
            count = this.payload!!.map.keys
                .filter { key -> this.isMultiplexKey(key) }
                .size
        }
        else {
            count = this.payload!!.map.size
        }

        return CompletableFuture.completedFuture(count)
    }


    fun isMultiplex (): Boolean {
        return this.multiplexNameList.size > 0
    }

    protected fun multiplexPath(): String {
        return this.multiplexNameList.joinToString("/")
    }

    protected fun multiplexNamespace(): String {
        if (!this.isMultiplex()) {
            throw Exception("not a multiplex memory")
        }

        val namespace = NAMESPACE_MULTIPLEX_SEPRATOR +
                        this.multiplexNameList.joinToString(NAMESPACE_MULTIPLEX_SEPRATOR)
        return namespace
    }
    protected fun isMultiplexKey (key: String): Boolean {

        if (NAMESPACE_MULTIPLEX_SEPRATOR_REGEX.matches(key)
            && NAMESPACE_KEY_SEPRATOR_REGEX.matches(key)) {

            val namespace = this.multiplexNamespace()
            return key.startsWith(namespace)
        }
        return false
    }

    protected fun resolveKey (name: String): String {

        if (this.isMultiplex()) {
            val namespace = this.multiplexNamespace()
            return namespace + NAMESPACE_KEY_SEPRATOR + name
        }
        else {
            return name
        }
    }
    companion object{
        private val log = LoggerFactory.getLogger(MemoryCard::class.java)
        val VERSION = "0.0.0"
        fun multiplex(memory:MemoryCard,name:String):MemoryCard{
            return MemoryCard()
        }

        fun fromJSON(text: String): MemoryCard {
            log.info("MemoryCard, fromJSON(...)")
            var jsonObj: MemoryCardJsonObject
            jsonObj = JsonUtils.readValue(text)
            val card = MemoryCard(jsonObj.options.name, jsonObj.options)
            card.payload = jsonObj.payload
            return MemoryCard()
        }

        fun fromJSON(obj: MemoryCardJsonObject): MemoryCard {
            log.info("MemoryCard, fromJSON(...)")
            val card = MemoryCard(obj.options.name, obj.options)
            card.payload = obj.payload

            return card
        }
    }


}

class MemoryCardPayload{
    val map: Map<String,Any> = HashMap()
}

data class Multiplex(
    val parent: MemoryCard,
    val name: String
)

class MemoryCardOptions{

    var name:String? = null
    var storageOptions:StorageBackendOptions? = null
    var multiplex:Multiplex? = null
}

data class MemoryCardJsonObject(
    val payload: MemoryCardPayload,
    val options: MemoryCardOptions
)




