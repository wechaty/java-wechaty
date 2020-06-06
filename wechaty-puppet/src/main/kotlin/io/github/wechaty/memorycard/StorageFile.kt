//package io.github.wechaty.memorycard
//
//import io.github.wechaty.utils.JsonUtils
//import org.apache.commons.io.FileUtils
//import org.apache.commons.io.FilenameUtils
//import org.apache.commons.lang3.StringUtils
//import java.io.File
//
//class StorageFile(val name: String, var options: StorageBackendOptions) : StorageBackend(name,options) {
//
//    private var absFileName:String
//
//    init {
//        options.type = "file"
//        options = options as StorageFileOptions
//        val file = File(name)
//        if(file.isAbsolute){
//            this.absFileName = name
//        }else{
//            this.absFileName = FilenameUtils.concat(System.getProperty("user.dir"),name)
//        }
//
//        if(!StringUtils.endsWith(this.absFileName,".memory-card.json")){
//            this.absFileName += ".memory-card.json"
//        }
//
//    }
//
//    override fun save(payload: MemoryCardPayload) {
//        val text = JsonUtils.write(payload)
//        val file = File(absFileName)
//        FileUtils.write(file,text,"UTF-8")
//
//    }
//
//    override fun load(): MemoryCardPayload {
//        val file = File(absFileName)
//        if(!file.exists()){
//            return MemoryCardPayload()
//        }
//        val text = FileUtils.readFileToString(file, "UTF-8")
//        return JsonUtils.readValue(text);
//
//    }
//
//    override fun destory() {
//        TODO("Not yet implemented")
//    }
//
//}
//
//
//
//fun main(){
//
//    StorageFile("test",StorageFileOptions())
//
//}
