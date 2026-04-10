package com.wuyumoom.yushop

import com.wuyumoom.yushop.cmd.Command
import com.wuyumoom.yushop.config.ConfigManager
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class YuShop : JavaPlugin() {
    companion object {
        lateinit var pluginFile: File
        lateinit var INSTANCE: YuShop
        val LOGO = arrayOf(
            "===============================================================================",
            "§f██╗   ██╗██╗   ██╗███████╗██╗  ██╗ ██████╗ ██████╗",
            "§f╚██╗ ██╔╝██║   ██║██╔════╝██║  ██║██╔═══██╗██╔══██╗",
            "§f ╚████╔╝ ██║   ██║███████╗███████║██║   ██║██████╔╝",
            "§f  ╚██╔╝  ██║   ██║╚════██║██╔══██║██║   ██║██╔═══╝ ",
            "§f   ██║   ╚██████╔╝███████║██║  ██║╚██████╔╝██║     ",
            "§f   ╚═╝    ╚═════╝ ╚══════╝╚═╝  ╚═╝ ╚═════╝ ╚═╝     ",
            "§e§l语之动态商店 §6§l启动完成！",
            "§e§l作者 : 姬无语 §6§lQQ1841375451",
            "==============================================================================="
        )
    }
    override fun onEnable() {
        INSTANCE = this
        pluginFile = this.file
        saveDefaultConfig()
        ConfigManager.load()
        getCommand("yushop")?.let {
            it.setExecutor(Command)
            it.tabCompleter = Command
        }
        Bukkit.getConsoleSender().sendMessage(*LOGO)
    }
    override fun onDisable() {

    }
}