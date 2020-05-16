package io.github.wechaty.user

import io.github.wechaty.Wechaty
import io.github.wechaty.filebox.FileBox
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class ContactSelf(wechaty: Wechaty) : Contact(wechaty){

    override fun avatar(): Future<FileBox> {
        return super.avatar()
    }

    fun avatar(fileBox:FileBox):Future<Void>{
        return CompletableFuture.supplyAsync {
            puppet.setContactAvatar(this.id!!, fileBox)
            null
        }

    }

}
