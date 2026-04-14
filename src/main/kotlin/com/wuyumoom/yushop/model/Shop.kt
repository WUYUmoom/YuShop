package com.wuyumoom.yushop.model

import com.wuyumoom.yucore.api.BukkitAPI
import com.wuyumoom.yushop.api.type.ShopType
import org.bukkit.configuration.Configuration

class Shop(
    val name: String,
    val shopType:ShopType,
    val shopSlot: IntArray,
    val weightProduct: MutableMap<Int, MutableList<Product>>,
    val product: MutableMap<String, Product>
) {
    companion object{
        /**
         * 创建一个商店
         */
        fun create(config: Configuration,name: String): Shop{
            val valueOf = ShopType.valueOf(config.getString("shop_type") ?: "BUY")
            val weightProduct : MutableMap<Int, MutableList<Product>> = mutableMapOf()
            val button = config.getConfigurationSection("Button")!!
            val product : MutableMap<String, Product> = mutableMapOf()
            button.getKeys(false).forEach {
                val maxPrice = button.getInt("$it.max_price")
                if (maxPrice > 0){
                    val create = Product.create(button.getConfigurationSection(it)!!)
                    val products = weightProduct[create.weight]
                    product[it] = create
                    if (products == null){
                        weightProduct[create.weight] = mutableListOf(create)
                    }else{
                        products.add(create)
                    }
                }
            }
            return Shop(name,valueOf,BukkitAPI.onSetInt(config.getString("shop_slot")),weightProduct,product)
        }
    }
}