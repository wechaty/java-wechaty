package io.github.wechaty.user

import io.github.wechaty.Wechaty
import io.github.wechaty.filebox.FileBox
import io.github.wechaty.utils.QrcodeUtils.Companion.guardQrCodeValue
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class ContactSelf(wechaty: Wechaty,id: String) : Contact(wechaty,id){

    override fun avatar(): Future<FileBox> {
        return super.avatar()
    }

    fun avatar(fileBox:FileBox):Future<Void>{
        return CompletableFuture.supplyAsync {
            puppet.setContactAvatar(super.id, fileBox)
            null
        }
    }

    fun qrcode(): Future<String> {
        log.info("Contact, qrcode()")

        val puppetId: String = try {
            this.puppet.selfId().toString()
        }
        catch (e: Exception) {
            throw Exception("Can not get qrcode, user might be either not logged in or already logged out")
        }

        if (this.id !== puppetId) {
            throw Exception("only can get qrcode for the login userself")
        }

        return CompletableFuture.supplyAsync {
            val qrcodeValue = this.puppet.contactSelfQRCode().get()
            guardQrCodeValue(qrcodeValue)
        }
    }

    fun name(name: String?): String? {
        if (name == null) {
            return super.name()
        }
        val puppetId = try {
            this.puppet.selfId()
        }
        catch (e: Exception) {
            throw Exception("Can not get qrcode, user might be either not logged in or already logged out")
        }
        if (this.id !== puppetId) {
            throw Exception("only can get qrcode for the login userself")
        }
        this.puppet.contactSelfName(name)
        return null
    }

    fun signature (signature: String): Future<Void> {
        log.debug("ContactSelf, signature()")

        val puppetId = try {
            this.puppet.selfId()
        }
        catch (e: Exception) {
            throw Exception("Can not get qrcode, user might be either not logged in or already logged out")
        }

        if (this.id !== puppetId) {
            throw Exception("only can get qrcode for the login userself")
        }
        // maybe
        return this.puppet.contactSelfSignature(signature)
    }

    companion object {
        private val log = LoggerFactory.getLogger(ContactSelf::class.java)
    }

}
