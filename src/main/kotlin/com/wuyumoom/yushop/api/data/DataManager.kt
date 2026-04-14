package com.wuyumoom.yushop.api.data

import com.wuyumoom.yucore.api.FileAPI
import com.wuyumoom.yushop.database.DatabaseManager
import com.wuyumoom.yushop.YuShop
import com.wuyumoom.yushop.config.ConfigManager
import com.wuyumoom.yushop.model.Product
import com.wuyumoom.yushop.model.Shop
import com.wuyumoom.yushop.util.getRandomProduct
import org.bukkit.configuration.file.YamlConfiguration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap

enum class StorageType {
    YML {
        override fun save(data: PlayerData) {
            val yml = FileAPI.getPlayer(data.name, YuShop.INSTANCE)
            yml.set("time", data.time)
            data.shopData.forEach { (name, playerShopData) ->
                yml.set("shop.$name.limit",playerShopData.limit)
                yml.set("shop.$name.product",playerShopData.product)
            }
            yml.save(FileAPI.getPlayerFile(data.name, YuShop.INSTANCE))
        }

        override fun load(player: String): PlayerData {
            val yml = FileAPI.getPlayer(player, YuShop.INSTANCE)
            val shopData: MutableMap<String, PlayerShopData> = mutableMapOf()
            val configurationSection = yml.getConfigurationSection("shop")
            if (configurationSection == null){
                val playerData = PlayerData(
                    player,
                    yml.getString("time") ?: "2021-01-01 00:00:00",
                    shopData
                )
                playerData.updateProduct()
                return playerData
            }else{
                configurationSection.getKeys(false).forEach { key ->
                    shopData[ key] = PlayerShopData.create(configurationSection.getConfigurationSection(key)!!)
                }
                val playerData = PlayerData(player, yml.getString("time") ?: "2021-01-01 00:00:00", shopData)
                playerData.updateProduct()
                return playerData
            }
        }

        override fun getLimit(
            product: String,
            shop: String
        ) : Int{
            return ConfigManager.dataLimit.getInt("${shop}.${product}")
        }

        override fun updateProduct(product: String, shop: String) {
            val limit = getLimit(product, shop)
            ConfigManager.dataLimit.set("${shop}.${product}",limit+1)
        }

        override fun upServerLimit() {
            ConfigManager.dataLimit = YamlConfiguration()
            ConfigManager.dataLimit.save(ConfigManager.dataFileLimit)
        }
    },
    MYSQL {
        override fun save(data: PlayerData) {
            DatabaseManager.savePlayerData(data)
        }

        override fun load(player: String): PlayerData {
            val playerData = DatabaseManager.loadPlayerData(player)
            playerData.updateProduct()
            return playerData
        }

        override fun getLimit(
            product: String,
            shop: String
        ): Int {
            return DatabaseManager.getServerLimit(shop, product)
        }

        override fun updateProduct(product: String, shop: String) {
            DatabaseManager.updateServerLimit(shop, product)
        }

        override fun upServerLimit() {
            DatabaseManager.resetServerLimit()
        }
    };

    abstract fun save(data: PlayerData)
    abstract fun load(player: String): PlayerData
    abstract fun getLimit(product: String,shop: String): Int
    abstract fun updateProduct(product: String,shop: String)
    abstract fun upServerLimit()
}


object DataManager {
    private val cache: ConcurrentHashMap<String, PlayerData> = ConcurrentHashMap()
    private var storageType: StorageType = StorageType.YML
    /**
     * 初始化数据管理器
     */
    fun init(storageType: StorageType) {
        this.storageType = storageType
    }
    /**
     * 获取或创建玩家数据（带缓存）
     */
    fun getData(player: String): PlayerData {
        return cache.getOrPut(player) {
            storageType.load(player)
        }
    }

    /**
     * 更新服务器限制次数
     */
    fun updateServerLimit(product: String,shop: String) {
        storageType.updateProduct(product,shop)
    }

    /**
     * 获取服务器限制次数
     */
    fun getServerLimit(product: Product,shop: Shop): Int{
        return storageType.getLimit(product.name,shop.name)
    }
    /**
     * 保存玩家数据
     */
    fun saveData(player: String) {
        val data = cache[player]?: return
        storageType.save(data)
    }


    /**
     * 保存玩家数据
     */
    fun saveAllData() {
        cache.values.forEach { data ->
            storageType.save(data)
        }
    }
    /**
     * 刷新服务器限制次数
     */
    fun upDateLimit() {
        storageType.upServerLimit()
    }
}

data class PlayerData(
    var name: String,
    var time: String,
    val shopData: MutableMap<String, PlayerShopData>
) {
    companion object {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }


    /**
     * 更新商品
     */
    fun updateProduct() {
        if (!isTimePassed()) {
            return
        }
        shopData.clear()
        ConfigManager.shop.forEach { (name, shop) ->
            var limit: MutableMap<String, Int> = mutableMapOf()
            var products: MutableMap<String, Int> = mutableMapOf()
            getRandomProduct(shop).forEach { product ->
                products[product.name] = product.setPrice()
            }
            shopData[name] = PlayerShopData(limit, products)
        }
        time = LocalDateTime.now().format(formatter)
    }


    /**
     * 判断是否超过冷却时间
     */
    fun isTimePassed(): Boolean {
        // 当前时间
        val now = LocalDateTime.now()
        // 记录时间
        val localDateTime = LocalDateTime.parse(time, formatter)
        val plusDays = localDateTime.plusDays(1)
        val targetTime = plusDays.toLocalDate().atStartOfDay().plusMinutes(1)
        return now.isAfter(targetTime)
    }
}