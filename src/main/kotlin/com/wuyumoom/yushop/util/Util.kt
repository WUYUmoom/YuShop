package com.wuyumoom.yushop.util

import com.wuyumoom.yushop.model.Product
import com.wuyumoom.yushop.model.Shop
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.math.max
import kotlin.random.Random



/**
 * 获取随机商品
 */
fun getRandomProduct(shop: Shop): MutableList<Product> {
    // 预先获取所有可用商品
    val availableTasks = mutableListOf<Product>()
    shop.weightProduct.forEach { (weight, tasks) ->
        tasks.forEach { task ->
            availableTasks.add(task)
        }
    }
    // 移除重复商品
    val uniqueAvailableTasks = availableTasks.distinct()
    // 如果可用任务少于所需数量，返回所有可用任务
    if (uniqueAvailableTasks.size <= shop.shopSlot.size) {
        return uniqueAvailableTasks.toMutableList()
    }
    // 使用加权随机选择不重复的任务
    val selectedTasks = mutableSetOf<Product>()
    val validTaskMap = mutableMapOf<Int, MutableList<Product>>()
    // 仅包含可接受的任务到权重映射中
    shop.weightProduct.forEach { (weight, tasks) ->
        val validTasks = tasks.filter { task ->
            task !in selectedTasks
        }.toMutableList()
        if (validTasks.isNotEmpty()) {
            validTaskMap[weight] = validTasks
        }
    }
    // 随机选择直到达到所需数量或没有更多任务
    while (selectedTasks.size < shop.shopSlot.size && validTaskMap.isNotEmpty()) {
        try {
            val task = validTaskMap.getWeightedTask()
            if (task !in selectedTasks) {
                selectedTasks.add(task)
            } else {
                // 如果选中了已有的任务，继续尝试
                continue
            }
        } catch (e: IllegalStateException) {
            // 如果权重映射为空，跳出循环
            break
        }
    }

    return selectedTasks.toMutableList()
}
/**
 * 检查玩家背包中是否有指定物品和数量
 *
 * @param player 要检查的玩家
 * @param targetItem 目标物品
 * @param requiredAmount 需要的数量，默认为1
 * @return 如果玩家背包中有足够的物品返回true，否则返回false
 */
fun hasItemInInventory(player: Player, targetItem: ItemStack, requiredAmount: Int = 1): Boolean {
    if (requiredAmount <= 0) return true
    if (targetItem.type.isAir) return false

    var totalCount = 0

    for (item in player.inventory.contents) {
        if (item != null && item.isSimilar(targetItem)) {
            totalCount += item.amount
            if (totalCount >= requiredAmount) {
                return true
            }
        }
    }

    return false
}

/**
 * 从玩家背包中移除指定物品和数量
 *
 * @param player 要操作的玩家
 * @param targetItem 目标物品
 * @param removeAmount 需要移除的数量
 * @return 实际移除的数量
 */
fun removeItemFromInventory(player: Player, targetItem: ItemStack, removeAmount: Int = 1): Int {
    if (removeAmount <= 0) return 0
    if (targetItem.type.isAir) return 0

    var removedCount = 0

    for (i in player.inventory.contents.indices) {
        if (removedCount >= removeAmount) break

        val item = player.inventory.getItem(i)
        if (item != null && item.isSimilar(targetItem)) {
            val available = item.amount
            val toRemove = minOf(available, removeAmount - removedCount)

            if (toRemove >= available) {
                player.inventory.setItem(i, null)
            } else {
                item.amount = available - toRemove
                player.inventory.setItem(i, item)
            }

            removedCount += toRemove
        }
    }

    return removedCount
}

/**
 * Selects a task from a weighted map where keys are weights and values are lists of tasks.
 * Uses the key (weight) for weighted random selection, then randomly picks a task from the selected list.
 *
 * @param random The random instance to use for selection
 * @return A task selected based on weight, never null
 * @throws IllegalStateException if the map is empty or all task lists are empty
 */
@JvmOverloads
fun <Task> MutableMap<Int, MutableList<Task>>.getWeightedTask(random: Random = Random.Default): Task {
    // Filter out entries with empty lists and calculate total weight
    val validEntries = entries.filter { it.value.isNotEmpty() }

    if (validEntries.isEmpty()) {
        throw IllegalStateException("Cannot select task: map is empty or all task lists are empty")
    }

    // Calculate total weight sum
    var weightSum = 0F
    validEntries.forEach { weightSum += max(0F, it.key.toFloat()) }

    if (weightSum <= 0F) {
        throw IllegalStateException("Cannot select task: total weight is zero or negative")
    }

    // Select a weight using weighted random
    val chosenSum = random.nextFloat() * weightSum
    var currentSum = 0F

    for (entry in validEntries) {
        val weight = max(0F, entry.key.toFloat())
        if (weight > 0) {
            currentSum += weight
            if (currentSum >= chosenSum) {
                // Randomly select a task from the chosen list
                val taskList = entry.value
                if (taskList.isEmpty()) {
                    throw IllegalStateException("Selected weight list is empty")
                }
                return taskList[random.nextInt(taskList.size)]
            }
        }
    }

    // Fallback to last entry (should not reach here, but safety check)
    val lastEntry = validEntries.last()
    if (lastEntry.value.isEmpty()) {
        throw IllegalStateException("Last entry task list is empty")
    }
    return lastEntry.value[random.nextInt(lastEntry.value.size)]
}
