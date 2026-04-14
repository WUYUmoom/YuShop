package com.wuyumoom.yushop.config

import com.wuyumoom.yucore.api.FileAPI
import com.wuyumoom.yushop.database.DatabaseManager
import com.wuyumoom.yucore.api.Message
import com.wuyumoom.yucore.file.view.ViewConfiguration
import com.wuyumoom.yushop.YuShop
import com.wuyumoom.yushop.api.data.DataManager
import com.wuyumoom.yushop.api.data.StorageType
import com.wuyumoom.yushop.listener.PluginEvent
import com.wuyumoom.yushop.model.Shop
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object ConfigManager {
    var config = YuShop.INSTANCE.config
    var message: Message = Message(config)
    val viewConfigurationMap: MutableMap<String, ViewConfiguration> = HashMap()
    var dataFile: File = File(YuShop.pluginFile, "data")
    var storage_mode : StorageType = StorageType.YML

    /**
     * 商店实列
     */
    var shop: MutableMap<String, Shop> = mutableMapOf()

    lateinit var dataLimit: YamlConfiguration
    lateinit var dataFileLimit: File


    var mysql_database : String = config.getString("Storage.mysql.database")?:"yushop"
    var mysql_host : String = config.getString("Storage.mysql.host")?:"127.0.0.1"
    var mysql_port : String = config.getString("Storage.mysql.port")?:"3306"
    var mysql_username : String = config.getString("Storage.mysql.username")?:"root"
    var mysql_password : String = config.getString("Storage.mysql.password")?: ""

    fun load() {
        message = Message(config)
        storage_mode = StorageType.valueOf(config.getString("Storage.mode")!!)
        DataManager.init(storage_mode)
        FileAPI.folderFiles(YuShop.INSTANCE, "shop", YuShop.pluginFile).forEach { file ->
            val name = file.name.replace(".yml", "")
            val loadConfiguration = YamlConfiguration.loadConfiguration(file)
            viewConfigurationMap[name] = ViewConfiguration(loadConfiguration)
            shop[name] = Shop.create(loadConfiguration, name)
        }
        if (storage_mode == StorageType.YML){
            dataFileLimit =File(YuShop.INSTANCE.dataFolder, "limit.yml")
            if (dataFileLimit.exists()){
                YuShop.INSTANCE.saveResource("limit.yml", false)
            }
            dataLimit = YamlConfiguration.loadConfiguration(dataFile)
            if (!dataFile.exists()) {
                dataFile.mkdirs() // 确保目录存在
            }
        }else{
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