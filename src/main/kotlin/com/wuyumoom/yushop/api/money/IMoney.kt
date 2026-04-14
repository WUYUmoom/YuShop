package com.wuyumoom.yushop.api.money

import org.bukkit.entity.Player

interface IMoney {
    fun give(player: Player, count: Int)
    fun take(player: Player, count: Int)
    fun hasEnough(player: Player, count: Int): Boolean
}