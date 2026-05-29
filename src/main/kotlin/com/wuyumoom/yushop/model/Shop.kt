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
        fun create(config: Configuration,name: String): Shop{
            try {
                val shopTypeStr = config.getString("shop_type") ?: "BUY"
                val valueOf = try {
                    ShopType.valueOf(shopTypeStr)
                } catch (e: IllegalArgumentException) {
                    throw IllegalArgumentException("商店 '$name' 的 shop_type 无效: '$shopTypeStr'，请使用 BUY 或 SELL")
                }

                val weightProduct : MutableMap<Int, MutableList<Product>> = mutableMapOf()
                val button = config.getConfigurationSection("Button")
                    ?: throw IllegalArgumentException("商店 '$name' 缺少 Button 配置段")

                val product : MutableMap<String, Product> = mutableMapOf()
                button.getKeys(false).forEach { key ->
                    try {
                        val maxPrice = button.getInt("$key.max_price")
                        if (maxPrice > 0){
                            val section = button.getConfigurationSection(key)
                                ?: throw IllegalArgumentException("商品 '$key' 的配置段不存在")
                            val create = try {
                                Product.create(section)
                            } catch (e: Exception) {
                                throw IllegalArgumentException("创建商品 '$key' 时出错: ${e.message}", e)
                            }
                            val products = weightProduct[create.weight]
                            product[key] = create
                            if (products == null){
                                weightProduct[create.weight] = mutableListOf(create)
                            }else{
                                products.add(create)
                            }
                        }
                    } catch (e: IllegalArgumentException) {
                        throw e
                    } catch (e: Exception) {
                        throw IllegalArgumentException("处理商品按钮 '$key' 时发生未知错误: ${e.message}", e)
                    }
                }

                val shopSlotStr = config.getString("shop_slot")
                    ?: throw IllegalArgumentException("商店 '$name' 缺少 shop_slot 配置")
                val shopSlot = try {
                    BukkitAPI.onSetInt(shopSlotStr)
                } catch (e: Exception) {
                    throw IllegalArgumentException("商店 '$name' 的 shop_slot 格式错误: '$shopSlotStr'，${e.message}")
                }

                return Shop(name, valueOf, shopSlot, weightProduct, product)
            } catch (e: IllegalArgumentException) {
                throw e
            } catch (e: Exception) {
                throw RuntimeException("创建商店 '$name' 时发生未知错误: ${e.message}", e)
            }
        }
    }
}