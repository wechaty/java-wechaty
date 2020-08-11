package io.github.wechaty.user

import io.github.wechaty.opengraph.openGraph
import io.github.wechaty.schemas.UrlLinkPayload
import org.apache.commons.lang3.StringUtils
import java.net.URL

class UrlLink(val payload:UrlLinkPayload) {

    fun url (): String {
        return payload.url
    }

    fun title (): String {
        return payload.title
    }

    fun thumbnailUrl ():String? {
        return this.payload.thumbnailUrl
    }

    fun description (): String? {
        return this.payload.description
    }

    override fun toString(): String {
        return "UrlLink(payload=$payload)"
    }

    companion object{
        fun create(url:String):UrlLink{
            val meta = openGraph(url).get()

            var description:String?
            var imageUrl:String?
            val title:String?

            val images = meta.getContent("image")
            imageUrl = if(StringUtils.isNotEmpty(images)){
                images
            }else{
                val properties = meta.getProperties("image")
                if(properties!= null && properties.isNotEmpty()){
                    properties[0]!!.extendedData.getContent("url")
                }else{
                    ""
                }
            }

            if(!StringUtils.startsWith(images,"http")){
                val url1 = URL(url)
                imageUrl = URL(url1,imageUrl).toString()
            }

            title = meta.getContent("title")

            description = meta.getContent("description")

            description = if(StringUtils.isEmpty(description)){
                title
            }else{
                description
            }

            val payload = UrlLinkPayload(title,url).apply {
                this.description = description
                this.thumbnailUrl = imageUrl
            }

            return UrlLink(payload)

        }

    }

}

fun main(){

    val create = UrlLink.create("https://xilidou.com")
//    val bilibili = UrlLink.create("https://www.bilibili.com/")
//    val image = UrlLink.create("https://img.xilidou.com/img/java-wechaty.png")
    println(create)
//    println(bilibili)
//    print(image)
    val urlLink = UrlLinkPayload("Nihao", "https://www.bilibili.com/")
    urlLink.thumbnailUrl = "https://xilidou.com/images/avatar.jpg"
    urlLink.description = "犀利豆的博客"
    print(urlLink)
}
