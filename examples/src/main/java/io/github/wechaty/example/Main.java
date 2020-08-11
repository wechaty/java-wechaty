package io.github.wechaty.example;


import io.github.wechaty.Wechaty;
import io.github.wechaty.user.Contact;
import io.github.wechaty.user.Room;
import io.github.wechaty.utils.QrcodeUtils;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.concurrent.ExecutionException;


public class Main {

    public static void main(String[] args){

        Wechaty bot = Wechaty.instance("your_token")
            .onScan((qrcode, statusScanStatus, data) -> {
                System.out.println(QrcodeUtils.getQr(qrcode));
                System.out.println("Online Image: https://wechaty.github.io/qrcode/" + qrcode);
            })
            .onLogin(user -> System.out.println(user.name() + "login"))
            .onMessage(message -> {
                Room room = message.room();
                String text = message.text();
                Contact from = message.from();
                if (StringUtils.equals(text, "ding")) {
                    if (room != null) {
                        room.say("dong");
                    }
                    else {
                        // say something to from contact
                        from.say("hello:" + from.name());
                    }
                }
            }).start(true);
    }
}
