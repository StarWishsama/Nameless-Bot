package io.github.starwishsama.comet.listeners

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.managers.GroupConfigManager
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.BotIsBeingMutedException
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import kotlin.time.ExperimentalTime

object RepeatListener : NListener {
    @ExperimentalTime
    override fun register(bot: Bot) {
        bot.subscribeGroupMessages {
            always {
                if (BotVariables.switch) {
                    try {
                        handleRepeat(this, RandomUtil.randomDouble())
                    } catch (e: BotIsBeingMutedException) {
                        BotVariables.logger.debug("[监听器] 机器人已被禁言, ${e.message}")
                    }
                }
            }
        }
    }

    private suspend fun handleRepeat(event: GroupMessageEvent, chance: Double) {
        if (canRepeat(event.group.id) && event.message[QuoteReply] == null && chance in 0.90..0.91) {
            // 避免复读过多图片刷屏
            val count = event.message.stream().filter { it is Image }.count()

            if (count <= 1L) {
                val msgChain = ArrayList<Message>()

                event.message.forEach { msgChain.add(it) }

                for (i in 0 until msgChain.size) {
                    val chain = msgChain[i]
                    if (chain is PlainText) {
                        msgChain[i] = PlainText(chain.content.replace("我", "你"))
                    }
                }
                event.reply(msgChain.asMessageChain())
            }
        }
    }

    private fun canRepeat(groupId: Long): Boolean {
        val cfg = GroupConfigManager.getConfig(groupId) ?: return true
        return cfg.doRepeat
    }

    override fun getName(): String = "复读机"
}