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
        override fun execute(product: Product,player: Player, count: Int,shop: Shop) {
            // 判断经济
            if (!product.currency.hasEnough(player,count)){
                ConfigManager.message.sendMessage("no_enough",player)
                return
            }
            val data = DataManager.getData(player.name)
            // 判断次数
            val limit = product.getLimit(data, shop)
            if (limit >= product.limitMax){
                ConfigManager.message.sendMessage("no_limit",player)
                return
            }
            product.currency.take(player,count)
            product.command.forEach {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),it.replace("%player%",player.name))
            }
            product.limit.updateProduct(data,product.name,shop)
        }
    },
    SELL {
        override fun execute(product: Product,player: Player, count: Int,shop: Shop) {
            if (!hasItemInInventory(player, product.item)) {
                ConfigManager.message.sendMessage("no_item",player)
                return
            }
            val data = DataManager.getData(player.name)
            // 判断次数
            val limit = product.getLimit(data, shop)
            if (limit >= product.limitMax){
                ConfigManager.message.sendMessage("no_limit",player)
                return
            }
            product.currency.give(player,count)
            product.limit.updateProduct(data,product.name,shop)
            removeItemFromInventory(player, product.item)
        }
    };
    abstract fun execute(product: Product,player: Player, count: Int,shop: Shop)
}