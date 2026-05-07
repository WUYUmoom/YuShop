package com.wuyumoom.yushop.config

import com.wuyumoom.yucore.api.FileAPI
import com.wuyumoom.yucore.api.Message
import com.wuyumoom.yucore.file.view.ViewConfiguration
import com.wuyumoom.yushop.YuShop
import com.wuyumoom.yushop.api.data.DataManager
import com.wuyumoom.yushop.api.data.StorageType
import com.wuyumoom.yushop.database.DatabaseManager
import com.wuyumoom.yushop.listener.PluginEvent
import com.wuyumoom.yushop.model.Shop
import java.io.File
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration

object ConfigManager {
    var config = YuShop.INSTANCE.config
    var message: Message = Message(config)
    val viewConfigurationMap: MutableMap<String, ViewConfiguration> = HashMap()
    lateinit var buy: ViewConfiguration
    var buyCount: MutableMap<String, Int> = mutableMapOf()
    var buyProduct: Int = 1

    lateinit var sell: ViewConfiguration
    var sellCount: MutableMap<String, Int> = mutableMapOf()
    var sellProduct: Int = 1

    var dataFile: File = File(YuShop.pluginFile, "data")
    var storage_mode: StorageType = StorageType.YML

    /** 商店实列 */
    var shop: MutableMap<String, Shop> = mutableMapOf()

    lateinit var dataLimit: YamlConfiguration
    lateinit var dataFileLimit: File

    var mysql_database: String = config.getString("Storage.mysql.database") ?: "yushop"
    var mysql_host: String = config.getString("Storage.mysql.host") ?: "127.0.0.1"
    var mysql_port: String = config.getString("Storage.mysql.port") ?: "3306"
    var mysql_username: String = config.getString("Storage.mysql.username") ?: "root"
    var mysql_password: String = config.getString("Storage.mysql.password") ?: ""

    fun load() {
        message = Message(config)
        FileAPI.folderFiles(YuShop.INSTANCE, "confirm", YuShop.pluginFile).forEach { file ->
            if (file.name.contains("购买")) {
                val loadConfiguration = YamlConfiguration.loadConfiguration(file)
				buyProduct = loadConfiguration.getInt("product")
                buy = ViewConfiguration(loadConfiguration)
                loadConfiguration.getConfigurationSection("Button")!!.getKeys(false).forEach {
                    val int = loadConfiguration.getInt("Button.${it}.count")
                    if (int > 0) {
                        buyCount[it] = int
                    }
                }
            }
            if (file.name.contains("回收")) {
                val loadConfiguration = YamlConfiguration.loadConfiguration(file)
				sellProduct  = loadConfiguration.getInt("product")
                sell = ViewConfiguration(loadConfiguration)
                loadConfiguration.getConfigurationSection("Button")!!.getKeys(false).forEach {
                    val int = loadConfiguration.getInt("Button.${it}.count")
                    if (int > 0) {
                        sellCount[it] = int
                    }
                }
            }
        }
        storage_mode = StorageType.valueOf(config.getString("Storage.mode")!!)
        DataManager.init(storage_mode)
        FileAPI.folderFiles(YuShop.INSTANCE, "shop", YuShop.pluginFile).forEach { file ->
            val name = file.name.replace(".yml", "")
            val loadConfiguration = YamlConfiguration.loadConfiguration(file)
            viewConfigurationMap[name] = ViewConfiguration(loadConfiguration)
            shop[name] = Shop.create(loadConfiguration, name)
        }
        if (storage_mode == StorageType.YML) {
            dataFileLimit = File(YuShop.INSTANCE.dataFolder, "limit.yml")
            if (dataFileLimit.exists()) {
                YuShop.INSTANCE.saveResource("limit.yml", false)
            }
            dataLimit = YamlConfiguration.loadConfiguration(dataFile)
            if (!dataFile.exists()) {
                dataFile.mkdirs() // 确保目录存在
            }
        } else {
            DatabaseManager.connect()
            if (DatabaseManager.isConnected()) {
                Bukkit.getPluginManager().registerEvents(PluginEvent(), YuShop.INSTANCE)
                YuShop.INSTANCE.server.logger.info("§a数据库连接成功")
            }
        }
    }

    fun reload() {
        shop.clear()
        viewConfigurationMap.clear()
        YuShop.INSTANCE.reloadConfig()
        config = YuShop.INSTANCE.config
        load()
    }
}
