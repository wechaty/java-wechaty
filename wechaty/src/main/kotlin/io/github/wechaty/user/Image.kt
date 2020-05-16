package io.github.wechaty.user

import io.github.wechaty.Accessory
import io.github.wechaty.Wechaty
import io.github.wechaty.io.github.wechaty.filebox.FileBox
import io.github.wechaty.io.github.wechaty.schemas.ImageType

class Image(wechaty: Wechaty):Accessory(wechaty){

    private var id:String?=null

    fun create(id:String):Image{
        val image = Image(wechaty)
        image.id = id
        return image
    }

    fun thumbnail():FileBox{
        return wechaty.getPuppet().messageImage(id!!, ImageType.Thumbnail).get()
    }

    fun hd():FileBox{
        return wechaty.getPuppet().messageImage(id!!, ImageType.HD).get()
    }

    fun artwork():FileBox{
        return wechaty.getPuppet().messageImage(id!!, ImageType.Artwork).get()
    }

}