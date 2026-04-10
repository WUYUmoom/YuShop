package com.wuyumoom.yushop.config

import com.wuyumoom.yucore.api.FileAPI
import com.wuyumoom.yucore.api.Message
import com.wuyumoom.yucore.file.view.ViewConfiguration
import com.wuyumoom.yushop.YuShop
import com.wuyumoom.yushop.model.Shop
import org.bukkit.configuration.file.YamlConfiguration

object ConfigManager {
    var config = YuShop.INSTANCE.config
    var message: Message = Message(config)
    val viewConfigurationMap: MutableMap<String, ViewConfiguration> = HashMap()

    /**
     * 商店实列
     */
    var shop: MutableMap<String, Shop> = mutableMapOf()


    fun load() {
        message = Message(config)
        FileAPI.folderFiles(YuShop.INSTANCE, "shop", YuShop.pluginFile).forEach { file ->
            val name = file.name.replace(".yml", "")
            val loadConfiguration = YamlConfiguration.loadConfiguration(file)
            viewConfigurationMap[name] = ViewConfiguration(loadConfiguration)
            shop[name] = Shop.create(loadConfiguration, name)
        }
    }

    fun reload() {
        YuShop.INSTANCE.reloadConfig()
        config = YuShop.INSTANCE.config
        load()

    }

}