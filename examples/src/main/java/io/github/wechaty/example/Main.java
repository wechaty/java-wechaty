package io.github.wechaty.example;


import io.github.wechaty.LoginListener;
import io.github.wechaty.MessageListener;
import io.github.wechaty.Wechaty;
import io.github.wechaty.filebox.FileBox;
import io.github.wechaty.io.github.wechaty.schemas.EventEnum;
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
            .onScan((qrcode, statusScanStatus, data) -> System.out.println(QrcodeUtils.getQr(qrcode)))
            .onLogin(user -> System.out.println(user))
            .onMessage(message -> {
                Room room = message.room();
                String text = message.text();
                if (StringUtils.equals(text, "#ding")) {
                    if (room != null) {
                        room.say("dong");
                    }
                }
            }).start(true);

//    }

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
//        FileBox fileBox = FileBox.fromFile("dong.jpg", "dong.jpg");
//
//        room1.say(fileBox).get();

    }


}
