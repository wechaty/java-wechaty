package io.github.wechaty.memorycard

import io.github.wechaty.memorycard.backend.StorageFile
import io.github.wechaty.utils.JsonUtils
import org.slf4j.LoggerFactory

abstract class StorageBackend(name: String, option: StorageBackendOptions) {

    init {
        log.debug("constructor({}, { type: {} })", name, option)
    }

    abstract fun save(payload: MemoryCardPayload)
    abstract fun load():MemoryCardPayload
    abstract fun destory()

    companion object{
        private val log = LoggerFactory.getLogger(StorageBackend::class.java)

        fun getStorage(name: String, options: StorageBackendOptions?): StorageBackend {
            log.info("getStorage', name: {}, options: {}", name, options?.let { JsonUtils.write(it) })

            var _options = options

            // 如果没有传option参数,默认用file后端
            if(options == null) {
                _options = StorageFileOptions()
                _options.type = "file"
            }

            if(_options?.type == null || _options.type!! !in BACKEND_DICT.keys) {
                throw Exception("backed unknown : ${_options?.type}")
            }
            return StorageFile(name, _options)
        }
    }

}

