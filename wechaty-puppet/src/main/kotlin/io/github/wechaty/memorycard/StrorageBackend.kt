package io.github.wechaty.memorycard

import org.slf4j.LoggerFactory

abstract class StorageBackend(name: String, option: StorageBackendOptions) {

    init {
        log.debug("constructor({}, { type: {} })",name,option)
    }

    abstract fun save(payload: MemoryCardPayload)
    abstract fun load():MemoryCardPayload
    abstract fun destory()

    companion object{
        private val log = LoggerFactory.getLogger(StorageBackend::class.java)
    }

}

