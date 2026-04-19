package com.wuyumoom.yushop.model.money

import com.mc9y.nyeconomy.api.NyEconomyAPI
import com.wuyumoom.yushop.api.money.IMoney
import com.wuyumoom.yushop.model.Product
import org.bukkit.entity.Player

class Nye(val nye: String): IMoney {
    override fun give(player: Player, count: Int) {
        val checkVaultType = NyEconomyAPI.getInstance().checkVaultType(nye)
        NyEconomyAPI.getInstance().deposit(checkVaultType, player.uniqueId, count)
    }

    override fun take(player: Player, count: Int) {
        val checkVaultType = NyEconomyAPI.getInstance().checkVaultType(nye)
        NyEconomyAPI.getInstance().withdraw(checkVaultType, player.uniqueId, count)
    }

    override fun hasEnough(player: Player, count: Int,executeCount: Int): Boolean {
        val checkedType = NyEconomyAPI.getInstance().checkVaultType(nye)
        val balance = NyEconomyAPI.getInstance().getBalance(checkedType, player.name)
        return balance >= (count*executeCount)
    }

    override fun getCanBuyCount(
        player: Player,
        count: Int
    ): Int {
        val checkedType = NyEconomyAPI.getInstance().checkVaultType(nye)
        val balance = NyEconomyAPI.getInstance().getBalance(checkedType, player.name)
        return balance / count
    }
}