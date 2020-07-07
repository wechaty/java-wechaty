package io.github.wechaty.io.github.wechaty.memorycard.backend

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

class StorageNop(val name: String, var options: StorageBackendOptions) : StorageBackend(name,options) {

    private var absFileName:String

    init {
        options.type = "file"
        options = options as StorageFileOptions
        val file = File(name)
        if(file.isAbsolute){
            this.absFileName = name
        }else{
            this.absFileName = FilenameUtils.concat(System.getProperty("user.dir"),name)
        }

        if(!StringUtils.endsWith(this.absFileName,".memory-card.json")){
            this.absFileName += ".memory-card.json"
        }

    }

    override fun save(payload: MemoryCardPayload) {
        val text = JsonUtils.write(payload)
        val file = File(absFileName)
        FileUtils.write(file,text,"UTF-8")

    }

    override fun load(): MemoryCardPayload {
        val file = File(absFileName)
        if(!file.exists()){
            return MemoryCardPayload()
        }
        val text = FileUtils.readFileToString(file, "UTF-8")
        return JsonUtils.readValue(text);

    }

    override fun destory() {
        TODO("Not yet implemented")
    }

    companion object {
        private val log = LoggerFactory.getLogger(StorageNop::class.java)
    }

}



fun main(){

    StorageFile("test", StorageFileOptions())

}
