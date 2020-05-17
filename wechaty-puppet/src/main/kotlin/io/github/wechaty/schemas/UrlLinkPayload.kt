package io.github.wechaty.schemas;

data class UrlLinkPayload(val title: String,val url: String) {

   var description: String? = null

   var thumbnailUrl: String? = null

}
