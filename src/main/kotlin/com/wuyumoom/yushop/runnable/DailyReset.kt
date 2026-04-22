
package com.wuyumoom.yushop.runnable

import com.wuyumoom.yushop.YuShop
import com.wuyumoom.yushop.api.data.DataManager
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import java.time.LocalDateTime
import java.time.Duration

object DailyReset {

    private lateinit var scheduledTask: BukkitRunnable

    /**
     * 启动每日重置任务调度器
     */
    fun start(plugin: YuShop) {
        scheduleNextReset(plugin)
    }

    /**
     * 安排下一次重置
     */
    private fun scheduleNextReset(plugin: YuShop) {
        val now = LocalDateTime.now()
        val nextReset = getNextResetTime(now)
        val delayInSeconds = Duration.between(now, nextReset).seconds
        val delayInTicks = maxOf(delayInSeconds * 20, 1L)

        Bukkit.getConsoleSender().sendMessage("§e[YuShop] §7下次重置时间: $nextReset (延迟: ${delayInSeconds}秒)")

        scheduledTask = object : BukkitRunnable() {
            override fun run() {
                executeDailyTask()
                scheduleNextReset(plugin)
            }
        }

        scheduledTask.runTaskLater(plugin, delayInTicks)
    }

    /**
     * 获取下一次重置时间（下一个 23:00）
     */
    private fun getNextResetTime(now: LocalDateTime): LocalDateTime {
        val today2300 = now.withHour(23).withMinute(0).withSecond(0).withNano(0)

        return if (now.isBefore(today2300)) {
            today2300
        } else {
            today2300.plusDays(1)
        }
    }

    /**
     * 执行每日重置任务
     */
    private fun executeDailyTask() {
        Bukkit.getConsoleSender().sendMessage("§a§l========================================")
        Bukkit.getConsoleSender().sendMessage("§a[YuShop] §7开始执行每日重置任务...")
        try {
            DataManager.upDateLimit()
            Bukkit.getConsoleSender().sendMessage("§a[YuShop] §7✓ 限购数据已重置")
        } catch (e: Exception) {
            Bukkit.getConsoleSender().sendMessage("§c[YuShop] §7每日重置任务失败: ${e.message}")
            e.printStackTrace()
        }

        Bukkit.getConsoleSender().sendMessage("§a§l========================================")
    }

}