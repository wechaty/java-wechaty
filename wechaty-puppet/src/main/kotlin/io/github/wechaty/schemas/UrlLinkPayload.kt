package io.github.wechaty.schemas;

data class UrlLinkPayload(val title: String,val url: String) {

   var description: String? = null

   var thumbnailUrl: String? = null

    override fun toString(): String {
        return "UrlLinkPayload(title='$title', url='$url', description=$description, thumbnailUrl=$thumbnailUrl)"
    }

}
