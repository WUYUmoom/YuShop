package com.wuyumoom.yushop.model.money

import com.wuyumoom.yushop.api.money.IMoney
import org.black_ixx.playerpoints.PlayerPoints
import org.bukkit.entity.Player

class Points: IMoney {
    override fun give(player: Player, count: Int) {
        PlayerPoints.getInstance().api.give(player.uniqueId, count)
    }

    override fun take(player: Player, count: Int) {
        PlayerPoints.getInstance().api.take(player.uniqueId, count)
    }

    override fun hasEnough(player: Player, count: Int): Boolean {
        val look = PlayerPoints.getInstance().api.look(player.uniqueId)
        return look >= count
    }
}