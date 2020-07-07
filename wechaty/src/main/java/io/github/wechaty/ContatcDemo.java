package io.github.wechaty;

import io.github.wechaty.schemas.ContactQueryFilter;
import io.github.wechaty.user.Contact;
import io.github.wechaty.user.Room;
import io.github.wechaty.user.manager.ContactManager;
import io.github.wechaty.user.manager.MessageManager;
import io.github.wechaty.utils.QrcodeUtils;

import java.util.List;

public class ContatcDemo {
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

                if (text.contains("find")) {
                    from.say("请输入联系人名字:");
                }
                else {
                    ContactQueryFilter queryFilter = new ContactQueryFilter();
                    queryFilter.setName("犀利豆");
                    ContactManager contactManager = new ContactManager(to.getWechaty());
                    Contact contact = contactManager.find(queryFilter);
                    // 如果查询参数为空,那么就查询全部
                    contactManager.findAll(new ContactQueryFilter());
                    assert contact != null;
                    System.out.println(contact.name());
                }
            })
            .start(true);
    }
}
