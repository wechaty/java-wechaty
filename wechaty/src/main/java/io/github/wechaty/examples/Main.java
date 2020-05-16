package io.github.wechaty.examples;


import io.github.wechaty.MessageListener;
import io.github.wechaty.Wechaty;
import io.github.wechaty.io.github.wechaty.filebox.FileBox;
import io.github.wechaty.user.Contact;
import io.github.wechaty.utils.QrcodeUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import okhttp3.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class Main {

    private static OkHttpClient client = new OkHttpClient();

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {

        FileBox fileBox = FileBox.fromUrl("https://img.xilidou.com/img/dong.jpg", null, null);

        Wechaty bot = Wechaty.instance("");

        bot.on("scan", (qrcode, statusScanStatus, data) -> {
            System.out.println(QrcodeUtils.getQr(qrcode));
        });

        bot.on("message", (MessageListener) message -> {

            Contact from = message.from();
            Contact room = message.room();

            String text = message.text();

            if (StringUtils.equals(text, "#ding")) {
                if (room != null) {
                    room.say("dong");
                } else {
                    from.say("dong");
                }
            }
        });
        bot.start().get();
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
