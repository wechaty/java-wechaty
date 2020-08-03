package io.github.wechaty.user

import io.github.wechaty.Accessory
import io.github.wechaty.Wechaty
import org.slf4j.LoggerFactory

class Tag(wechaty:Wechaty,val id:String):Accessory(wechaty){

    fun add(to:Contact){
        wechaty.getPuppet().tagContactAdd(this.id,to.id!!).get()
    }

    fun remove(from:Contact){
        wechaty.getPuppet().tagContactRemove(this.id!!,from.id!!).get()
    }

    companion object{
        private val log = LoggerFactory.getLogger(Tag::class.java)
    }

}
