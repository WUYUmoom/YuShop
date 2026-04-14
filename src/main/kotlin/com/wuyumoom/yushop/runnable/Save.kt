package com.wuyumoom.yushop.runnable

import com.wuyumoom.yushop.api.data.DataManager
import com.wuyumoom.yushop.config.ConfigManager
import org.bukkit.scheduler.BukkitRunnable

object Save: BukkitRunnable() {
    override fun run() {
        DataManager.saveAllData()
        ConfigManager.dataLimit.save(ConfigManager.dataFileLimit)
    }
}