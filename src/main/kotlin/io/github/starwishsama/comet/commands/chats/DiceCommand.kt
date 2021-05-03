package io.github.starwishsama.comet.commands.chats

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.utils.CometUtil.toChain
import io.github.starwishsama.comet.utils.StringUtil.isNumeric
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import java.util.regex.Pattern

class DiceCommand : ChatCommand {
    // 骰子正则表达式
    private val pattern = Pattern.compile("(\\d)([dD])(\\d{1,3})")

    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (args.isEmpty()) {
            return getHelp().toChain()
        }

        val input = parseInput(args[0])

        if (input.size < 2) {
            return "骰子格式错误! 格式示例: 1d100".toChain()
        }

        val result = doDice(input[0], input[1])

        return "结果: ${args[0]}=${result.convertToString()}".toChain()
    }

    override fun getProps(): CommandProps {
        return CommandProps(
            "dice",
            listOf("tz", "骰子"),
            "投骰子",
            "nbot.commands.dice",
            UserLevel.USER
        )
    }

    override fun getHelp(): String = """
        /dice [指定方法] 投骰子
        
        如: /dice 1d100
    """.trimIndent()

    private fun parseInput(input: String): List<Int> {
        val matcher = pattern.matcher(input)
        val result = mutableListOf<Int>()

        if (!Pattern.matches(pattern.pattern(), input)) {
            return emptyList()
        }

        while (matcher.find()) {
            val time = matcher.group(1)
            val d = matcher.group(2)
            val diceSize = matcher.group(3)

            if (time.isNumeric()) {
                val parseTime = matcher.group(1).toIntOrNull() ?: return emptyList()
                if (parseTime <= 10) {
                    result.add(parseTime)
                } else {
                    return emptyList()
                }
            } else if (time.isEmpty() && d.toLowerCase() == "d") {
                result.add(1)
            }

            if (diceSize.isNumeric()) {
                result.add(diceSize.toIntOrNull() ?: return emptyList())
            }
        }

        return result
    }

    private fun doDice(time: Int, base: Int): List<DiceResult> {
        val results = mutableListOf<DiceResult>()

        repeat(time) {
            val result = RandomUtil.randomInt(0, base)

            results.add(DiceResult(base, result))
        }

        return results
    }

    data class DiceResult(
        val base: Int,
        val result: Int
    )

    private fun List<DiceResult>.convertToString(): String {
        return buildString {
            this@convertToString.forEach {
                append("d${it.base}(${it.result})")
                append("+")
            }
        }.removeSuffix("+")
    }
}