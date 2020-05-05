package io.github.wechaty.examples;


import io.github.wechaty.MessageListener;
import io.github.wechaty.Wechaty;
import io.github.wechaty.schemas.RoomQueryFilter;
import io.github.wechaty.user.Contact;
import io.github.wechaty.user.Room;
import io.github.wechaty.utils.QrcodeUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


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

                Contact room = message.room();

                if(room != null){
                    room.say("dong");
                }else {
                    from.say("dong");
                }

            }

        });

        bot.start().get();

//        Room room = bot.room();
//
//        RoomQueryFilter roomQueryFilter = new RoomQueryFilter();
//
//        roomQueryFilter.setTopic("ChatOps - Donut");
//
//        Future<List<Room>> all = room.findAll(roomQueryFilter);
//
//        List<Room> rooms = all.get();
//
//        Room room1 = rooms.get(0);
//
//        room1.say("hi from kotlin wechaty bot");

    }
}
