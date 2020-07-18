package io.github.wechaty.memorycard.backend

import io.github.wechaty.memorycard.MemoryCardPayload
import io.github.wechaty.memorycard.StorageBackend
import io.github.wechaty.memorycard.StorageBackendOptions
import io.github.wechaty.memorycard.StorageFileOptions
import io.github.wechaty.utils.JsonUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.lang.Exception

// 本身是不存储数据的
// 存储了持久化的文件名和以及用什么方式存储
class StorageFile(val name: String, var options: StorageBackendOptions) : StorageBackend(name, options) {

    private var absFileName: String

    init {

        log.info("StorageFile, constructor(%s, ...)", name)

        options.type = "file"
        options = options as StorageFileOptions

        val file = File(name)

        if(file.isAbsolute) {
            this.absFileName = name
        }
        else {
            this.absFileName = FilenameUtils.concat(System.getProperty("user.dir"),name)
        }

        if(!StringUtils.endsWith(this.absFileName,".memory-card.json")) {
            this.absFileName += ".memory-card.json"
        }

    }

    override fun load(): MemoryCardPayload {
        log.info("StorageFile, load() from %s", this.absFileName)

        val file = File(absFileName)
        if(!file.exists()){
            return MemoryCardPayload()
        }

        var text = ""
        try {
            text = FileUtils.readFileToString(file, "UTF-8")
        }
        catch (e: IOException) {
            log.error("load() from file %s error %s", this.absFileName, e.toString())
        }

        val payload = MemoryCardPayload()
        try {
            payload.map = JsonUtils.readValue(text)
        }
        catch (e: Exception) {
            log.error("MemoryCard, load() exception: %s", e)
        }
        return payload
    }

    override fun save(payload: MemoryCardPayload) {
        log.info("StorageFile, save() to %s", this.absFileName)

        val text = JsonUtils.write(payload.map)
        val file = File(absFileName)
        try {
            FileUtils.write(file,text,"UTF-8")
        }
        catch (e: IOException) {
            log.error("MemoryCard, save() exception: %s", e)
        }
    }

    override fun destory() {
        log.info("StorageFile, destoay()")

        val file = File(absFileName)
        if (file.exists()) {
            val deleteQuietly = FileUtils.deleteQuietly(file)
            if (deleteQuietly) {
                log.info("destory() ${this.absFileName} success")
            }
            else {
                log.warn("destory() ${this.absFileName} failed")
            }
        }
    }

    override fun toString(): String {
        return "${this.name}<${this.absFileName}>"
    }
    companion object {
        private val log = LoggerFactory.getLogger(StorageFile::class.java)
    }
}



fun main() {
    val storageFile = StorageFile("test", StorageFileOptions())
}
