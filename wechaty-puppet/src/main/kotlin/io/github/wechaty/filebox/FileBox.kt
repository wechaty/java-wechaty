package io.github.wechaty.filebox

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.wechaty.utils.JsonUtils
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.collections4.CollectionUtils
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
    var mimeType: String? = null

    @JsonProperty
    var base64: String? = null

    @JsonProperty
    var remoteUrl: String? = null

    @JsonProperty
    var qrCode: String? = null

    @JsonProperty
    var buffer: ByteArray? = null
    var localPath: String? = null

    @JsonProperty
    var headers: OutgoingHttpHeaders? = null

    @JsonProperty
    var name: String? = null

    @JsonProperty
    var metadata: Metadata? = null

    @JsonProperty
    var boxType: FileBoxType

    private val client: OkHttpClient = OkHttpClient()

    init {
        when (options) {
            is FileBoxOptionsBuffer -> {
                this.name = options.name
                this.boxType = options.type
                this.buffer = options.buffer
            }

            is FileBoxOptionsFile -> {
                this.name = options.name
                this.boxType = options.type
                this.localPath = options.path
            }

            is FileBoxOptionsUrl -> {
                this.name = options.name
                this.boxType = options.type
                this.remoteUrl = options.url
                this.headers = options.headers
            }

            is FileBoxOptionsStream -> {
                this.name = options.name
                this.boxType = options.type
            }

            is FileBoxOptionsQRCode -> {
                this.name = options.name
                this.boxType = options.type
                this.qrCode = options.qrCode
            }

            is FileBoxOptionsBase64 -> {
                this.name = options.name
                this.boxType = options.type
                this.base64 = options.base64
            }
        }
    }

    fun type(): FileBoxType {
        return this.boxType
    }

    fun ready(): Future<Void> {
        if (this.boxType == FileBoxType.Url) {

        }

        return CompletableFuture.completedFuture(null);
    }

    fun syncRemoteName(): Future<Void> {

        val httpHeadHeader = httpHeadHeader(this.remoteUrl!!)

        val path: Path = File(localPath!!).toPath()
        val mimeType = Files.probeContentType(path)

        val get = httpHeadHeader["content-type"]

        when {
            CollectionUtils.isNotEmpty(get) -> {
                this.mimeType = get?.get(0)
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

    private fun httpHeadHeader(url: String): Map<String, List<String>> {

        val request: Request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            val headers = response.headers
            return headers.toMultimap()
        }
    }

    fun toJsonString(): String {
        buffer = getBufferByte(this)

        return JsonUtils.write(this)

    }

    private fun getBufferByte(fileBox: FileBox): ByteArray? {
        when (fileBox.type()) {
            FileBoxType.File -> {

                val file = File(ClassLoader.getSystemResource("dong.jpg").path)

                return FileUtils.readFileToByteArray(file)

            }
            FileBoxType.Url -> {
                return null;
            }
            FileBoxType.Base64 -> {
                return null;
            }
            else -> {
                TODO()
            }

        }
    }

    companion object {

        @JvmStatic
        fun fromFile(path: String, name: String): FileBox {
            var localname = name

            if (StringUtils.isEmpty(name)) {
                localname = FilenameUtils.getBaseName(path)
            }

            val fileBoxOptionsFile = FileBoxOptionsFile(path = path, name = localname)
            return FileBox(fileBoxOptionsFile)

        }

        @JvmStatic
        fun fromJson(obj: String): FileBox {

//            return JsonUtils.readValue<FileBox>(obj)

            val jsonNode = JsonUtils.mapper.readTree(obj)

            var fileBox: FileBox

            val type = jsonNode.findValue("boxType").asInt()

            when (type) {

                FileBoxType.Base64.code -> {
                    fileBox = fromBase64(
                        jsonNode.findValue("base64").asText(),
                        jsonNode.findValue("name").asText()
                    )
                }

                FileBoxType.Url.code -> {
                    fileBox = fromUrl(
                        jsonNode.findValue("remoteUrl").asText(),
                        jsonNode.findValue("name").asText()
                    )
                }

                FileBoxType.QRcode.code -> {
                    fileBox = fromQRCode(
                        jsonNode.findValue("qrCode").asText()
                    )
                }
                else -> {
                    throw Exception("unknown filebox json object{type} $jsonNode")
                }
            }

//            fileBox.metadata = jsonNode.get("metadata")
            return fileBox;
        }

        @JvmStatic
        fun fromBase64(base64: String, name: String): FileBox {
            val options = FileBoxOptionsBase64(base64 = base64, name = name)
            return FileBox(options)
        }

        @JvmStatic
        fun fromDataUrl(dataUrl: String, name: String): FileBox {
            val base64 = dataUrlToBase64(dataUrl);
            return fromBase64(base64, name)
        }

        @JvmStatic
        fun fromQRCode(qrCode: String): FileBox {
            val optionsQRCode = FileBoxOptionsQRCode(name = "qrcode.png", qrCode = qrCode)
            return FileBox(optionsQRCode)
        }

        @JvmStatic
        fun fromUrl(url: String, name: String?, headers: OutgoingHttpHeaders? = null): FileBox {

            var localName: String? = name

            if (StringUtils.isEmpty(url)) {
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
        fun dataUrlToBase64(dataUrl: String): String {
            val split = StringUtils.split(dataUrl, ",")
            return split[split.size - 1]
        }

    }
}
