package com.wuyumoom.yushop.view

import com.wuyumoom.yucore.api.ItemStackAPI
import com.wuyumoom.yucore.file.view.ViewConfiguration
import com.wuyumoom.yucore.view.GuiSession
import com.wuyumoom.yushop.api.data.DataManager
import com.wuyumoom.yushop.api.data.PlayerData
import com.wuyumoom.yushop.api.type.ShopType
import com.wuyumoom.yushop.model.Product
import com.wuyumoom.yushop.model.Shop
import com.wuyumoom.yushop.util.getWeightedTask
import com.wuyumoom.yushop.util.hasItemInInventory
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.collections.component1
import kotlin.collections.component2

class ShopGUI() {
    var product: Product? = null
    var count: Int = 1
    var price = 0

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
                this.product = product
                setBuyButton(viewConfiguration,guiSession)
                price  = ItemStackAPI.getNBT(item, "yushopcount")?.toInt() ?: return@onClick
//                shop.shopType.execute(product,player, count,shop)
//                draw(player,shop,viewConfiguration,guiSession)
                //BuyGUI(shop,product,count).open(player)
            }else{
                when(nbt){
                    "购买加1"->{
                        count++
                        setBuyButton(viewConfiguration,guiSession)
                        draw(player,shop,viewConfiguration,guiSession)
                    }
                    "购买减1"->{
                        if (count <= 1){
                            count = 1
                            return@onClick
                        }
                        count--
                        setBuyButton(viewConfiguration,guiSession)
                    }
                    "购买加10"->{
                        count += 10
                        setBuyButton(viewConfiguration,guiSession)
                    }
                    "购买减10"->{
                        if (count <= 10){
                            count = 1
                            return@onClick
                        }
                        count -= 10
                        setBuyButton(viewConfiguration,guiSession)
                    }
                    "确定购买"->{
                        val currentProduct = this.product
                        if (currentProduct == null){
                            return@onClick
                        }
                        shop.shopType.execute(currentProduct,player, price,shop,count)
                        draw(player,shop,viewConfiguration,guiSession)
                    }
                    "购买全部"->{
                        if (shop.shopType == ShopType.BUY){
                            val currentProduct = this.product
                            if (currentProduct == null){
                                return@onClick
                            }
                            val data = DataManager.getData(player.name)
                            val limit = currentProduct.getLimit(data, shop)
                            count = currentProduct.currency.getCanBuyCount(player,price)
                            if (count >= limit){
                                count = limit
                            }
                            setBuyButton(viewConfiguration,guiSession)
                        }
                        if (shop.shopType == ShopType.SELL){
                            val currentProduct = this.product
                            if (currentProduct == null){
                                return@onClick
                            }
                            val data = DataManager.getData(player.name)
                            val limit = currentProduct.getLimit(data, shop)
                            val hasItemInInventory = hasItemInInventory(player, currentProduct.item, count)
                            count = if (hasItemInInventory <= limit){
                                hasItemInInventory
                            }else{
                                limit
                            }
                            setBuyButton(viewConfiguration,guiSession)
                        }
                    }
                }
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
    private fun setBuyButton(viewConfiguration: ViewConfiguration,guiSession: GuiSession) {
        val button = viewConfiguration.button["确定购买"]?: return
        val itemStack = button.itemStack.clone()
        val itemMeta = itemStack.itemMeta ?: return
        itemMeta.setDisplayName(itemMeta.displayName.replace("%count%", count.toString()))
        itemStack.itemMeta = itemMeta
        button.slot.forEach {
            guiSession.inventory.setItem(it, itemStack)
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