package io.github.wechaty.example;

import io.github.wechaty.Wechaty;
import io.github.wechaty.plugins.WechatyPlugins;

public class MainWithPlugin {

    public static void main(String[] args) {

        Wechaty bot = Wechaty.instance("your-token")
            .use(WechatyPlugins.ScanPlugin(), WechatyPlugins.DingDongPlugin(null))
            .start(true);

    }

}
