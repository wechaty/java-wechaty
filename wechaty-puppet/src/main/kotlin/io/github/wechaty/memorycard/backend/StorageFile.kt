package io.github.wechaty.io.github.wechaty.memorycard.backend

import io.github.wechaty.io.github.wechaty.status.StateSwitch
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
import java.lang.Exception
import kotlin.math.abs

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
        val text = FileUtils.readFileToString(file, "UTF-8")
        var payload = MemoryCardPayload()
        try {
            payload = JsonUtils.readValue(text);
        }
        catch (e: Exception) {
            log.error("MemoryCard, load() exception: %s", e)
        }
        return payload

    }

    override fun save(payload: MemoryCardPayload) {
        log.info("StorageFile, save() to %s", this.absFileName)

        val text = JsonUtils.write(payload)
        val file = File(absFileName)

        FileUtils.write(file,text,"UTF-8")
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



fun main(){

    StorageFile("test", StorageFileOptions())

}
