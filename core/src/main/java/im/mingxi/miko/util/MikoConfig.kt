package im.mingxi.miko.util

import com.tencent.mmkv.MMKV

object MikoConfig {
    private val config = MMKV.mmkvWithID("global_config")
    private val cache = MMKV.mmkvWithID("global_cache")

    fun getCacheMap(): HashMap<String, String> {
        val map = HashMap<String, String>()
        cache.allKeys()!!.forEach {
            map[it] = cache.getString(it, "")!!
        }
        return map
    }

    fun getConfigMap(): HashMap<String, String> {
        val map = HashMap<String, String>()
        config.allKeys()!!.forEach {
            map[it] = cache.getString(it, "")!!
        }
        return map
    }
}