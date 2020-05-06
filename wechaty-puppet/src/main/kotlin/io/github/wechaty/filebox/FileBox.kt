package io.github.wechaty.io.github.wechaty.filebox

import io.vertx.core.MultiMap
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.client.WebClient
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class FileBox(options: FileBoxOptions) {

    private var mimeType: String? = null
    private  var base64  : String? = null
    private  var remoteUrl : String? = null
    private  var qrCode    : String? = null

    private  var buffer    : Buffer? = null
    private  var localPath : String? = null
    private  var stream :Readable? =null

    private  var headers: OutgoingHttpHeaders? =null

    private lateinit var name :String
    private var metadata: Metadata? = null
    private lateinit var boxType:FileBoxType

    private var vertx:Vertx = Vertx.vertx()

    init {
        when(options){
            is FileBoxOptionsBuffer ->{
                this.name = options.name
                this.boxType = options.type
                this.buffer = options.buffer
            }

            is FileBoxOptionsFile ->{
                this.name = options.name
                this.boxType = options.type
                this.localPath = options.path
            }

            is FileBoxOptionsUrl ->{
                this.name = options.name
                this.boxType = options.type
                this.remoteUrl = options.url
                this.headers = options.headers
            }

            is FileBoxOptionsStream ->{
                this.name = options.name
                this.boxType = options.type
                this.stream = options.stream
            }

            is FileBoxOptionsQRCode ->{
                this.name = options.name
                this.boxType = options.type
                this.qrCode = options.qrCode
            }

            is FileBoxOptionsBase64 ->{
                this.name = options.name
                this.boxType = options.type
                this.base64 = options.base64
            }
            
            

        }

    }

    fun type():FileBoxType{
        return this.boxType
    }

    fun ready():Future<Void>{
        if(this.boxType == FileBoxType.Url){

        }

        return CompletableFuture.completedFuture(null);
    }

    fun syncRemoteName():Future<Void>{

        val httpHeadHeader = httpHeadHeader(this.remoteUrl!!)

        val path: Path = File(localPath!!).toPath()
        val mimeType = Files.probeContentType(path)

        val map = httpHeadHeader.get()
        val get = map.get("content-type")

        when {
            StringUtils.isNotEmpty(get) -> {
                this.mimeType = get
            }
            StringUtils.isNotEmpty(mimeType) -> {
                this.mimeType = mimeType
            }
            else -> {
                this.mimeType = null
            }
        }

        return CompletableFuture.completedFuture(null);

    }

    private fun httpHeadHeader(url:String):Future<MultiMap>{

        val future = CompletableFuture<MultiMap>()

        val client = WebClient.create(vertx)

        client.headAbs(url).send {
            if(it.succeeded()){
                val result = it.result()
                val headers = result.headers()
                future.complete(headers)
            }else{
                future.complete(null)
            }
        }
        return future
    }

    fun toJsonString():String {
        return "";
    }

    companion object{

        @JvmStatic
        fun fromFile(path:String,name:String):FileBox{
            var localname = name

            if(StringUtils.isEmpty(name)){
                localname = FilenameUtils.getBaseName(path)
            }

            val fileBoxOptionsFile = FileBoxOptionsFile(path = path, name = localname)
            return FileBox(fileBoxOptionsFile)

        }

    }







}
