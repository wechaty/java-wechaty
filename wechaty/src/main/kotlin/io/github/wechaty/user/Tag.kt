package io.github.wechaty.user

import io.github.wechaty.Accessory
import io.github.wechaty.Wechaty
import org.slf4j.LoggerFactory

class Tag(wechaty:Wechaty):Accessory(wechaty){

    constructor(wechaty: Wechaty,id: String):this(wechaty){
        this.id = id
    }
    private var id:String ? = null
    fun load(id:String):Tag{
        val existingTag = wechaty.getTagCache().getIfPresent(id)

        if(existingTag != null){
            return existingTag
        }
        val tag = Tag(wechaty, id)

        wechaty.getTagCache().put(id,tag)
        return tag

    }

    fun get(tag:String):Tag{
        return load(tag)
    }

    fun delete(tag: Tag){
        wechaty.getPuppet().tagContactDelete(tag.id!!).get()
    }

    fun add(to:Contact){
        wechaty.getPuppet().tagContactAdd(this.id!!,to.id!!).get()
    }

    fun remove(from:Contact){
        wechaty.getPuppet().tagContactRemove(this.id!!,from.id!!).get()
    }

    fun delete(tag: Tag,favorite: Favorite){

    }

    companion object{
        private val log = LoggerFactory.getLogger(Tag::class.java)
    }

}
