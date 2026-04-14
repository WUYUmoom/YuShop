package com.wuyumoom.yushop.listener

import com.wuyumoom.yushop.api.data.DataManager
import com.wuyumoom.yushop.config.ConfigManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PluginEvent: Listener {
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent){
        DataManager.saveData(event.player.name)
    }

}