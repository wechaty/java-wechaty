package io.github.wechaty.memorycard.backend

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import io.github.wechaty.memorycard.*
import io.github.wechaty.utils.JsonUtils
import org.slf4j.LoggerFactory

class StorageS3(val name: String, var options: StorageBackendOptions) : StorageBackend(name,options) {

    private var s3: AmazonS3

    init {
        log.info("StorageS3, constructor()")
        options.type = "s3"
        options = options as StorageS3Options
        val _options = options as StorageS3Options

        val basicAWSCredentials = BasicAWSCredentials(_options.accessKeyId, _options.secretAccessKey)
        this.s3 = AmazonS3ClientBuilder.standard().withCredentials(AWSStaticCredentialsProvider(basicAWSCredentials))
            .withRegion(_options.region).build()

    }

    override fun save(payload: MemoryCardPayload) {
        log.info("StorageS3, save()")
        val options = this.options as StorageS3Options
        try {
            this.s3.putObject(JsonUtils.write(payload.map), options.bucket, this.name)
        }
        catch (e: Exception) {
            log.error("上传文件:${this.name}错误")
        }
    }

    override fun load(): MemoryCardPayload {
        log.info("StorageS3, load()")

        val options = this.options as StorageS3Options
        val result = try {
            this.s3.getObject(options.bucket, this.name)
        }
        catch (e: Exception) {
            log.error("获取文件:${this.name}错误")
            null
        }

        if (result == null || result.objectContent == null) {
            return MemoryCardPayload()
        }
        val objectContent = result.objectContent

        var payloadMap = StringBuffer()
        var readBuf = ByteArray(1024)
        var readLen = 0
        while (true) {
            readLen = objectContent.read(readBuf)
            if (readLen > 0) {
                payloadMap.append(String(readBuf, 0, readLen))
            }
            else {
                break
            }
        }

        var payload = MemoryCardPayload()
        payload.map = JsonUtils.readValue(payloadMap.toString())
        return payload
    }

    override fun destory() {
        log.info("StorageS3, destory()")
        val options = this.options as StorageS3Options
        try {
            this.s3.deleteObject(options.bucket, this.name)
        }
        catch (e: Exception) {
            log.error("删除${this.name}错误")
        }
    }

    override fun toString(): String {
        return "${this.name}<${this.name}>"
    }

    fun shutdown() {
        log.info("StorageS3, shutdown()")
        this.s3.shutdown()
    }

    companion object {
        private val log = LoggerFactory.getLogger(StorageS3::class.java)
    }

}

fun main(){

//    val storageS3 = StorageS3("test", StorageS3Options("1", "1", "2", "3"))
//    val load = storageS3.load()

}
