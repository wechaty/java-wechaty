package io.github.wechaty.memorycard.backend

import io.github.wechaty.memorycard.*
import org.slf4j.LoggerFactory

class StorageNop(val name: String, var options: StorageBackendOptions) : StorageBackend(name,options) {

    init {
        log.info("StorageNop, constructor(%s, ...)", name)
        options.type = "nop"
        options = options as StorageNopOptions
    }

    override fun load(): MemoryCardPayload {
        log.info("StorageNop, load()")
        return MemoryCardPayload()
    }

    override fun save(payload: MemoryCardPayload) {
        log.info("StorageNop, save()")
    }

    override fun destory() {
        log.info("StorageNop, destroy()")
    }

    override fun toString(): String {
        return "${this.name} <nop>"
    }

    companion object {
        private val log = LoggerFactory.getLogger(StorageNop::class.java)
    }
}



fun main() {
    val storageNop = StorageNop("test", StorageNopOptions())
    println(storageNop)
}
