# java-wechaty

![Java Wechaty](https://wechaty.github.io/java-wechaty/images/java-wechaty.png)

## Connecting Chatbots

[![Powered by Wechaty](https://img.shields.io/badge/Powered%20By-Wechaty-brightgreen.svg)](https://github.com/Wechaty/wechaty)

Wechaty is a RPA SDK for Wechat **Individual** Account that can help you create a chatbot in 6 lines of Java.

## WORK IN PROGRESS

Work in progress...

Please come back after 4 weeks...

## Voice of the Developers

> "Wechaty is a great solution, I believe there would be much more users recognize it." [link](https://github.com/Wechaty/wechaty/pull/310#issuecomment-285574472)  
> &mdash; <cite>@Gcaufy, Tencent Engineer, Author of [WePY](https://github.com/Tencent/wepy)</cite>
>
> "太好用，好用的想哭"  
> &mdash; <cite>@xinbenlv, Google Engineer, Founder of HaoShiYou.org</cite>
>
> "最好的微信开发库" [link](http://weibo.com/3296245513/Ec4iNp9Ld?type=comment)  
> &mdash; <cite>@Jarvis, Baidu Engineer</cite>
>
> "Wechaty让运营人员更多的时间思考如何进行活动策划、留存用户，商业变现" [link](http://mp.weixin.qq.com/s/dWHAj8XtiKG-1fIS5Og79g)  
> &mdash; <cite>@lijiarui, Founder & CEO of Juzi.BOT.</cite>
>
> "If you know js ... try Wechaty, it's easy to use."  
> &mdash; <cite>@Urinx Uri Lee, Author of [WeixinBot(Python)](https://github.com/Urinx/WeixinBot)</cite>

See more at [Wiki:Voice Of Developer](https://github.com/Wechaty/wechaty/wiki/Voice%20Of%20Developer)

## Join Us

Wechaty is used in many ChatBot projects by thousands of developers. If you want to talk with other developers, just scan the following QR Code in WeChat with secret code _java wechaty_, join our **Wechaty Java Developers' Home**.

![Wechaty Java Developers' Home](https://wechaty.github.io/wechaty/images/bot-qr-code.png)

Scan now, because other Wechaty Java developers want to talk with you too! (secret code: _java wechaty_)

## The World's Shortest Java ChatBot: 6 lines of Code

```java
class Bot{
  public static void main(String args[]){
    bot = Wechaty.instance()
      .on('scan', (qrcode, status, string) -> System.out.println('Scan QR Code to login: %s\nhttps://api.qrserver.com/v1/create-qr-code/?data=%s', status, encodeURIComponent(qrcode)))
      .on('login', user -> System.out.println('User %s logined', user))
      .on('message', message -> System.out.println('Message: %s', message))
      .start();
  }
}
```

## Development

To be writen:

```sh
make install
make bot
```

## Java Wechaty Developing Plan

We already have Wechaty in TypeScript, It will be not too hard to translate the TypeScript(TS) to Java because [wechaty](https://github.com/wechaty/wechaty) has only 3,000 lines of the TS code, they are well designed and de-coupled by the [wechaty-puppet](https://github.com/wechaty/wechaty-puppet/) abstraction. So after we have translated those 3,000 lines of TypeScript code, we will almost be done.

As we have already a ecosystem of Wechaty in TypeScript, so we will not have to implement everything in Java, especially, in the Feb 2020, we have finished the [@chatie/grpc](https://github.com/chatie/grpc) service abstracting module with the [wechaty-puppet-hostie](https://github.com/wechaty/wechaty-puppet-hostie) implmentation.

The following diagram shows out that we can reuse almost everything in TypeScript, and what we need to do is only the block located at the top right of the diagram: `Wechaty (Java)`.

```ascii
  +--------------------------+ +--------------------------+
  |                          | |                          |
  |   Wechaty (TypeScript)   | |     Wechaty (Java)       |
  |                          | |                          |
  +--------------------------+ +--------------------------+

  +-------------------------------------------------------+
  |                 Wechaty Puppet Hostie                 |
  |                                                       |
  |                (wechaty-puppet-hostie)                |
  +-------------------------------------------------------+

+---------------------  @chatie/grpc  ----------------------+

  +-------------------------------------------------------+
  |                Wechaty Puppet Abstract                |
  |                                                       |
  |                   (wechaty-puppet)                    |
  +-------------------------------------------------------+

  +--------------------------+ +--------------------------+
  |      Pad Protocol        | |      Web Protocol        |
  |                          | |                          |
  | wechaty-puppet-padplus   | |(wechaty-puppet-puppeteer)|
  +--------------------------+ +--------------------------+
  +--------------------------+ +--------------------------+
  |    Windows Protocol      | |       Mac Protocol       |
  |                          | |                          |
  | (wechaty-puppet-windows) | | (wechaty-puppet-macpro)  |
  +--------------------------+ +--------------------------+
```

## Example: How to Translate TypeScript to Java

There's a 100 lines class named `Image` in charge of downloading the WeChat image to different sizes.

It is a great example for demonstrating how do we translate the TypeScript to Java in Wechaty Way:

### Image Class Source Code

- TypeScript: <https://github.com/wechaty/wechaty/blob/master/src/user/image.ts>
- Java: <https://github.com/wechaty/java-wechaty/blob/master/src/wechaty/user/image.py>

If you are interested in the translation and want to look at how it works, it will be a good start from reading and comparing those two `Image` class files in TypeScript and Java at the same time.

## To-do List

- TS: TypeScript
- SLOC: Source Lines Of Code

### Wechaty Internal Modules

1. [ ] Class Wechaty
    - TS SLOC(1160): <https://github.com/wechaty/wechaty/blob/master/src/wechaty.ts>
    - [ ] Code
    - [ ] Unit Tests
    - [ ] Documentation
1. [ ] Class Contact
    - TS SLOC(804): <https://github.com/wechaty/wechaty/blob/master/src/user/contact.ts>
    - [x] Code
    - [ ] Unit Tests
    - [ ] Documentation
1. [ ] Class ContactSelf
    - TS SLOC(199): <https://github.com/wechaty/wechaty/blob/master/src/user/contact-self.ts>
    - [x] Code
    - [ ] Unit Tests
    - [ ] Documentation
1. [ ] Class Message
    - TS SLOC(1054): <https://github.com/wechaty/wechaty/blob/master/src/user/message.ts>
    - [x] Code
    - [ ] Unit Tests
    - [ ] Documentation
1. [ ] Class Room
    - TS SLOC(1194): <https://github.com/wechaty/wechaty/blob/master/src/user/room.ts>
    - [x] Code
    - [ ] Unit Tests
    - [ ] Documentation
1. [ ] Class Image
    - TS SLOC(60): <https://github.com/wechaty/wechaty/blob/master/src/user/image.ts>
    - [x] Code
    - [ ] Unit Tests
    - [ ] Documentation
1. [ ] Class Accessory
    - TS SLOC(179): <https://github.com/wechaty/wechaty/blob/master/src/accessory.ts>
    - [x] Code
    - [ ] Unit Tests
    - [ ] Documentation
1. [ ] Class Config
    - TS SLOC(187): <https://github.com/wechaty/wechaty/blob/master/src/config.ts>
    - [ ] Code
    - [ ] Unit Tests
    - [ ] Documentation
1. [ ] Class Favorite
    - TS SLOC(52): <https://github.com/wechaty/wechaty/blob/master/src/user/favorite.ts>
    - [ ] Code
    - [ ] Unit Tests
    - [ ] Documentation
1. [ ] Class Friendship
    - TS SLOC(417): <https://github.com/wechaty/wechaty/blob/master/src/user/friendship.ts>
    - [x] Code
    - [ ] Unit Tests
    - [ ] Documentation
1. [ ] Class MiniProgram
    - TS SLOC(70): <https://github.com/wechaty/wechaty/blob/master/src/user/mini-program.ts>
    - [ ] Code
    - [ ] Unit Tests
    - [ ] Documentation
1. [ ] Class RoomInvitation
    - TS SLOC(317): <https://github.com/wechaty/wechaty/blob/master/src/user/room-invitation.ts>
    - [ ] Code
    - [ ] Unit Tests
    - [ ] Documentation
1. [ ] Class Tag
    - TS SLOC(190): <https://github.com/wechaty/wechaty/blob/master/src/user/tag.ts>
    - [x] Code
    - [ ] Unit Tests
    - [ ] Documentation
1. [ ] Class UrlLink
    - TS SLOC(107): <https://github.com/wechaty/wechaty/blob/master/src/user/url-link.ts>
    - [ ] Code
    - [ ] Unit Tests
    - [ ] Documentation

### Wechaty External Modules

1. [ ] Class FileBox
    - TS SLOC(638): <https://github.com/huan/file-box/blob/master/src/file-box.ts>
    - [ ] Code
    - [ ] Unit Tests
    - [ ] Documentation
1. [ ] Class MemoryCard
    - TS SLOC(376): <https://github.com/huan/memory-card/blob/master/src/memory-card.ts>
    - [ ] Code
    - [ ] Unit Tests
    - [ ] Documentation
1. [ ] Class WechatyPuppet
    - TS SLOC(1115): <https://github.com/wechaty/wechaty-puppet/blob/master/src/puppet.ts>
    - [x] Code
    - [ ] Unit Tests
    - [ ] Documentation
1. [ ] Class WechatyPuppetHostie
    - TS SLOC(909): <https://github.com/wechaty/wechaty-puppet-hostie/blob/master/src/grpc/puppet-client.ts>
    - [x] Code
    - [ ] Unit Tests
    - [ ] Documentation

## Usage

1. Add a token to class `io.github.wechaty.examples.Main` in folder `wechaty/src/main/java`
2. Build:

    ```shell
    mvn install
    ```

3. Run

    ```shell
    java -jar wechaty/target/wechaty-1.0.0-SNAPSOHOT-jar-with-dependencies.jar   
    # or run in background  
    nohup java -jar wechaty/target/wechaty-1.0.0-SNAPSOHOT-jar-with-dependencies.jar &>> nohup.out & tailf nohup.out
    ```

4. enjoy

## Requirements

1. JDK/JRE

## Install

```shell
mvn install wechaty
```

## Links

1. [Publish Java Module to Maven Central Repo - OSSRH Guide](https://central.sonatype.org/pages/ossrh-guide.html)
1. [Kotlin vs Java: Most Important Differences That You Must Know](https://hackr.io/blog/kotlin-vs-java)

## History

### master

### v0.0.1 (Mar 12, 2020)

1. Project created.
1. First contributor joined: [@diaozxin007](https://github.com/diaozxin007) Zhengxin DIAO (刁政欣)

## Related Projects

- [Wechaty](https://github.com/wechaty/wechaty) - WeChat Bot SDK for Individual Account in TypeScript
- [Python Wechaty](https://github.com/wechaty/python-wechaty) - Python WeChat Bot SDK for Individual Account.
- [Go Wechaty](https://github.com/wechaty/go-wechaty) - Go WeChat Bot SDK for Individual Account.

## Authors

- [@diaozxin007](https://github.com/diaozxin007) diaozxin@gmail.com
  - Website: [犀利豆的博客](https://xilidou.com/)
- [@huan](https://github.com/huan) ([李卓桓](http://linkedin.com/in/zixia)), Tencent TVP of Chatbot, zixia@zixia.net

## Maintainers

See [MAINTAINERS](MAINTAINERS)

## Copyright & License

- Code & Docs © 2020 Wechaty
- Code released under the Apache-2.0 License
- Docs released under Creative Commons
