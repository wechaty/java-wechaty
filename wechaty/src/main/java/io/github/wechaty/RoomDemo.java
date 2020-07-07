package io.github.wechaty;

import io.github.wechaty.schemas.ContactQueryFilter;
import io.github.wechaty.user.Contact;
import io.github.wechaty.user.Room;
import io.github.wechaty.user.manager.ContactManager;
import io.github.wechaty.user.manager.RoomManager;
import io.github.wechaty.utils.QrcodeUtils;

public class RoomDemo {
    public static void main(String[] args) {
        Wechaty bot = Wechaty.instance(Token.token)
            .onScan((qrcode, statusScanStatus, data) -> System.out.println(QrcodeUtils.getQr(qrcode)))
            .onLogin(user -> System.out.println("User logined :" + user.name()))
            .onMessage(message -> {
                String text = message.text();
                Contact from = message.from();
                Contact to = message.to();
                Room room = message.room();
                System.out.println("收到来自" + from.name() + "的消息:" + text);

                ContactQueryFilter queryFilter = new ContactQueryFilter();
                queryFilter.setName("Friday BOT");
                ContactManager contactManager = new ContactManager(to.getWechaty());
                Contact contact = contactManager.find(queryFilter);

                if (contact != null) {
                    room.add(contact);
                    System.out.println(contact.name());
                    room.say("已邀请" + contact.name() + "加入群聊");
                }
            })
            .start(true);
    }
}
