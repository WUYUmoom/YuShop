package com.wuyumoom.yushop.database


import com.wuyumoom.yushop.YuShop
import com.wuyumoom.yushop.api.data.PlayerData
import com.wuyumoom.yushop.api.data.PlayerShopData
import com.wuyumoom.yushop.config.ConfigManager
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import kotlin.collections.iterator
import kotlin.use

object DatabaseManager {
    val databaseName: String
        get() = ConfigManager.mysql_database

    private val urlOriginal: String
        get() = "jdbc:mysql://${ConfigManager.mysql_host}:${ConfigManager.mysql_port}?useSSL=false&connectTimeout=5000&socketTimeout=10000&autoReconnect=true&failOverReadOnly=false&characterEncoding=utf8"

    private val url: String
        get() = "jdbc:mysql://${ConfigManager.mysql_host}:${ConfigManager.mysql_port}/$databaseName?useSSL=false&connectTimeout=5000&socketTimeout=10000&autoReconnect=true&failOverReadOnly=false&characterEncoding=utf8"

    private val username: String
        get() = ConfigManager.mysql_username

    private val password: String
        get() = ConfigManager.mysql_password

    @Volatile
    private var connection: Connection? = null

    init {
        if (!createDatabase()) {
            YuShop.INSTANCE.server.logger.warning("无法创建数据库 $databaseName。")
        }
        if (createTables()) {
            YuShop.INSTANCE.server.logger.info("§a数据库表创建/检测成功")
        } else {
            YuShop.INSTANCE.server.logger.warning("无法创建数据库表。")
        }
        if (!connect()) {
            YuShop.INSTANCE.server.logger.warning("无法连接到数据库！")
        }
    }

    /**
     * 创建表
     */
    private fun createTables(): Boolean {
        return try {
            DriverManager.getConnection(url, username, password).use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS `yushop_player_data` (
                            `id` INT AUTO_INCREMENT PRIMARY KEY,
                            `player_name` VARCHAR(36) NOT NULL UNIQUE,
                            `last_update_time` VARCHAR(30) NOT NULL DEFAULT '2021-01-01 00:00:00',
                            `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            INDEX `idx_player_name` (`player_name`)
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                    """.trimIndent())

                    stmt.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS `yushop_shop_data` (
                            `id` INT AUTO_INCREMENT PRIMARY KEY,
                            `player_name` VARCHAR(36) NOT NULL,
                            `shop_name` VARCHAR(50) NOT NULL,
                            `product_name` VARCHAR(50) NOT NULL,
                            `price` INT NOT NULL DEFAULT 0,
                            `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            UNIQUE KEY `unique_player_shop_product` (`player_name`, `shop_name`, `product_name`),
                            INDEX `idx_player_name` (`player_name`),
                            INDEX `idx_shop_name` (`shop_name`),
                            FOREIGN KEY (`player_name`) REFERENCES `yushop_player_data`(`player_name`) ON DELETE CASCADE
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                    """.trimIndent())

                    stmt.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS `yushop_limit_data` (
                            `id` INT AUTO_INCREMENT PRIMARY KEY,
                            `player_name` VARCHAR(36) NOT NULL,
                            `shop_name` VARCHAR(50) NOT NULL,
                            `product_name` VARCHAR(50) NOT NULL,
                            `purchase_count` INT NOT NULL DEFAULT 0,
                            `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            UNIQUE KEY `unique_player_shop_product` (`player_name`, `shop_name`, `product_name`),
                            INDEX `idx_player_name` (`player_name`),
                            INDEX `idx_shop_name` (`shop_name`),
                            FOREIGN KEY (`player_name`) REFERENCES `yushop_player_data`(`player_name`) ON DELETE CASCADE
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                    """.trimIndent())

                    stmt.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS `yushop_server_limit` (
                            `id` INT AUTO_INCREMENT PRIMARY KEY,
                            `shop_name` VARCHAR(50) NOT NULL,
                            `product_name` VARCHAR(50) NOT NULL,
                            `purchase_count` INT NOT NULL DEFAULT 0,
                            `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            UNIQUE KEY `unique_shop_product` (`shop_name`, `product_name`),
                            INDEX `idx_shop_name` (`shop_name`)
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                    """.trimIndent())
                }
            }
            true
        } catch (e: SQLException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 创建库
     */
    private fun createDatabase(): Boolean {
        if (checkDatabaseExists()) return true
        return try {
            DriverManager.getConnection(urlOriginal, username, password).use { conn ->
                conn.prepareStatement("CREATE DATABASE IF NOT EXISTS `$databaseName` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci").use { stmt ->
                    stmt.executeUpdate()
                }
            }
            YuShop.INSTANCE.server.logger.info("§a数据库 §b$databaseName §a创建成功。")
            true
        } catch (e: SQLException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 测试库链接
     */
    private fun checkDatabaseExists(): Boolean {
        return try {
            DriverManager.getConnection(urlOriginal, username, password).use { conn ->
                conn.prepareStatement("SHOW DATABASES LIKE ?").use { stmt ->
                    stmt.setString(1, databaseName)
                    stmt.executeQuery().use { rs -> rs.next() }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            false
        }
    }

    @Synchronized
    fun ensureConnection(): Connection? {
        return try {
            val conn = connection
            if (conn != null && !conn.isClosed && conn.isValid(2)) return conn
            if (!connect()) null else connection
        } catch (e: SQLException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 数据库连接
     */
    fun connect(): Boolean {
        return try {
            val conn = connection
            if (conn != null && !conn.isClosed && conn.isValid(2)) return true

            connection = DriverManager.getConnection(url, username, password)
            true
        } catch (e: SQLException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 是否链接数据库
     */
    fun isConnected(): Boolean {
        return try {
            val conn = connection ?: return false
            !conn.isClosed && conn.isValid(2)
        } catch (e: SQLException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 关闭数据库连接
     */
    fun close() {
        try {
            connection?.let {
                if (!it.isClosed) {
                    it.close()
                }
            }
            connection = null
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    /**
     * 从数据库加载玩家数据
     * @param playerName 玩家名称
     * @return PlayerData 对象，如果玩家不存在则返回默认数据
     */
    fun loadPlayerData(playerName: String): PlayerData {
        val conn = ensureConnection() ?: run {
            YuShop.INSTANCE.server.logger.warning("数据库连接失败，无法加载玩家数据: $playerName")
            return PlayerData(
                playerName,
                "2021-01-01 00:00:00",
                mutableMapOf()
            )
        }

        val shopData: MutableMap<String, PlayerShopData> = mutableMapOf()
        var lastUpdateTime = "2021-01-01 00:00:00"

        try {
            conn.prepareStatement("SELECT `last_update_time` FROM `yushop_player_data` WHERE `player_name` = ?").use { stmt ->
                stmt.setString(1, playerName)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        lastUpdateTime = rs.getString("last_update_time")
                    }
                }
            }

            conn.prepareStatement("SELECT `shop_name`, `product_name`, `price` FROM `yushop_shop_data` WHERE `player_name` = ?").use { stmt ->
                stmt.setString(1, playerName)
                stmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        val shopName = rs.getString("shop_name")
                        val productName = rs.getString("product_name")
                        val price = rs.getInt("price")

                        if (!shopData.containsKey(shopName)) {
                            shopData[shopName] = PlayerShopData(mutableMapOf(), mutableMapOf())
                        }
                        shopData[shopName]!!.product[productName] = price
                    }
                }
            }

            conn.prepareStatement("SELECT `shop_name`, `product_name`, `purchase_count` FROM `yushop_limit_data` WHERE `player_name` = ?").use { stmt ->
                stmt.setString(1, playerName)
                stmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        val shopName = rs.getString("shop_name")
                        val productName = rs.getString("product_name")
                        val count = rs.getInt("purchase_count")

                        if (!shopData.containsKey(shopName)) {
                            shopData[shopName] = PlayerShopData(mutableMapOf(), mutableMapOf())
                        }
                        shopData[shopName]!!.limit[productName] = count
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            YuShop.INSTANCE.server.logger.warning("加载玩家数据时发生错误: $playerName")
        }

        return PlayerData(playerName, lastUpdateTime, shopData)
    }

    /**
     * 保存玩家数据到数据库
     * @param playerData 玩家数据对象
     */
    fun savePlayerData(playerData: PlayerData) {
        val conn = ensureConnection() ?: run {
            YuShop.INSTANCE.server.logger.warning("数据库连接失败，无法保存玩家数据: ${playerData.name}")
            return
        }

        try {
            conn.prepareStatement("""
                INSERT INTO `yushop_player_data` (`player_name`, `last_update_time`) 
                VALUES (?, ?) 
                ON DUPLICATE KEY UPDATE `last_update_time` = ?
            """.trimIndent()).use { stmt ->
                stmt.setString(1, playerData.name)
                stmt.setString(2, playerData.time)
                stmt.setString(3, playerData.time)
                stmt.executeUpdate()
            }

            for ((shopName, shopData) in playerData.shopData) {
                for ((productName, price) in shopData.product) {
                    conn.prepareStatement("""
                        INSERT INTO `yushop_shop_data` (`player_name`, `shop_name`, `product_name`, `price`) 
                        VALUES (?, ?, ?, ?) 
                        ON DUPLICATE KEY UPDATE `price` = ?
                    """.trimIndent()).use { stmt ->
                        stmt.setString(1, playerData.name)
                        stmt.setString(2, shopName)
                        stmt.setString(3, productName)
                        stmt.setInt(4, price)
                        stmt.setInt(5, price)
                        stmt.executeUpdate()
                    }
                }

                for ((productName, count) in shopData.limit) {
                    conn.prepareStatement("""
                        INSERT INTO `yushop_limit_data` (`player_name`, `shop_name`, `product_name`, `purchase_count`) 
                        VALUES (?, ?, ?, ?) 
                        ON DUPLICATE KEY UPDATE `purchase_count` = ?
                    """.trimIndent()).use { stmt ->
                        stmt.setString(1, playerData.name)
                        stmt.setString(2, shopName)
                        stmt.setString(3, productName)
                        stmt.setInt(4, count)
                        stmt.setInt(5, count)
                        stmt.executeUpdate()
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            YuShop.INSTANCE.server.logger.warning("保存玩家数据时发生错误: ${playerData.name}")
        }
    }

    /**
     * 获取服务器限购数量
     * @param shopName 商店名称
     * @param productName 商品名称
     * @return 已购买数量
     */
    fun getServerLimit(shopName: String, productName: String): Int {
        val conn = ensureConnection() ?: return 0

        return try {
            conn.prepareStatement("SELECT `purchase_count` FROM `yushop_server_limit` WHERE `shop_name` = ? AND `product_name` = ?").use { stmt ->
                stmt.setString(1, shopName)
                stmt.setString(2, productName)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) rs.getInt("purchase_count") else 0
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            0
        }
    }

    /**
     * 更新服务器限购数量（增加1）
     * @param shopName 商店名称
     * @param productName 商品名称
     */
    fun updateServerLimit(shopName: String, productName: String) {
        val conn = ensureConnection() ?: return

        try {
            conn.prepareStatement("""
                INSERT INTO `yushop_server_limit` (`shop_name`, `product_name`, `purchase_count`) 
                VALUES (?, ?, 1) 
                ON DUPLICATE KEY UPDATE `purchase_count` = `purchase_count` + 1
            """.trimIndent()).use { stmt ->
                stmt.setString(1, shopName)
                stmt.setString(2, productName)
                stmt.executeUpdate()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    /**
     * 重置服务器限购数据（每日重置时调用）
     */
    fun resetServerLimit() {
        val conn = ensureConnection() ?: return

        try {
            conn.prepareStatement("TRUNCATE TABLE `yushop_server_limit`").use { stmt ->
                stmt.executeUpdate()
            }
            YuShop.INSTANCE.server.logger.info("§a服务器限购数据已重置")
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }
}