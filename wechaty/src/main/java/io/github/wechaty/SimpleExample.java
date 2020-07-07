package io.github.wechaty;

import io.github.wechaty.filebox.FileBox;
import io.github.wechaty.user.Contact;
import io.github.wechaty.user.Room;
import io.github.wechaty.user.UrlLink;
import io.github.wechaty.user.UrlLinkKt;
import io.github.wechaty.utils.QrcodeUtils;

import java.util.List;

public class SimpleExample {
    public static void main(String[] args) {

        Wechaty bot = Wechaty.instance(Token.token)
            .onScan((qrcode, statusScanStatus, data) -> System.out.println(QrcodeUtils.getQr(qrcode)))
            .onLogin(user -> System.out.println("User logined :" + user.name()))
            .onMessage(message -> {
                Contact to = message.to();
                String text = message.text();
                Contact from = message.from();
                Room room = message.room();
                UrlLink urlLink = UrlLink.Companion.create("https://wechaty.js.org/v/zh/api");
                System.out.println(message.text());
                System.out.println("收到来自" + from.name() + "的消息:" + text);
                if (text.contains("ding")) {
                    if (room != null) {
                        room.say("nihao" + from.name());
                    } else {
                        System.out.println("现在是单对单聊天");
                        // 向from说话
                        from.say("nihaosdasd");
                        from.say(urlLink);
                        from.say(FileBox.fromUrl("https://upload-images.jianshu.io/upload_images/13859386-effeaff79bda02fd.jpg?imageMogr2/auto-orient/strip|imageView2/1/w/300/h/240/format/webp", "images.jpg", null));
                        to.say("woyehao");
                    }
                }

                else {
                    System.out.println("不包含ding");
                }
            })
            .onLogout((contactId, reason) -> {
                System.out.println("用户id为:" + contactId);
                System.out.println("理由为" + contactId);
            })
            .start(true);
    }
}
