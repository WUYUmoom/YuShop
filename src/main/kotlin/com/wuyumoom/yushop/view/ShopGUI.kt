package com.wuyumoom.yushop.view

import com.wuyumoom.yucore.api.ItemStackAPI
import com.wuyumoom.yucore.file.view.ViewConfiguration
import com.wuyumoom.yucore.view.GuiSession
import com.wuyumoom.yushop.api.data.DataManager
import com.wuyumoom.yushop.model.Product
import com.wuyumoom.yushop.model.Shop
import com.wuyumoom.yushop.util.getWeightedTask
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.collections.component1
import kotlin.collections.component2

object ShopGUI {

    /**
     * 打开界面
     */
    fun open(viewConfiguration: ViewConfiguration, player: Player, shop: Shop) {
        val guiSession = GuiSession(viewConfiguration, player)
        guiSession.onClick { event ->
            event.isCancelled = true
            val item = event.currentItem ?: return@onClick
            val nbt = ItemStackAPI.getNBT(item, "yubutton") ?: return@onClick
            val product = shop.product[nbt]
            if (product != null) {
                val count = ItemStackAPI.getNBT(item, "yushopcount")?.toInt() ?: return@onClick
//                shop.shopType.execute(product,player, count,shop)
//                draw(player,shop,viewConfiguration,guiSession)
                BuyGUI(shop,product,count).open(player)
            }
        }
        draw(player,shop,viewConfiguration,guiSession)
        guiSession.open()
    }
    fun draw(player: Player,shop: Shop,viewConfiguration: ViewConfiguration,guiSession: GuiSession){
        var index = -1
        val data = DataManager.getData(player.name)
        val playerShopData = data.shopData[shop.name] ?: return
        playerShopData.product.forEach { (name, count) ->
            val button = viewConfiguration.button[name]
            if (button == null) {
                Bukkit.getConsoleSender().sendMessage("[YuShop] 界面配置错误")
                return@forEach
            }
            index++
            val clone = button.itemStack.clone()
            val product = shop.product[name] ?: return@forEach
            val itemStack = ItemStackAPI.setNBT(
                setLore(
                    clone,count,product.getLimit(data, shop),product.limitMax
                ), "yushopcount", count.toString())
            guiSession.inventory.setItem(shop.shopSlot[index], itemStack)
        }
    }

    /**
     * 设置lore显示
     */
    fun setLore(item: ItemStack, count: Int,shopCount: Int,limitMax: Int): ItemStack {
        val clone = item.clone()
        val itemMeta = clone.itemMeta ?: return clone

        val originalLore = itemMeta.lore ?: return clone
        val newLore = originalLore.map { line ->
            line.replace("%price%", count.toString())
                .replace("%limit_count%", shopCount.toString())
                .replace("%limit_max%", limitMax.toString())
        }

        itemMeta.lore = newLore
        clone.itemMeta = itemMeta
        return clone
    }

}