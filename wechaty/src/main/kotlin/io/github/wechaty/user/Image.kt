package io.github.wechaty.user

import io.github.wechaty.Accessory
import io.github.wechaty.Wechaty
import io.github.wechaty.filebox.FileBox
import io.github.wechaty.schemas.ImageType

class Image(wechaty: Wechaty,val id:String):Accessory(wechaty){

    fun thumbnail(): FileBox {
        return wechaty.getPuppet().messageImage(id, ImageType.Thumbnail).get()
    }

    fun hd():FileBox{
        return wechaty.getPuppet().messageImage(id, ImageType.HD).get()
    }

    fun artwork():FileBox{
        return wechaty.getPuppet().messageImage(id, ImageType.Artwork).get()
    }

}
