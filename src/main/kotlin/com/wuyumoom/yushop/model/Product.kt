package com.wuyumoom.yushop.model

import com.wuyumoom.yucore.api.BukkitAPI
import com.wuyumoom.yushop.api.data.PlayerData
import com.wuyumoom.yushop.api.money.IMoney
import com.wuyumoom.yushop.api.type.ShopLimit
import com.wuyumoom.yushop.model.money.Nye
import com.wuyumoom.yushop.model.money.Points
import com.wuyumoom.yushop.model.money.Vault
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack
import java.lang.Math.random
import kotlin.math.pow

class Product(
    val name: String,
    val itemName: String,
    val command: MutableList<String>,
    val limit: ShopLimit,
    val limitMax: Int,
    val item: ItemStack,
    val minPrice: Int,
    val maxPrice: Int,
    val currency: IMoney,
    val priceProbability: Int,
    val weight: Int,
) {

    /**
     * 获取可购买次数
     */
    fun getLimit(playerData: PlayerData, shop: Shop): Int {
        return limit.getLimit(playerData, shop, this)
    }

    /**
     * 设置商品价格
     */
    fun setPrice(): Int {
        val priceRange = maxPrice - minPrice
        if (priceRange <= 0) return minPrice

        val probability = priceProbability.coerceIn(0, 100) / 100.0

        val randomFactor = random().pow(1.0 / (probability * 9 + 1))

        return minPrice + (priceRange * randomFactor).toInt()
    }


    companion object {
        /**
         * 获取货币
         */
        private fun getMoney(configurationSection: ConfigurationSection): IMoney {
            val currency = configurationSection.getString("currency") ?: "Vault"
            return when (currency) {
                "Points" -> {
                    Points()
                }
                "Nye" -> {
                    Nye(configurationSection.getString("nye") ?: "测试")
                }

                "Vault" -> {
                    Vault()
                }
                else -> {
                    throw IllegalArgumentException("Invalid currency: $currency")
                }
            }
        }

        /**
         * 创建实例
         */
        fun create(configurationSection: ConfigurationSection): Product {
            val productName = configurationSection.name

            try {
                val itemName = BukkitAPI.onReplace(configurationSection.getString("name") ?: "未知")

                val command = configurationSection.getStringList("command")
                if (command.isEmpty()) {
                    throw IllegalArgumentException("商品 '$productName' 的 command 列表不能为空")
                }

                val limitTypeStr = configurationSection.getString("limit.type") ?: "Personal"
                val shopLimit = try {
                    ShopLimit.valueOf(limitTypeStr)
                } catch (e: IllegalArgumentException) {
                    throw IllegalArgumentException("商品 '$productName' 的 limit.type 无效: '$limitTypeStr'，请使用 Personal 或 Server")
                }

                val limitMax = configurationSection.getInt("limit.max")
                if (limitMax < 0) {
                    throw IllegalArgumentException("商品 '$productName' 的 limit.max 不能为负数: $limitMax")
                }

                val minPrice = configurationSection.getInt("min_price")
                val maxPrice = configurationSection.getInt("max_price")

                if (minPrice < 0) {
                    throw IllegalArgumentException("商品 '$productName' 的 min_price 不能为负数: $minPrice")
                }
                if (maxPrice < 0) {
                    throw IllegalArgumentException("商品 '$productName' 的 max_price 不能为负数: $maxPrice")
                }
                if (minPrice > maxPrice) {
                    throw IllegalArgumentException("商品 '$productName' 的 min_price ($minPrice) 不能大于 max_price ($maxPrice)")
                }

                val priceProbability = configurationSection.getInt("price_probability")
                if (priceProbability !in 0..100) {
                    throw IllegalArgumentException("商品 '$productName' 的 price_probability 必须在 0-100 之间: $priceProbability")
                }

                val weight = configurationSection.getInt("weight")
                if (weight <= 0) {
                    throw IllegalArgumentException("商品 '$productName' 的 weight 必须大于 0: $weight")
                }

                val money = getMoney(configurationSection)

                val materialStr = configurationSection.getString("id") ?: "STONE"
                val material = Material.getMaterial(materialStr)
                    ?: throw IllegalArgumentException("商品 '$productName' 的物品 ID 无效: '$materialStr'，请使用有效的 Minecraft 物品 ID（如 DIAMOND、IRON_INGOT 等）")

                return Product(
                    productName,
                    itemName,
                    command,
                    shopLimit,
                    limitMax,
                    ItemStack(material),
                    minPrice,
                    maxPrice,
                    money,
                    priceProbability,
                    weight
                )
            } catch (e: IllegalArgumentException) {
                throw e
            } catch (e: Exception) {
                val causeMessage = e.cause?.message ?: "未知错误"
                throw RuntimeException("创建商品 '$productName' 时发生错误: $causeMessage", e)
            }
        }
    }
}
