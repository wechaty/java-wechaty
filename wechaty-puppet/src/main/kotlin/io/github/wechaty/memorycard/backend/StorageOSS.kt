package io.github.wechaty.memorycard.backend

import com.aliyun.oss.OSSClient
import com.aliyun.oss.OSSClientBuilder
import io.github.wechaty.memorycard.*
import io.github.wechaty.utils.JsonUtils
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

// 使用华为的云存储服务
class StorageOSS(val name: String, var options: StorageBackendOptions) : StorageBackend(name,options) {

    private lateinit var oss: OSSClient

    init {
        log.info("StorageOSS, constructor()")
        options.type = "oss"
        options = options as StorageOSSOptions
        var _options = options as StorageOSSOptions
        this.oss = OSSClientBuilder().build(_options.endPoint,_options.accessKeyId, _options.secretAccessKey) as OSSClient
    }

    override fun save(payload: MemoryCardPayload) {
        log.info("StorageOSS, save()")
        this.putObject(payload)
    }

    override fun load(): MemoryCardPayload {
        log.info("StorageOSS, load()")
        val card = this.getObject()

        log.info("press", card)
        return card
    }

    override fun destory() {
        log.info("StorageOSS, destroy()")
        this.deleteObject()
    }

    override fun toString(): String {
        return "${this.name}<${this.name}>"
    }

    private fun putObject(payload: MemoryCardPayload) {
        val options = this.options as StorageObsOptions
        val putObject = this.oss.putObject(options.bucket, this.name, ByteArrayInputStream(JsonUtils.write(payload.map).toByteArray()))
        // 还需要处理异常
    }

    private fun getObject(): MemoryCardPayload {
        val options = this.options as StorageObsOptions
        val obsObject = this.oss.getObject(options.bucket, this.name)
        println(obsObject)
        val input = obsObject.objectContent
        var byte = ByteArray(1024)
        val bos = ByteArrayOutputStream()
        var len = 0;
        while (true) {
            len = input.read(byte)
            if (len != -1) {
                bos.write(byte, 0, len)
            }
            else {
                break
            }
        }
        input.close()
        var card = MemoryCardPayload()
        card.map = JsonUtils.readValue(String(bos.toByteArray()))
        return card
    }

    private fun deleteObject() {
        val options = this.options as StorageObsOptions
        val deleteObject = this.oss.deleteObject(options.bucket, this.name)
    }

    companion object {
        private val log = LoggerFactory.getLogger(StorageObs::class.java)
    }
}

fun main() {

}
