package io.github.wechaty.memorycard.backend

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import io.github.wechaty.memorycard.*
import io.github.wechaty.utils.JsonUtils
import org.slf4j.LoggerFactory

class StorageS3(val name: String, var options: StorageBackendOptions) : StorageBackend(name,options) {

    private lateinit var s3: AmazonS3

    init {
        log.info("StorageS3, constructor()")
        options.type = "s3"
        options = options as StorageS3Options
        var _options = options as StorageS3Options

        val basicAWSCredentials = BasicAWSCredentials(_options.accessKeyId, _options.secretAccessKey)
        this.s3 = AmazonS3ClientBuilder.standard().withCredentials(AWSStaticCredentialsProvider(basicAWSCredentials))
            .withRegion(_options.region).build()

    }

    override fun save(payload: MemoryCardPayload) {
        log.info("StorageS3, save()")
        val options = this.options as StorageS3Options

        this.s3.putObject(JsonUtils.write(payload.map), options.bucket, this.name)

    }

    override fun load(): MemoryCardPayload {
        log.info("StorageS3, load()")

        val options = this.options as StorageS3Options
        val result = this.s3.getObject(options.bucket, this.name)
        if (result == null || result.objectContent == null) {
            return MemoryCardPayload()
        }
        // 这里还有问题
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
        this.s3.deleteObject(options.bucket, this.name)
        this.s3.shutdown()
    }

    override fun toString(): String {
        return "${this.name}<${this.name}>"
    }


    companion object {
        private val log = LoggerFactory.getLogger(StorageS3::class.java)
    }

}

fun main(){

//    val storageS3 = StorageS3("test", StorageS3Options("1", "1", "2", "3"))
//    val load = storageS3.load()

}
