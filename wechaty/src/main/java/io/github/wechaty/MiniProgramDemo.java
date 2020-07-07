package io.github.wechaty;

import io.github.wechaty.schemas.ContactQueryFilter;
import io.github.wechaty.user.Contact;
import io.github.wechaty.user.MiniProgram;
import io.github.wechaty.user.Room;
import io.github.wechaty.user.manager.ContactManager;
import io.github.wechaty.utils.QrcodeUtils;

public class MiniProgramDemo {
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

                from.say(MiniProgram.Companion.create());
            })
            .start(true);
    }
}
