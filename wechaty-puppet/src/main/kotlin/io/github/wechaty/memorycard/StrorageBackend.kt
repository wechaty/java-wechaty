//package io.github.wechaty.memorycard
//
//import org.slf4j.LoggerFactory
//import kotlin.reflect.full.createInstance
//import kotlin.reflect.full.primaryConstructor
//
//abstract class StorageBackend(name: String, option: StorageBackendOptions) {
//
//    init {
//        log.debug("constructor({}, { type: {} })",name,option)
//    }
//
//    abstract fun save(payload: MemoryCardPayload)
//    abstract fun load():MemoryCardPayload
//    abstract fun destory()
//
//    companion object{
//        private val log = LoggerFactory.getLogger(StorageBackend::class.java)
//
//        fun getStorage(name: String,options: StorageBackendOptions?):StorageBackend{
//
//            var _options = options
//
//            if(options == null) {
//                _options = StorageFileOptions()
//                _options.type = "file"
//            }
//
//            if(_options?.type == null || _options.type !in BACKEND_DICT.keys){
//                throw Exception("backed unknown : ${_options?.type}")
//            }
//
//
//        }
//
//    }
//
//}
//
