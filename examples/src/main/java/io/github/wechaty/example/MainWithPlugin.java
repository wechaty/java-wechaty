package io.github.wechaty.example;

import io.github.wechaty.Wechaty;
import io.github.wechaty.plugins.WecahtyPlugins;

public class MainWithPlugin {

    public static void main(String[] args) {

        Wechaty bot = Wechaty.instance("your-token")
            .use(WecahtyPlugins.ScanPlugin(), WecahtyPlugins.DingDongPlugin(null))
            .start(true);

    }

}
