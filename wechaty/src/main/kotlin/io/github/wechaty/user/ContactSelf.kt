package io.github.wechaty.user

import io.github.wechaty.Wechaty
import io.github.wechaty.filebox.FileBox
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class ContactSelf(wechaty: Wechaty) : Contact(wechaty){

    constructor(wechaty: Wechaty,id: String):this(wechaty){
        super.id = id
    }

    override fun load(id: String): ContactSelf {
        if (this.id != null && this.id == id) {
            return this
        } else {
            val contact = ContactSelf(wechaty,id)
            wechaty.getContactCache().put(id, contact)
            return contact
        }
    }

    override fun avatar(): Future<FileBox> {
        return super.avatar()
    }

    fun avatar(fileBox:FileBox):Future<Void>{
        return CompletableFuture.supplyAsync {
            puppet.setContactAvatar(super.id!!, fileBox)
            null
        }

    }

}
