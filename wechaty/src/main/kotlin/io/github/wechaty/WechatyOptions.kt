package io.github.wechaty

import io.github.wechaty.memorycard.MemoryCard
import io.github.wechaty.schemas.PuppetOptions

class WechatyOptions {

    var memory: MemoryCard? = null

    var name:String = "Wechaty"

//    var puppet:String = "wechaty-puppet-hostie"
    var puppet:String = "io.github.wechaty.grpc.GrpcPuppet"

    var puppetOptions:PuppetOptions? = null

    var ioToken:String? = null

}
typealias WechatyPlugin = (Wechaty) -> Unit

