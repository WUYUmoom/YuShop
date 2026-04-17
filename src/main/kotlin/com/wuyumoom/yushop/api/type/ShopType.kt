package com.wuyumoom.yushop.api.type

import com.wuyumoom.yushop.api.data.DataManager
import com.wuyumoom.yushop.config.ConfigManager
import com.wuyumoom.yushop.model.Product
import com.wuyumoom.yushop.model.Shop
import com.wuyumoom.yushop.util.hasItemInInventory
import com.wuyumoom.yushop.util.removeItemFromInventory
import org.bukkit.Bukkit
import org.bukkit.entity.Player

enum class ShopType {
    BUY {
        override fun execute(product: Product,player: Player, count: Int,shop: Shop,executeCount: Int) {
            // 判断经济
            if (!product.currency.hasEnough(player,count,executeCount)){
                ConfigManager.message.sendMessage("no_enough",player)
                return
            }
            val data = DataManager.getData(player.name)
            // 判断次数
            val limit = product.getLimit(data, shop)
            if (limit +executeCount > product.limitMax){
                ConfigManager.message.sendMessage("no_limit",player)
                return
            }
            for (i in 0 until executeCount){
                product.currency.take(player,count)
                product.command.forEach {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),it.replace("%player%",player.name))
                }
            }
            product.limit.updateProduct(data,product.name,shop,executeCount)
        }
    },
    SELL {
        override fun execute(product: Product,player: Player, count: Int,shop: Shop,executeCount: Int) {
            if (hasItemInInventory(player, product.item) >= executeCount) {
                ConfigManager.message.sendMessage("no_item",player)
                return
            }
            val data = DataManager.getData(player.name)
            // 判断次数
            val limit = product.getLimit(data, shop)
            if (limit + executeCount > product.limitMax){
                ConfigManager.message.sendMessage("no_limit",player)
                return
            }
            product.currency.give(player,(count*executeCount))
            product.limit.updateProduct(data,product.name,shop,executeCount)
            removeItemFromInventory(player, product.item,executeCount)
        }
    };
    abstract fun execute(product: Product,player: Player, count: Int,shop: Shop,executeCount: Int= 1)
}