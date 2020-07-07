package io.github.wechaty;

import io.github.wechaty.filebox.FileBox;
import io.github.wechaty.user.Contact;
import io.github.wechaty.user.Room;
import io.github.wechaty.user.UrlLink;
import io.github.wechaty.utils.QrcodeUtils;

import java.util.List;

public class MetionDemo {
    public static void main(String[] args) {
        Wechaty bot = Wechaty.instance(Token.token)
            .onScan((qrcode, statusScanStatus, data) -> System.out.println(QrcodeUtils.getQr(qrcode)))
            .onLogin(user -> System.out.println("User logined :" + user.name()))
            .onMessage(message -> {
                String text = message.text();
                Contact from = message.from();
                Room room = message.room();
                System.out.println(message.text());
                System.out.println("收到来自" + from.name() + "的消息:" + text);

                List<Contact> contacts = message.mentionList();
                for (Contact contact : contacts) {
                    System.out.println(contact.name());
                }
            })
            .start(true);
    }
}
