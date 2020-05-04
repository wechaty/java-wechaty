package io.github.wechaty.type

import io.github.wechaty.user.Contact
import java.util.concurrent.Future

interface Sayable {
    fun say(something: Any, contact: Contact): Future<Any>
}

interface Acceptable{
    fun accept():Future<Void>
}
