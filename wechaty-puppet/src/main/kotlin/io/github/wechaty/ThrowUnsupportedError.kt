package io.github.wechaty.io.github.wechaty

fun throwUnsupportedError(){
    throw Exception(
            listOf(
                "Wechaty Puppet Unsupported API Error.",
                " ",
                "Learn More At https://github.com/wechaty/wechaty-puppet/wiki/Compatibility"
            ).toString()
    )
}