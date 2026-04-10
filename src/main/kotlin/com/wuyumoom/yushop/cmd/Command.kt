package com.wuyumoom.yushop.cmd

import com.wuyumoom.yushop.config.ConfigManager
import com.wuyumoom.yushop.view.ShopGUI
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
            val configuration = ConfigManager.viewConfigurationMap["默认个人商店"]!!
            ShopGUI.open(configuration,sender, ConfigManager.shop["默认个人商店"]!!)
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): List<String> {
        return mutableListOf()
    }
}