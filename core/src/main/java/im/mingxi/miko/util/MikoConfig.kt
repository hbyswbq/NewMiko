package im.mingxi.miko.util

import com.tencent.mmkv.MMKV

object MikoConfig {

    private val config = MMKV.mmkvWithID("global_config")
    private val cache = MMKV.mmkvWithID("global_cache")

    fun getCacheMap(): HashMap<String, String> {
        val map = HashMap<String, String>()

        cache.allKeys()?.forEach {
            map[it] = cache.getString(it, "") ?: ""
        }

        return map
    }

    fun getConfigMap(): HashMap<String, String> {
        val map = HashMap<String, String>()

        config.allKeys()?.forEach {
            map[it] = config.getString(it, "") ?: ""
        }

        return map
    }

    var autoTransferEnable: Boolean
        get() = config.decodeBool("auto_transfer_enable", true)
        set(value) {
            config.encode("auto_transfer_enable", value)
        }

    var autoTransferDelay: Int
        get() = config.decodeInt("auto_transfer_delay", 0)
        set(value) {
            config.encode("auto_transfer_delay", value)
        }

    var autoTransferReply: String
        get() = config.decodeString(
            "auto_transfer_reply",
            "已收款，谢谢"
        ) ?: "已收款，谢谢"

        set(value) {
            config.encode("auto_transfer_reply", value)
        }
}
