package io.github.wechaty.memorycard

import io.github.wechaty.utils.JsonUtils
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import javax.xml.bind.JAXBElement
import kotlin.math.abs

const val NAMESPACE_MULTIPLEX_SEPRATOR = "\r"
const val NAMESPACE_KEY_SEPRATOR       = "\n"

val NAMESPACE_MULTIPLEX_SEPRATOR_REGEX = Regex(NAMESPACE_MULTIPLEX_SEPRATOR)
val NAMESPACE_KEY_SEPRATOR_REGEX       = Regex(NAMESPACE_KEY_SEPRATOR)

// 名字可以由options传入也可以直接传入
class MemoryCard {

    private var name:String? = null
    protected var parent:MemoryCard? = null
    protected var payload:MemoryCardPayload ? = null
    protected var storage:StorageBackend? = null
    protected val multiplexNameList = mutableListOf<String>()

    // 是否是multiplex,用哪个存储后端,memorycard名字是什么
    private var options:MemoryCardOptions

    // name和options里面的name有可能同时为空
    constructor(name: String? = null, options: MemoryCardOptions? = null) {
        val _options: MemoryCardOptions = options ?: MemoryCardOptions()
        log.info("MemoryCard, constructor({})", JsonUtils.write(_options))

        if(name != null) {
            _options.name = name
        }

        else if (_options.name != null) {
            this.name = _options.name
        }
        else {
            // 如果没有给名字就用一个default作为名字
            this.name = "default"
            _options.name = "default"
        }
        this.options = _options

        if (_options.multiplex != null) {
            this.parent = _options.multiplex!!.parent
            this.payload = this.parent!!.payload

            this.multiplexNameList.addAll(parent!!.multiplexNameList)
            this.multiplexNameList.add(_options.multiplex!!.name)

            this.storage = null
        }
        else {
            this.payload = null
            this.multiplexNameList.clear()
            this.storage = getStore()
        }
    }

    private fun getStore(): StorageBackend? {

        log.debug("getStorage() for storage type: {}",
            if (this.options != null && this.options.storageOptions != null && this.options.storageOptions!!.type != null)
                this.options
            else
                "N/A"
        )

        if (this.options == null) {
            return null
        }

        // 默认得到一个file的后端
        return StorageBackend.getStorage(
            this.options.name!!,
            this.options.storageOptions
        )
    }

    fun load(): Future<Void> {
        log.info("MemoryCard, load() from storage: {}", this.storage ?: "N/A")
        if (this.isMultiplex()) {
            log.info("MemoryCard, load() should not be called on a multiplex MemoryCard. NOOP")
            return CompletableFuture.completedFuture(null)
        }
        if (this.payload != null) {
            throw Exception("memory had already loaded before.")
        }

        if (this.storage != null) {
            this.payload = this.storage!!.load()
        }
        else {
            log.info("MemoryCard, load() no storagebackend")
            this.payload = MemoryCardPayload()
        }
        return CompletableFuture.completedFuture(null)
    }

    fun save(): Future<Void> {
        if (this.isMultiplex()) {
            if (this.parent == null) {
                throw Exception("multiplex memory no parent")
            }
            this.parent!!.save()
        }

        log.info("MemoryCard, <{}>{} save() to {}",this.name ?: "", this.multiplexPath(), this.storage ?: "N/A")
        if (this.payload == null) {
            throw Exception("no payload, please call load() first.")
        }

        if (this.storage == null) {
            log.info("MemoryCard, save() no storage, NOOP")
            return CompletableFuture.completedFuture(null)
        }

        this.storage!!.save(this.payload!!)
        return CompletableFuture.completedFuture(null)
    }

    fun destory(): Future<Void> {
        log.info("MemoryCard, destroy() storage: {}", this.storage ?: "N/A")
        if (this.isMultiplex()) {
            throw Exception("can not destroy on a multiplexed memory")
        }

        // this.clear()

        if (this.storage != null) {
            this.storage!!.destory()
            this.storage = null
        }
        this.payload = null
        return CompletableFuture.completedFuture(null)
    }


    fun size(): Future<Int> {
        log.info("MemoryCard, <{}> size", this.multiplexPath())

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

        // \r + \n
        val namespace = NAMESPACE_MULTIPLEX_SEPRATOR +
                        this.multiplexNameList.joinToString(NAMESPACE_MULTIPLEX_SEPRATOR)
        return namespace
    }
    protected fun isMultiplexKey(key: String): Boolean {

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

    fun  get(name: String): CompletableFuture<Any?>? {
        log.info("MemoryCard, <{}> get({})", this.multiplexPath(), name)
        if (this.payload == null) {
            throw Exception("no payload, please call load() first.")
        }

        val key = this.resolveKey(name)
        return CompletableFuture.supplyAsync {
            this.payload!!.map.get(key)
        }
    }

    fun <T : Any> set(name: String, data: T): Future<Void> {
        log.info("MemoryCard, <{}> set({}, {})", this.multiplexPath(), name, data)

        if (this.payload == null) {
            throw Exception("no payload, please call load() first.")
        }

        val key = this.resolveKey(name)
        this.payload!!.map[key] = data as Any
        return CompletableFuture.completedFuture(null)
    }

    fun clear(): Future<Void> {
        log.info("MemoryCard, <{}> clear()", this.multiplexPath())

        if (this.payload == null) {
            throw Exception("no payload, please call load() first.")
        }
        if (this.isMultiplex()) {
            val keys = this.payload!!.map.keys
            for (key in keys) {
                this.payload!!.map.remove(key)
            }
        }
        else {
            this.payload = MemoryCardPayload()
        }
        return CompletableFuture.completedFuture(null)
    }

    fun delete(name: String): Future<Void> {
        log.info("MemoryCard, <{}> delete({})", this.multiplexPath(), name)
        if (this.payload == null) {
            throw Exception("no payload, please call load() first.")
        }

        val key = this.resolveKey(name)
        this.payload!!.map.remove(key)
        return CompletableFuture.completedFuture(null)
    }

    fun entries(): MutableSet<MutableMap.MutableEntry<String, Any>> {
        log.info("MemoryCard, <{}> *entries()", this.multiplexPath())

        if (this.payload == null) {
            throw Exception("no payload, please call load() first.")
        }

        return this.payload!!.map.entries
    }

    fun has(key: String): Future<Boolean> {
        log.info("MemoryCard, <{}> has ({})", this.multiplexPath(), key)

        if (this.payload == null) {
            throw Exception("no payload, please call load() first.")
        }

        val absoluteKey = this.resolveKey(key)
        return CompletableFuture.supplyAsync {
            this.payload!!.map.containsKey(absoluteKey)
        }
    }

    fun keys(): MutableSet<String> {
        log.info("MemoryCard, <{}> keys()", this.multiplexPath())

        if (this.payload == null) {
            throw Exception("no payload, please call load() first.")
        }
        var result = mutableSetOf<String>()
        for (key in this.payload!!.map.keys) {
            if (this.isMultiplex()) {
                if (this.isMultiplexKey(key)) {
                    val namespace = this.multiplexNamespace()
                    val mpKey = key.substring(namespace.length + 1)
                    result.add(mpKey)
                }
                continue
            }
            result.add(key)
        }
        return result
    }


    fun values(): MutableCollection<Any> {
        log.info("MemoryCard, <{}> values()", this.multiplexPath())

        if (this.payload == null) {
            throw Exception("no payload, please call load() first.")
        }

        return this.payload!!.map.values
    }

    fun multiplex (name: String): MemoryCard {
        log.info("MemoryCard, multiplex({})", name)

        // FIXME: as any ?
        return multiplex(this, name)
    }

    override fun toString(): String {
        var mpString = ""
        if (this.multiplexNameList.size > 0) {
            mpString = this.multiplexNameList
                .map { mpName -> "multiplex(${mpName})" }
                .joinToString("")
        }
        var name = ""
        if (this.options != null && this.options.name != null) {
            name = this.options.name.toString()
        }

        return "MemoryCard<${name}>${mpString}"
    }
    companion object{
        private val log = LoggerFactory.getLogger(MemoryCard::class.java)
        val VERSION = "0.0.0"

        fun multiplex(memory: MemoryCard, name: String): MemoryCard{
            log.info("MemoryCard, static multiplex({}, {})", memory, name)
            memory.options.multiplex = Multiplex(name = name, parent = memory)
            val mpMemory = MemoryCard(options = memory.options)
            return mpMemory
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
    var map = mutableMapOf<String, Any>()
}

data class Multiplex(
    val parent: MemoryCard,
    val name: String
)

data class MemoryCardOptions(
    var name: String? = null,
    var storageOptions:StorageBackendOptions? = null,
    var multiplex:Multiplex? = null
)

data class MemoryCardJsonObject(
    val payload: MemoryCardPayload,
    val options: MemoryCardOptions
)





