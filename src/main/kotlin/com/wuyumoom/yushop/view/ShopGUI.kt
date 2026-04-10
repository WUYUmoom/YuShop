package com.wuyumoom.yushop.view

import com.wuyumoom.yucore.file.view.ViewConfiguration
import com.wuyumoom.yucore.view.GuiSession
import com.wuyumoom.yushop.model.Product
import com.wuyumoom.yushop.model.Shop
import com.wuyumoom.yushop.util.getWeightedTask
import org.bukkit.Bukkit
import org.bukkit.entity.Player
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
        }
        var index = -1
        getRandomProduct(shop).forEach { product ->
            Bukkit.getConsoleSender().sendMessage("正在打开界面: ${product.name}")
            val button = viewConfiguration.button[product.name]
            if (button == null){
                Bukkit.getConsoleSender().sendMessage("未找到商品按钮: ${product.name}")
                return@forEach
            }
            index++
            val i = shop.shopSlot[index]
            Bukkit.getConsoleSender().sendMessage("添加位置$i")
            guiSession.inventory.setItem(i, button.itemStack)
            Bukkit.getConsoleSender().sendMessage("已打开界面: ${product.name}")
        }
        guiSession.open()
    }

    /**
     * 获取随机商品
     */
    fun getRandomProduct(shop: Shop): MutableList<Product> {
        // 预先获取所有可用商品
        val availableTasks = mutableListOf<Product>()
        shop.product.forEach { (weight, tasks) ->
            tasks.forEach { task ->
                availableTasks.add(task)
            }
        }
        // 移除重复商品
        val uniqueAvailableTasks = availableTasks.distinct()
        // 如果可用任务少于所需数量，返回所有可用任务
        if (uniqueAvailableTasks.size <= shop.shopSlot.size) {
            return uniqueAvailableTasks.toMutableList()
        }
        // 使用加权随机选择不重复的任务
        val selectedTasks = mutableSetOf<Product>()
        val validTaskMap = mutableMapOf<Int, MutableList<Product>>()
        // 仅包含可接受的任务到权重映射中
        shop.product.forEach { (weight, tasks) ->
            val validTasks = tasks.filter { task ->
                task !in selectedTasks
            }.toMutableList()
            if (validTasks.isNotEmpty()) {
                validTaskMap[weight] = validTasks
            }
        }
        // 随机选择直到达到所需数量或没有更多任务
        while (selectedTasks.size < shop.shopSlot.size && validTaskMap.isNotEmpty()) {
            try {
                val task = validTaskMap.getWeightedTask()
                if (task !in selectedTasks) {
                    selectedTasks.add(task)
                } else {
                    // 如果选中了已有的任务，继续尝试
                    continue
                }
            } catch (e: IllegalStateException) {
                // 如果权重映射为空，跳出循环
                break
            }
        }

        return selectedTasks.toMutableList()
    }

}