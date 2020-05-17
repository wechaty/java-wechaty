package io.github.wechaty.user

import io.github.wechaty.schemas.UrlLinkPayload

class UrlLink(var payload:UrlLinkPayload) {

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

    companion object{
        fun create(url:String):UrlLink{
            TODO()
        }

    }

}