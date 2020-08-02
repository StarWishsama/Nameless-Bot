package io.github.starwishsama.comet.api.twitter

import cn.hutool.http.ContentType
import cn.hutool.http.HttpException
import cn.hutool.http.HttpResponse
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.roxstudio.utils.CUrl
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.gson
import io.github.starwishsama.comet.api.ApiExecutor
import io.github.starwishsama.comet.exceptions.EmptyTweetException
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.exceptions.TwitterApiException
import io.github.starwishsama.comet.objects.pojo.twitter.Tweet
import io.github.starwishsama.comet.objects.pojo.twitter.TwitterErrorInfo
import io.github.starwishsama.comet.objects.pojo.twitter.TwitterUser
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.NetUtil
import io.github.starwishsama.comet.utils.isType
import java.io.IOException
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

/**
 * Twitter API
 *
 * 支持获取蓝鸟用户信息 & 最新推文
 * @author Nameless
 */
object TwitterApi : ApiExecutor {
    // 蓝鸟 API 地址
    private const val twitterApiUrl = "https://api.twitter.com/1.1/"

    // 使用 curl 获取 token, 请
    private const val twitterTokenGetUrl = "https://api.twitter.com/oauth2/token"

    // Bearer Token
    var token: String? = BotVariables.cache["token"].asString

    private var cacheTweet = mutableMapOf<String, LinkedList<Tweet>>()

    // Api 调用次数
    override var usedTime: Int = 0

    private const val apiReachLimit = "已达到 Twitter API调用上限"

    fun getBearerToken() {
        try {
            val curl = CUrl(twitterTokenGetUrl).opt(
                    "-u",
                    "${BotVariables.cfg.twitterToken}:${BotVariables.cfg.twitterSecret}",
                    "--data",
                    "grant_type=client_credentials"
            )

            if (BotVariables.cfg.proxyUrl.isNotEmpty() && BotVariables.cfg.proxyPort != -1) {
                curl.proxy(BotVariables.cfg.proxyUrl, BotVariables.cfg.proxyPort)
            }

            val result = curl.exec("UTF-8")

            if (JsonParser.parseString(result).isJsonObject) {
                // Get Token
                token = JsonParser.parseString(result).asJsonObject["access_token"].asString
                BotVariables.logger.debug("[蓝鸟] 成功获取 Access Token")
            }
        } catch (e: IOException) {
            BotVariables.logger.warning("获取 Token 时出现问题", e)
        }
    }

    @Throws(RateLimitException::class, TwitterApiException::class)
    fun getUserInfo(username: String): TwitterUser? {
        if (isReachLimit()) {
            throw RateLimitException(apiReachLimit)
        }

        usedTime++

        val startTime = LocalDateTime.now()
        val url = "$twitterApiUrl/users/show.json?screen_name=$username&tweet_mode=extended"
        val conn = NetUtil.doHttpRequestGet(url, 12_000)
                .header("authorization", "Bearer $token")

        var result: HttpResponse? = null
        try {
            result = conn.executeAsync()
        } catch (e: HttpException) {
            BotVariables.logger.warning("[蓝鸟] 在获取用户信息时出现了问题", e)
        }

        var tUser: TwitterUser? = null
        val body = result?.body()

        if (body != null) {
            try {
                tUser = gson.fromJson(result?.body(), TwitterUser::class.java)
            } catch (e: JsonSyntaxException) {
                try {
                    val errorInfo = gson.fromJson(result?.body(), TwitterErrorInfo::class.java)
                    BotVariables.logger.warning("[蓝鸟] 调用 API 时出现了问题\n${errorInfo.getReason()}")
                    throw TwitterApiException(errorInfo.errors[0].code, errorInfo.errors[0].reason)
                } catch (e: JsonSyntaxException) {
                    BotVariables.logger.warning("[蓝鸟] 解析推文 JSON 时出现问题: 不支持的类型", e)
                    FileUtil.createErrorReportFile("twitter", e, body, url)
                }
            }
        }

        BotVariables.logger.debug("[蓝鸟] 查询用户信息耗时 ${Duration.between(startTime, LocalDateTime.now()).toMillis()}ms")
        return tUser
    }

    @Throws(RateLimitException::class, EmptyTweetException::class, TwitterApiException::class)
    fun getUserLatestTweet(username: String): Tweet? {
        if (isReachLimit()) {
            throw RateLimitException(apiReachLimit)
        }

        usedTime++
        val request =
                NetUtil.doHttpRequestGet(
                        "$twitterApiUrl/statuses/user_timeline.json?screen_name=$username&count=2&tweet_mode=extended",
                        5_000
                ).header("authorization", "Bearer $token")

        val result = request.execute()

        if (result.isOk && result.isType(ContentType.JSON.value)) {
            return parseJsonToTweet(result.body())
        }

        return null
    }

    @Throws(RateLimitException::class)
    fun getTweetById(id: Long): Tweet? {
        if (isReachLimit()) {
            throw RateLimitException(apiReachLimit)
        }

        val request = NetUtil.doHttpRequestGet("$twitterApiUrl/statuses/show.json?id=$id&tweet_mode=extended", 5_000).header("authorization", "Bearer $token")
        val response = request.executeAsync()

        if (response.isOk && response.isType(ContentType.JSON.value)) {
            return parseJsonToTweet(response.body())
        }

        return null
    }

    @Throws(RateLimitException::class)
    fun getTweetWithCache(username: String): Tweet? {
        val startTime = LocalDateTime.now()

        try {
            var cacheTweets = cacheTweet[username]
            val result: Tweet?

            if (cacheTweets == null) {
                cacheTweets = LinkedList()
            }

            result = if (cacheTweets.isNotEmpty() && Duration.between(cacheTweets[0].getSentTime(), LocalDateTime.now())
                    .toMinutes() <= 1
            ) {
                cacheTweets[0]
            } else {
                getUserLatestTweet(username)
            }

            BotVariables.logger.debug(
                "[蓝鸟] 查询用户最新推文耗时 ${Duration.between(startTime, LocalDateTime.now()).toMillis()}ms"
            )
            return result
        } catch (x: TwitterApiException) {
            BotVariables.logger.warning("[蓝鸟] 调用 API 时出现了问题\n错误代码: ${x.code}\n理由: ${x.reason}}")
        }
        return null
    }

    @Synchronized
    fun addCacheTweet(username: String, tweet: Tweet) {
        if (!cacheTweet.containsKey(username)) {
            cacheTweet[username] = LinkedList()
        } else {
            cacheTweet[username]?.add(tweet)
        }
    }

    private fun parseJsonToTweet(json: String): Tweet? {
        var tweet: Tweet? = null
        try {
            val tweets = gson.fromJson(
                    json,
                    object : TypeToken<List<Tweet>>() {}.type
            ) as List<Tweet>

            if (tweets.isEmpty()) {
                throw EmptyTweetException()
            }
            tweet = tweets[0]
            addCacheTweet(tweet.user.name, tweet)
        } catch (e: JsonSyntaxException) {
            try {
                val errorInfo = gson.fromJson(json, TwitterErrorInfo::class.java)
                BotVariables.logger.warning("[蓝鸟] 调用 API 时出现了问题\n${errorInfo}")
                throw TwitterApiException(errorInfo.errors[0].code, errorInfo.errors[0].reason)
            } catch (e: JsonSyntaxException) {
                BotVariables.logger.warning("[蓝鸟] 解析推文 JSON 时出现问题: 不支持的类型", e)
            }
        }
        return tweet
    }

    override fun isReachLimit(): Boolean {
        return usedTime >= getLimitTime()
    }

    /** Twitter API: 1500次/15min, 10w次/24h */
    override fun getLimitTime(): Int = 1000
}