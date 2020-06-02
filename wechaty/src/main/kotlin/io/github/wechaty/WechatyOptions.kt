package io.github.wechaty

import io.github.wechaty.memorycard.MemoryCard
import io.github.wechaty.schemas.PuppetOptions

class WechatyOptions {

    var memory:MemoryCard? = null

    var name:String = "Wechaty"

    var puppet:String = "wechaty-puppet-hostie"

    var puppetOptions:PuppetOptions? = null

    var ioToken:String? = null

}
