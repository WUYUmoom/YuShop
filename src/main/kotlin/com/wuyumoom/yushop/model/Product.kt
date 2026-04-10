package com.wuyumoom.yushop.model

import com.wuyumoom.yushop.api.ShopLimit
import org.bukkit.configuration.ConfigurationSection

class Product(
    val name: String,
    val command: MutableList<String>,
    val limit: ShopLimit,
    val limitMax: Int,
    val minPrice: Int,
    val maxPrice: Int,
    val priceProbability: Int,
    val weight: Int,
) {
    companion object{
        /**
         *
         */
        fun create(configurationSection: ConfigurationSection): Product{
            val name = configurationSection.name
            val command = configurationSection.getStringList("command")
            val shopLimit = ShopLimit.valueOf(configurationSection.getString("limit.type")?: "Personal")
            val limitMax = configurationSection.getInt("limit.max")
            val minPrice = configurationSection.getInt("min_price")
            val maxPrice = configurationSection.getInt("max_price")
            val priceProbability = configurationSection.getInt("price_probability")
            val weight = configurationSection.getInt("weight")
            return Product(name,command,shopLimit,limitMax,minPrice,maxPrice,priceProbability,weight)
        }
    }
}