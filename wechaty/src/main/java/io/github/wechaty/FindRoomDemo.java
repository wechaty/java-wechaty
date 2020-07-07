package io.github.wechaty;

import com.sun.xml.internal.ws.encoding.policy.MtomPolicyMapConfigurator;
import io.github.wechaty.schemas.ContactQueryFilter;
import io.github.wechaty.schemas.RoomQueryFilter;
import io.github.wechaty.user.Contact;
import io.github.wechaty.user.Room;
import io.github.wechaty.user.manager.ContactManager;
import io.github.wechaty.user.manager.RoomManager;
import io.github.wechaty.utils.QrcodeUtils;

import java.util.List;

public class FindRoomDemo {
    public static void main(String[] args) {
        Wechaty bot = Wechaty.instance(Token.token)
            .onScan((qrcode, statusScanStatus, data) -> System.out.println(QrcodeUtils.getQr(qrcode)))
            .onLogin(user -> System.out.println("User logined :" + user.name()))
            .onMessage(message -> {
                String text = message.text();
                Contact from = message.from();
                Contact to = message.to();
                Room room = message.room();
                System.out.println("接收到了消息" + text);
            })
            .onLogout((contactId, reason) -> {
                System.out.println("用户id为:" + contactId);
                System.out.println("理由为:" + reason);
            })
            .start(true);
    }
}
