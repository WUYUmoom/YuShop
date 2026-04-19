package com.wuyumoom.yushop.cmd

import com.wuyumoom.yucore.api.NMS
import com.wuyumoom.yushop.YuShop
import com.wuyumoom.yushop.config.ConfigManager
import com.wuyumoom.yushop.view.ShopGUI
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

object Command: TabExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): Boolean {
        if (sender is Player){
            if (args.isNotEmpty()){
                if (args[0]=="item"){
                    val itemInMainHand = sender.inventory.itemInMainHand
                    if (itemInMainHand.type == org.bukkit.Material.AIR){
                        sender.sendMessage("请将物品放入主手")
                        return true
                    }
                    val mnsItemStack = NMS.getMNSItemStack(itemInMainHand)
                    val save = mnsItemStack.save(YuShop.reg)
                    Bukkit.getConsoleSender().sendMessage("物品nbt:$save")
                    return true
                }
                if (args[0]=="reload"){
                    if (!sender.hasPermission("yushop.reload")){
                        return true
                    }
                    ConfigManager.reload()
                    sender.sendMessage("§a重载成功")
                    return true
                }
                val configuration = ConfigManager.viewConfigurationMap[args[0]]?:return true
                ShopGUI().open(configuration,sender, ConfigManager.shop[args[0]]!!)
            }else{
                return true
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): List<String> {
        if (args.isEmpty()) {
            return emptyList()
        }
        return when (args.size) {
            1 -> {
                val input = args[0].lowercase()
                val suggestions = mutableListOf<String>()

                // 添加子命令
                suggestions.add("item")
                suggestions.add("reload")

                // 添加所有商店名称
                ConfigManager.shop.keys.forEach { shopName ->
                    suggestions.add(shopName)
                }

                // 过滤并排序
                suggestions.filter { it.lowercase().startsWith(input) }.sorted()
            }
            else -> emptyList()
        }
    }
}