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
                    return Points()
                }
                "Nye" -> {
                    return Nye(configurationSection.getString("nye") ?: "测试")
                }

                "Vault" -> {
                    return Vault()
                }

                else -> {
                    throw IllegalArgumentException("Invalid currency: $currency")
                }
            }
        }

        /**
         * 创建实列
         */
        fun create(configurationSection: ConfigurationSection): Product {
            val name = configurationSection.name
            val itemName = BukkitAPI.onReplace(configurationSection.getString("name") ?: name)
            val command = configurationSection.getStringList("command")
            val shopLimit = ShopLimit.valueOf(configurationSection.getString("limit.type") ?: "Personal")
            val limitMax = configurationSection.getInt("limit.max")
            val minPrice = configurationSection.getInt("min_price")
            val maxPrice = configurationSection.getInt("max_price")
            val priceProbability = configurationSection.getInt("price_probability")
            val weight = configurationSection.getInt("weight")
            val money = getMoney(configurationSection)
            return Product(
                name,
                itemName,
                command,
                shopLimit,
                limitMax,
                ItemStack(Material.getMaterial(configurationSection.getString("id") ?: "STONE")!!),
                minPrice,
                maxPrice,
                money,
                priceProbability,
                weight
            )
        }
    }
}