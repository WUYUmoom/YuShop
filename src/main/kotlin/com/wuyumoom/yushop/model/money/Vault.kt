package com.wuyumoom.yushop.model.money

import com.wuyumoom.yushop.YuShop
import com.wuyumoom.yushop.api.money.IMoney
import org.bukkit.entity.Player

class Vault: IMoney {
    override fun give(player: Player, count: Int) {
        YuShop.economy.depositPlayer(player, count.toDouble())
    }

    override fun take(player: Player, count: Int) {
        YuShop.economy.withdrawPlayer(player, count.toDouble())
    }

    override fun hasEnough(player: Player, count: Int): Boolean {
        val balance = YuShop.economy.getBalance(player)
        return balance >= count
    }
}