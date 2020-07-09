package io.github.wechaty.io.github.wechaty.memorycard.test

import io.github.wechaty.io.github.wechaty.memorycard.backend.StorageFile
import io.github.wechaty.memorycard.*
import org.junit.Before
import org.junit.Test


class MemoryCardTest {

    lateinit var memoryCard: MemoryCard

    @Before
    fun setUp() {
        memoryCard = MemoryCard("test")
    }
    @Test
    fun testCreate() {

        this.memoryCard.load()
        this.memoryCard.values().forEach {
            println(it)
        }
        this.memoryCard.destory()
    }

    @Test
    fun testLoad() {
        this.memoryCard.load()
        this.memoryCard.set("a", "b")
        this.memoryCard.save()
    }

    @Test
    fun testSave() {
        var card = MemoryCard()
        card.load()
        card.set("a", "你好")
//        println(card.get<Any>("a")?.get())
        card.keys().forEach {
            println(it)
        }
        card.save()
    }
    @Test
    fun testDestory() {
    }

    @Test
    fun testMultiplex() {
        memoryCard.load()
        // 这里的name是前缀的名字
        // \rparent\nson
        val multiplex = Multiplex(this.memoryCard, "parent")
        val memoryCardOptions = MemoryCardOptions("son", multiplex = multiplex)
        println(memoryCardOptions)
        // 用debug模式会有问题
        // 如果一个memory是multiple的,那么它自身就不会保存,只会保存一个父类的
        var son = MemoryCard(options = memoryCardOptions)
        son.load()

        son.set("son", "b")
        son.set("nihao", "asdasd")
        println(son.get<Any>("son")?.get())
        son.delete("nihao")
        println(son.has("nihao").get())
        println(son.has("son").get())
        son.save()
    }

}
