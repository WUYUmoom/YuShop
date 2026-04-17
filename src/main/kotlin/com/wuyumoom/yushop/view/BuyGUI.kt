package com.wuyumoom.yushop.view

import com.wuyumoom.yucore.api.ItemStackAPI
import com.wuyumoom.yucore.view.GuiSession
import com.wuyumoom.yushop.api.data.DataManager
import com.wuyumoom.yushop.config.ConfigManager
import com.wuyumoom.yushop.model.Product
import com.wuyumoom.yushop.model.Shop
import org.bukkit.entity.Player

class BuyGUI(
    val shop: Shop,
    val product: Product,
    val count: Int
) {
    companion object{

    }
    /**
     * 打开购买界面
     */
    fun open(player: Player) {
        val guiSession = GuiSession(ConfigManager.buy, player)
        guiSession.onClick { event ->
            event.isCancelled = true
            val item = event.currentItem ?: return@onClick
            val nbt = ItemStackAPI.getNBT(item, "yubutton") ?: return@onClick
            val i = ConfigManager.buyCount[nbt]
            if (i != null){
                shop.shopType.execute(product,player, count*i,shop)
                draw(guiSession, player)
            }
        }
        draw(guiSession, player)
        guiSession.open()
    }
    fun draw( guiSession: GuiSession, player: Player){
        ConfigManager.buyCount.forEach {(key, value)->
            val button = ConfigManager.buy.button[key]?: return@forEach
            val clone = button.itemStack.clone()
            val itemMeta = clone.itemMeta ?: return@forEach
            itemMeta.setDisplayName(itemMeta.displayName.replace("%name%", product.itemName))
            val originalLore = itemMeta.lore ?: return@forEach
            val data = DataManager.getData(player.name)
            val playerShopData = data.shopData[shop.name] ?: return@forEach
            val i1 = playerShopData.limit.getOrDefault(product.name, 0)
            val newLore = originalLore.map { line ->
                line.replace("%price%", (count*value).toString())
                    .replace("%limit_count%", i1.toString())
                    .replace("%limit_max%", product.limitMax.toString())
            }
            itemMeta.lore = newLore
            clone.itemMeta = itemMeta
            button.slot.forEach { slot ->
                guiSession.inventory.setItem(slot, clone)
            }
        }
    }

}