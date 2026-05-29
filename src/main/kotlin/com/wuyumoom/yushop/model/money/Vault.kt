package com.wuyumoom.yushop.model.money

import com.wuyumoom.yushop.api.money.IMoney
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class Vault: IMoney {
    val rsp = Bukkit.getServer().servicesManager.getRegistration(Economy::class.java)
    override fun give(player: Player, count: Int) {
        val economy = rsp?.getProvider() ?: throw IllegalStateException("无法获取经济系统")
        economy.depositPlayer(player, count.toDouble())
    }

    override fun take(player: Player, count: Int) {
        val economy = rsp?.getProvider() ?: throw IllegalStateException("无法获取经济系统")
        economy.withdrawPlayer(player, count.toDouble())
    }

    override fun hasEnough(player: Player, count: Int,executeCount: Int): Boolean {
        val economy = rsp?.getProvider() ?: throw IllegalStateException("无法获取经济系统")
        val balance =economy.getBalance(player)
        return balance >= (count*executeCount)
    }

    override fun getCanBuyCount(
        player: Player,
        count: Int
    ): Int {
        val economy = rsp?.getProvider() ?: throw IllegalStateException("无法获取经济系统")
        val balance = economy.getBalance(player)
        return (balance / count).toInt()
    }
}