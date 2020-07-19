package io.github.wechaty.user

import io.github.wechaty.Wechaty
import io.github.wechaty.filebox.FileBox
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class ContactSelf(wechaty: Wechaty, id: String) : Contact(wechaty, id) {

    fun avatar(fileBox: FileBox) {
        puppet.setContactAvatar(super.id, fileBox)
    }

    fun setName(name:String){
        puppet.contactSelfName(name).get()
        sync()
    }

    fun signature(signature:String){

        var puppetId:String? = puppet.selfId()

        let{
            puppetId != null
        }.run {
            puppet.contactSelfSignature(signature).get()
            sync()
        }

    }
}
