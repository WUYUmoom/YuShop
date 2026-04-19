package com.wuyumoom.yushop.api.money

import com.wuyumoom.yushop.model.Product
import org.bukkit.entity.Player

interface IMoney {
    fun give(player: Player, count: Int)
    fun take(player: Player, count: Int)
    fun hasEnough(player: Player, count: Int,executeCount: Int): Boolean
    /**
     * 能购买多少
     */
    fun getCanBuyCount(player: Player, count: Int): Int
}