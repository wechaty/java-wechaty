package io.github.wechaty.memorycard.backend

import com.aliyun.oss.OSSClient
import com.aliyun.oss.OSSClientBuilder
import com.aliyun.oss.model.PutObjectRequest
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

    private fun putObject(payload: MemoryCardPayload) {
        val options = this.options as StorageOSSOptions

        val putObjectRequest = PutObjectRequest(options.bucket, this.name, ByteArrayInputStream(JsonUtils.write(payload.map).toByteArray()))
        try {
            this.oss.putObject(putObjectRequest)
        }
        catch (e: Exception) {
            log.error("上传${this.name}错误")
        }
    }

    private fun getObject(): MemoryCardPayload {
        val options = this.options as StorageOSSOptions
        val ossObject = this.oss.getObject(options.bucket, this.name)

        val input = ossObject.objectContent
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
        ossObject.close()
        var card = MemoryCardPayload()
        card.map = JsonUtils.readValue(String(bos.toByteArray()))
        return card
    }

    private fun deleteObject() {
        val options = this.options as StorageOSSOptions
        try {
            this.oss.deleteObject(options.bucket, this.name)
        }
        catch (e: Exception) {
            log.error("删除${this.name}错误")
        }
    }

    fun shutdown() {
        log.info("StorageOSS, shutdown()")
        this.oss.shutdown()
    }

    override fun toString(): String {
        return "${this.name}<${this.name}>"
    }

    companion object {
        private val log = LoggerFactory.getLogger(StorageObs::class.java)
    }
}

fun main() {
    val storageOSSOptions = StorageOSSOptions("LTAI4G2iSKd5m8aTZprdj133", "UT9ns4KvNdZkdMIIPEU87upQv2fsBp",
        "oss-cn-beijing.aliyuncs.com", "cybersa")
    val storageOSS = StorageOSS("objectkey", storageOSSOptions)
    val load = storageOSS.load()
//    load.map.put("a", "b")
//    storageOSS.save(load)
}
