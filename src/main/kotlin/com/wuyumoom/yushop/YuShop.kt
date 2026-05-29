package com.wuyumoom.yushop

import com.wuyumoom.yushop.api.data.DataManager
import com.wuyumoom.yushop.api.data.StorageType
import com.wuyumoom.yushop.cmd.Command
import com.wuyumoom.yushop.config.ConfigManager
import com.wuyumoom.yushop.database.DatabaseManager
import com.wuyumoom.yushop.listener.PluginEvent
import com.wuyumoom.yushop.runnable.DailyReset
import com.wuyumoom.yushop.runnable.Save
import java.io.File
import net.milkbowl.vault.economy.Economy
import net.minecraft.core.RegistryAccess
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_21_R1.CraftServer
import org.bukkit.plugin.java.JavaPlugin

class YuShop : JavaPlugin() {
    companion object {
        lateinit var reg: RegistryAccess
        lateinit var pluginFile: File
        lateinit var INSTANCE: YuShop
        val LOGO =
                arrayOf(
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
        val craftServer: CraftServer = Bukkit.getServer() as CraftServer
        reg = craftServer.server.registryAccess()
        ConfigManager.load()
        getCommand("yushop")?.let {
            it.setExecutor(Command)
            it.tabCompleter = Command
        }
        if (ConfigManager.storage_mode == StorageType.YML) {
            Save.runTaskTimer(this, 20L, 20L)
        }
        Bukkit.getPluginManager().registerEvents(PluginEvent(), YuShop.INSTANCE)
        DailyReset.start(this)
        Bukkit.getConsoleSender().sendMessage(*LOGO)
    }

    override fun onDisable() {
        DataManager.saveAllData()
        if (ConfigManager.storage_mode == StorageType.YML) {
            ConfigManager.dataLimit.save(ConfigManager.dataFileLimit)
        }
    }
}

