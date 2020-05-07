package io.github.wechaty.io.github.wechaty.filebox

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import io.github.wechaty.io.github.wechaty.utils.JsonUtils
import io.vertx.core.MultiMap
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.core.json.get
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future


class FileBox(options: FileBoxOptions) {

    @JsonProperty
    private var mimeType: String? = null
    @JsonProperty
    private  var base64  : String? = null
    @JsonProperty
    private  var remoteUrl : String? = null
    @JsonProperty
    private  var qrCode    : String? = null
    @JsonProperty
    private  var buffer    : ByteArray?=null
    private  var localPath : String? = null
    @JsonProperty
    private var headers : OutgoingHttpHeaders? = null
    @JsonProperty
    private var name :String? = null
    @JsonProperty
    private var metadata: Metadata? = null
    @JsonProperty
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
        buffer = toByte(this)

        return JsonUtils.write(this)

    }

    fun toByte(fileBox: FileBox):ByteArray?{
        when(fileBox.type()){
            FileBoxType.File ->{

                val file = File(ClassLoader.getSystemResource("dong.jpg").path)

                return FileUtils.readFileToByteArray(file)

            }

            FileBoxType.Url ->{
                return null;
            }

            else ->{
                TODO()
            }

        }
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

        @JvmStatic
        fun fromJson(obj:String):FileBox{
            val jsonObject = JsonObject(obj)

            var fileBox:FileBox

            val type = jsonObject.getInteger("boxType")

            when(type){

                FileBoxType.Base64.code ->{
                    fileBox = fromBase64(
                            jsonObject.getString("base64"),
                            jsonObject.getString("name")
                    )
                }

                FileBoxType.Url.code ->{
                    fileBox = fromUrl(
                            jsonObject.getString("name"),
                            jsonObject.getString("remoteUrl")
                    )
                }

                FileBoxType.QRcode.code ->{
                    fileBox = fromQRCode(
                            jsonObject.getString("qrCode")
                    )
                }
                else ->{
                    throw Exception("unknown filebox json object{type} $jsonObject")
                }
            }

            fileBox.metadata = jsonObject.get("metadata")
            return fileBox;
        }

        @JvmStatic
        fun fromBase64(base64: String,name:String):FileBox{
            val options = FileBoxOptionsBase64(base64 = base64, name = name)
            return FileBox(options)
        }

        @JvmStatic
        fun fromDataUrl(dataUrl: String,name:String):FileBox{
            val base64 = dataUrlToBase64(dataUrl);
            return fromBase64(base64,name)
        }

        @JvmStatic
        fun fromQRCode(qrCode:String):FileBox{
            val optionsQRCode = FileBoxOptionsQRCode(name = "qrcode.png", qrCode = qrCode)
            return FileBox(optionsQRCode)
        }

        @JvmStatic
        fun fromUrl(url:String,name: String?,headers: OutgoingHttpHeaders? = null):FileBox{

            var localName :String? = name

            if(StringUtils.isEmpty(url)){
                val parsedUrl = URL(url)
                localName = parsedUrl.path
            }

            val optionsUrl = FileBoxOptionsUrl(name = localName, url = url)
            optionsUrl.headers = headers
            return FileBox(optionsUrl)

        }



        /**
         * ?????
         */
        fun dataUrlToBase64(dataUrl :String):String{
            val split = StringUtils.split(dataUrl, ",")
            return split[split.size - 1]
        }

    }








}
