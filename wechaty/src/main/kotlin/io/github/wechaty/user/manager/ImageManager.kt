package io.github.wechaty.user.manager

import io.github.wechaty.Accessory
import io.github.wechaty.Wechaty
import io.github.wechaty.user.Image
import org.slf4j.LoggerFactory

class ImageManager (wechaty: Wechaty): Accessory(wechaty){

    fun create(id:String):Image{
        return Image(wechaty,id)
    }

    companion object {
        private val log = LoggerFactory.getLogger(ImageManager::class.java)
    }

}
