package com.wuyumoom.yushop.util

import kotlin.math.max
import kotlin.random.Random

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
