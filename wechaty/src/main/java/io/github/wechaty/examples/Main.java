package io.github.wechaty.examples;


import io.github.wechaty.MessageListener;
import io.github.wechaty.Wechaty;
import io.github.wechaty.schemas.ContactQueryFilter;
import io.github.wechaty.user.Contact;
import io.github.wechaty.user.Message;
import io.github.wechaty.utils.QrcodeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ExecutionException;


public class Main {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        String token = "_";

        Wechaty bot = Wechaty.instance(token);

        bot.on("scan", (qrcode, statusScanStatus, data) -> {
            System.out.println(qrcode);
            System.out.println(QrcodeUtils.getQr(qrcode));
        });

        bot.on("message", (MessageListener) message -> {
            String text = message.text();

            if(StringUtils.equals(text,"#ding")){

                Contact from = message.from();

                from.say("dong");

            }

        });

        bot.start().get();

    }
}
