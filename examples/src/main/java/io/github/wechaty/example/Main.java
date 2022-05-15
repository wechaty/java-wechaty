package io.github.wechaty.example;


import io.github.wechaty.Wechaty;
import io.github.wechaty.WechatyOptions;
import io.github.wechaty.schemas.PuppetOptions;
import io.github.wechaty.schemas.ScanStatus;
import io.github.wechaty.user.Room;
import io.github.wechaty.utils.JsonUtils;
import io.github.wechaty.utils.QrcodeUtils;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.concurrent.ExecutionException;


public class Main {

    public static void main(String[] args) {
        String yourDiyToken = "";
//        runWithTarget(yourDiyToken, "127.0.0.1:8788");
        runWithCloud(yourDiyToken);
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


    public static void runWithCloud(String token) {
        Wechaty bot = Wechaty.instance(token)
            .onScan((qrcode, statusScanStatus, data) -> {
                if (statusScanStatus == ScanStatus.Waiting) {
                    System.out.println(QrcodeUtils.getQr(qrcode));
                }
            })
            .onLogin(user -> System.out.println(user.name() + "/" + user.gender() + "/" + user.city()))
            .onMessage(message -> {
                System.out.print(message.text() + "/" + message.talker().name() + "/");
                String text = message.text();
                if (StringUtils.equals(text, "#ding")) {
                    if (message.room() != null) {
                        message.room().say("dong");
                    } else if (message.talker() != null) {
                        message.talker().say("dong");
                    }
                }
            }).start(true);
    }

    public static void runWithTarget(String token, String hostPort) {
        WechatyOptions wechatyOptions = new WechatyOptions();
        PuppetOptions puppetOptions = new PuppetOptions();
        puppetOptions.setToken(token);
        puppetOptions.setEndPoint(hostPort);
        wechatyOptions.setPuppetOptions(puppetOptions);
        Wechaty bot = Wechaty.instance(wechatyOptions)
            .onScan((qrcode, statusScanStatus, data) -> {
                if (statusScanStatus == ScanStatus.Waiting) {
                    System.out.println(QrcodeUtils.getQr(qrcode));
                }
            })
            .onLogin(user -> System.out.println(user.name() + "/" + user.gender() + "/" + user.city()))
            .onMessage(message -> {
                System.out.print(message.text() + "/" + message.talker().name() + "/");
                String text = message.text();
                if (StringUtils.equals(text, "#ding")) {
                    if (message.room() != null) {
                        message.room().say("dong");
                    } else if (message.talker() != null) {
                        message.talker().say("dong");
                    }
                }
            }).start(true);
    }


}
