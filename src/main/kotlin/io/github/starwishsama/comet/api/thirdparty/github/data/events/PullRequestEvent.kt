/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.github.data.events

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.utils.StringUtil.limitStringSize
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class PullRequestEvent(
    val action: String,
    @JsonProperty("pull_request")
    val pullRequestInfo: PullRequestInfo,
    @JsonProperty("repository")
    val repository: PushEvent.RepoInfo,
    val sender: IssueEvent.SenderInfo
) : GithubEvent {

    data class PullRequestInfo(
        @JsonProperty("html_url")
        val url: String,
        val title: String,
        val body: String,
        @JsonProperty("created_at")
        val createdTime: String,
    ) {
        fun convertCreatedTime(): String {
            val localTime =
                LocalDateTime.parse(createdTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME).atZone(ZoneId.systemDefault())
            return CometVariables.yyMMddPattern.format(localTime)
        }
    }

    override fun toMessageWrapper(): MessageWrapper {
        val wrapper = MessageWrapper()

        wrapper.addText("\uD83D\uDD27 仓库 ${repository.fullName} 有新的提交更改\n")
        wrapper.addText("| ${pullRequestInfo.body}\n")
        wrapper.addText("| 发布时间 ${pullRequestInfo.convertCreatedTime()}\n")
        wrapper.addText("| 发布人 ${sender.login}\n")
        wrapper.addText("| 提交更改信息: \n")
        wrapper.addText("| ${pullRequestInfo.title}\n")
        wrapper.addText("| ${pullRequestInfo.body.limitStringSize(100).trim()}")
        wrapper.addText("| 查看完整信息: ${pullRequestInfo.url}")

        return wrapper
    }

    override fun repoName(): String = repository.fullName

    override fun sendable(): Boolean = action == "opened"
}
