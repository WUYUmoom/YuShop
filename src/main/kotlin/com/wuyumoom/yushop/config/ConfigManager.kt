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

    lateinit var sell: ViewConfiguration
    var sellCount: MutableMap<String, Int> = mutableMapOf()

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
        try {
            message = Message(config)

            // 加载确认界面配置
            FileAPI.folderFiles(YuShop.INSTANCE, "confirm", YuShop.pluginFile).forEach { file ->
                try {
                    if (file.name.contains("购买")) {
                        val loadConfiguration = YamlConfiguration.loadConfiguration(file)
                        buy = ViewConfiguration(loadConfiguration)

                        // 安全地获取按钮配置
                        val buttonSection = loadConfiguration.getConfigurationSection("Button")
                        if (buttonSection != null) {
                            buttonSection.getKeys(false).forEach { key ->
                                try {
                                    val int = loadConfiguration.getInt("Button.${key}.count")
                                    if (int > 0) {
                                        buyCount[key] = int
                                    }
                                } catch (e: Exception) {
                                    YuShop.INSTANCE.server.logger.warning("§c加载购买配置按钮 '$key' 时出错: ${e.message}")
                                }
                            }
                        } else {
                            YuShop.INSTANCE.server.logger.warning("§c购买配置文件缺少 Button 节点: ${file.name}")
                        }
                    }
                    if (file.name.contains("回收")) {
                        val loadConfiguration = YamlConfiguration.loadConfiguration(file)
                        sell = ViewConfiguration(loadConfiguration)

                        // 安全地获取按钮配置
                        val buttonSection = loadConfiguration.getConfigurationSection("Button")
                        if (buttonSection != null) {
                            buttonSection.getKeys(false).forEach { key ->
                                try {
                                    val int = loadConfiguration.getInt("Button.${key}.count")
                                    if (int > 0) {
                                        sellCount[key] = int
                                    }
                                } catch (e: Exception) {
                                    YuShop.INSTANCE.server.logger.warning("§c加载回收配置按钮 '$key' 时出错: ${e.message}")
                                }
                            }
                        } else {
                            YuShop.INSTANCE.server.logger.warning("§c回收配置文件缺少 Button 节点: ${file.name}")
                        }
                    }
                } catch (e: Exception) {
                    YuShop.INSTANCE.server.logger.warning("§c加载确认配置文件时出错 [${file.name}]: ${e.message}")
                }
            }

            // 加载存储模式
            try {
                storage_mode = StorageType.valueOf(config.getString("Storage.mode") ?: "YML")
                DataManager.init(storage_mode)
            } catch (e: Exception) {
                YuShop.INSTANCE.server.logger.severe("§c存储模式配置错误，使用默认值 YML: ${e.message}")
                storage_mode = StorageType.YML
                DataManager.init(storage_mode)
            }

            // 初始化数据存储
            if (storage_mode == StorageType.YML) {
                try {
                    dataFileLimit = File(YuShop.INSTANCE.dataFolder, "limit.yml")
                    if (!dataFileLimit.exists()) {
                        YuShop.INSTANCE.saveResource("limit.yml", true)
                    }
                    dataLimit = YamlConfiguration.loadConfiguration(dataFileLimit)
                    if (!dataFileLimit.parentFile.exists()) {
                        dataFileLimit.parentFile.mkdirs()
                    }
                } catch (e: Exception) {
                    YuShop.INSTANCE.server.logger.severe("§c初始化 YAML 数据存储时出错: ${e.message}")
                }
            } else {
                try {
                    DatabaseManager.connect()
                    if (DatabaseManager.isConnected()) {
                        YuShop.INSTANCE.server.logger.info("§a数据库连接成功")
                    } else {
                        YuShop.INSTANCE.server.logger.warning("§c数据库连接失败，请检查配置")
                    }
                } catch (e: Exception) {
                    YuShop.INSTANCE.server.logger.severe("§c数据库连接时发生异常: ${e.message}")
                }
            }
        } catch (e: Exception) {
            YuShop.INSTANCE.server.logger.severe("§c配置加载过程中发生严重错误: ${e.message}")
            e.printStackTrace()
        }
        // 加载商店配置
        FileAPI.folderFiles(YuShop.INSTANCE, "shop", YuShop.pluginFile).forEach { file ->
            try {
                val name = file.name.replace(".yml", "")
                val loadConfiguration = YamlConfiguration.loadConfiguration(file)

                // 验证必要的配置节点是否存在
                if (loadConfiguration.getConfigurationSection("Button") == null) {
                    YuShop.INSTANCE.server.logger.warning("§c商店配置文件缺少 Button 节点，已跳过: ${file.name}")
                    return@forEach
                }

                viewConfigurationMap[name] = ViewConfiguration(loadConfiguration)
                shop[name] = Shop.create(loadConfiguration, name)
            } catch (e: Exception) {
                YuShop.INSTANCE.server.logger.warning("§c加载商店配置文件时出错 [${file.name}]: ${e.message}")
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
