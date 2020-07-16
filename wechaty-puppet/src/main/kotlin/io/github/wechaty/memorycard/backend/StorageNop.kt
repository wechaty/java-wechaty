package io.github.wechaty.memorycard.backend

import io.github.wechaty.memorycard.*
import io.github.wechaty.utils.JsonUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.io.File

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
    var storageNop = StorageNop("test", StorageNopOptions())
    println(storageNop)
}
