package com.wuyumoom.yushop.model

import com.wuyumoom.yucore.api.BukkitAPI
import com.wuyumoom.yushop.api.ShopType
import org.bukkit.Bukkit
import org.bukkit.configuration.Configuration
import org.bukkit.configuration.ConfigurationSection

class Shop(
    val name: String,
    val shopType:ShopType,
    val shopSlot: IntArray,
    val product: MutableMap<Int, MutableList<Product>>,
) {
    companion object{
        /**
         * 创建一个商店
         */
        fun create(config: Configuration,name: String): Shop{
            val valueOf = ShopType.valueOf(config.getString("shop_type") ?: "BUY")
            val product : MutableMap<Int, MutableList<Product>> = mutableMapOf()
            val button = config.getConfigurationSection("Button")!!
            button.getKeys(false).forEach {
                val maxPrice = button.getInt("$it.max_price")
                if (maxPrice > 0){
                    val create = Product.create(button.getConfigurationSection(it)!!)
                    val products = product[create.weight]
                    if (products == null){
                        product[create.weight] = mutableListOf(create)
                    }else{
                        products.add(create)
                    }
                }
            }
            return Shop(name,valueOf,BukkitAPI.onSetInt(config.getString("shop_slot")),product)
        }
    }
}