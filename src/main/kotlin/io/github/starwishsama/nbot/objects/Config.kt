package io.github.starwishsama.nbot.objects

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.nbot.enums.MusicApi


open class Config {
    @SerializedName("botId")
    var botId: Long = 0

    @SerializedName("botPassword")
    var botPassword: String = "password"
    @SerializedName("auto_save_config_time")
    var autoSaveTime : Int = 15
    @SerializedName("rcon_url")
    var rConUrl: String? = null
    @SerializedName("rcon_port")
    var rConPort: Int = 0
    @SerializedName("rcon_password")
    var rConPassword: String? = null
    @SerializedName("netease_api")
    var netEaseApi: String = "http://localhost:3000"
    @SerializedName("cool_down_time")
    var coolDownTime: Int = 7
    @SerializedName("filter_words")
    lateinit var filterWords: Array<String>
    @SerializedName("default_music_api")
    var musicApi = MusicApi.QQ
    @SerializedName("bilibili_password")
    var userPassword: String? = null
    @SerializedName("command_prefix")
    var commandPrefix: List<String> = mutableListOf(".", "。", "#", "!", "/")

    @SerializedName("bili_user_name")
    var biliUserName: String? = null

    @SerializedName("bili_password")
    var biliPassword: String? = null

    @SerializedName("owner_id")
    var ownerId: Long = 0L

    @SerializedName("saucenao_api_key")
    var saucenaoApiKey = "noKey"

    @SerializedName("r6tab_apikey")
    var r6tabKey = "noKey"

    @SerializedName("universal_subs")
    var subList = mutableListOf<Long>()

    @SerializedName("push_groups")
    var pushGroups = mutableListOf<Long>()

    @SerializedName("check_delay")
    var checkDelay: Long = 1

    @SerializedName("twitter_token")
    var twitterToken: String? = null

    @SerializedName("twitter_secret")
    var twitterSecret: String? = null

    @SerializedName("proxy_url")
    var proxyUrl: String? = null

    @SerializedName("proxy_port")
    var proxyPort: Int = 0
}