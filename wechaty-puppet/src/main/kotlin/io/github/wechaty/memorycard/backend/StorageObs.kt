package io.github.wechaty.memorycard.backend

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.obs.services.ObsClient
import io.github.wechaty.memorycard.*
import io.github.wechaty.utils.JsonUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

// 使用华为的云存储服务
class StorageObs(val name: String, var options: StorageBackendOptions) : StorageBackend(name,options) {

    private var obs: ObsClient

    init {
        log.info("StorageObs, constructor()")
        options.type = "obs"
        options = options as StorageObsOptions
        var _options = options as StorageObsOptions
        this.obs = ObsClient(_options.accessKeyId, _options.secretAccessKey, _options.server)
    }

    override fun save(payload: MemoryCardPayload) {
        log.info("StorageObs, save()")
        this.putObject(payload)
    }

    override fun load(): MemoryCardPayload {
        log.info("StorageObs, load()")
        val card = this.getObject()
        log.info("press", card)
        return card
    }

    override fun destory() {
        log.info("StorageObs, destroy()")
        this.deleteObject()
    }

    private fun putObject(payload: MemoryCardPayload) {
        val options = this.options as StorageObsOptions
        val putObject = this.obs.putObject(options.bucket, this.name, ByteArrayInputStream(JsonUtils.write(payload.map).toByteArray()))
        if (putObject.statusCode >= 300) {
            throw Exception("obs putObject error")
        }
    }

    private fun getObject(): MemoryCardPayload {
        val options = this.options as StorageObsOptions

        val obsObject = try {
            this.obs.getObject(options.bucket, this.name)
        }
        catch (e: Exception) {
            log.error("获取${name}错误")
            null
        }

        if (obsObject == null) {
            return MemoryCardPayload()
        }
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
        obs.close()
        var card = MemoryCardPayload()
        card.map = JsonUtils.readValue(String(bos.toByteArray()))
        return card
    }

    private fun deleteObject() {
        val options = this.options as StorageObsOptions
        val deleteObject = try {
            this.obs.deleteObject(options.bucket, this.name)
        }
        catch (e: Exception) {
            log.error("删除${name}错误")
            null
        }
        if (deleteObject == null) {
            throw Exception("obs deleteObject error")
        }
        if (deleteObject.statusCode >= 300) {
            throw Exception("obs deleteObject error")
        }
    }
    override fun toString(): String {
        return "${this.name}<${this.name}>"
    }

    fun shutdown() {
        log.info("StorageObs, shutdown()")
        this.obs.close()
    }

    companion object {
        private val log = LoggerFactory.getLogger(StorageObs::class.java)
    }
}



fun main(){
    val storageObsOptions = StorageObsOptions("D5RKYDQRCRYICGP65H2R", "K0Va8jn8kWBK8jzdmC4QC2vvqsgF5Epz1iWhZOOp",
        "obs.cn-north-4.myhuaweicloud.com", "cybersa")

    val storageObs = StorageObs("notexist", storageObsOptions)
    val load = storageObs.load()
//    var memory = MemoryCardPayload()
//    var address = Address("福州", "付件")
//    var person = Person("sda", 13, address)
//    memory.map.put("person", person)
//    storageObs.save(memory)

//    val load = storageObs.load()
//    println(load.map)
//    load.map.forEach { t, u -> print(t + ":" + u) }
//    storageObs.destory()

//    var obsClient = ObsClient("D5RKYDQRCRYICGP65H2R", "K0Va8jn8kWBK8jzdmC4QC2vvqsgF5Epz1iWhZOOp",
//        "obs.cn-north-4.myhuaweicloud.com")
//    var map = mutableMapOf<String, String>()
//    map.put("a", "nihsdasd")
//    obsClient.putObject("cybersa", "objectname", ByteArrayInputStream(JsonUtils.write(map).toByteArray()))
//    val obsObject = obsClient.getObject("cybersa", "objectname")
//    var byte = ByteArray(1024)
//    var len = obsObject.objectContent.read(byte)
//    println(String(byte, 0 , len))
//    obsClient.close()
}
