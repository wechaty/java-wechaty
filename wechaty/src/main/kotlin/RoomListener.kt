import io.github.wechaty.user.Contact
import io.github.wechaty.user.Message
import io.github.wechaty.user.RoomInvitation
import java.util.*

@FunctionalInterface
interface InviteListener {
    fun handler(inviter: Contact, invitation: RoomInvitation)
}

@FunctionalInterface
interface LeaveListener {
    fun handler(leaverList: List<Contact>, remover: Contact, date: Date)
}

@FunctionalInterface
interface MessageListener {
    fun handler(message: Message, date: Date)
}

@FunctionalInterface
interface JoinListener {
    fun handler(inviteeList: List<Contact>, inviter: Contact, date: Date)
}

@FunctionalInterface
interface TopicListener {
    fun handler(topic: String, oldTopic: String, changer: Contact, date: Date)
}



