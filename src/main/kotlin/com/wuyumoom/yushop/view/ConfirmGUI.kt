package com.wuyumoom.yushop.view

import com.wuyumoom.yucore.api.ItemStackAPI
import com.wuyumoom.yucore.file.view.ViewConfiguration
import com.wuyumoom.yucore.view.GuiSession
import com.wuyumoom.yushop.api.data.DataManager
import com.wuyumoom.yushop.config.ConfigManager
import com.wuyumoom.yushop.model.Product
import com.wuyumoom.yushop.model.Shop
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ConfirmGUI(val shop: Shop, val product: Product, val count: Int,val item:ItemStack,gui:GuiSession) {
    /** 打开购买界面 */
    fun open(
            player: Player,
            view: ViewConfiguration,
            buyCount: MutableMap<String, Int>,
            productSlot: Int
    ) {
        val guiSession = GuiSession(view, player)
        guiSession.onClick { event ->
            event.isCancelled = true
            val item = event.currentItem ?: return@onClick
            val nbt = ItemStackAPI.getNBT(item, "yubutton") ?: return@onClick
            if(nbt == "返回"){
				guiSession.open()
				return@onClick
			}
			val i = ConfigManager.buyCount[nbt]
            if (i != null) {
                shop.shopType.execute(product, player, count * i, shop)
                draw(guiSession, player, view, buyCount, productSlot)
				return@onClick
            }

        }
        draw(guiSession, player, view, buyCount, productSlot)
        guiSession.open()
    }
    fun draw(
            guiSession: GuiSession,
            player: Player,
            view: ViewConfiguration,
            buyCount: MutableMap<String, Int>,
            productSlot: Int
    ) {
		guiSession.inventory.setItem(productSlot, item)	
        val data = DataManager.getData(player.name)
        buyCount.forEach { (key, value) ->
            val button = view.button[key] ?: return@forEach
            val clone = button.itemStack.clone()
            val itemMeta = clone.itemMeta ?: return@forEach
            itemMeta.setDisplayName(itemMeta.displayName.replace("%name%", product.itemName))
            val originalLore = itemMeta.lore ?: return@forEach
            val playerShopData = data.shopData[shop.name] ?: return@forEach
            val i1 = playerShopData.limit.getOrDefault(product.name, 0)
            val newLore =
                    originalLore.map { line ->
                        line.replace("%price%", (count * value).toString())
                                .replace("%limit_count%", i1.toString())
                                .replace("%limit_max%", product.limitMax.toString())
                    }
            itemMeta.lore = newLore
            clone.itemMeta = itemMeta
            button.slot.forEach { slot -> guiSession.inventory.setItem(slot, clone) }
        }
    }
}
