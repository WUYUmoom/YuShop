package com.wuyumoom.yushop.api.data

import org.bukkit.configuration.ConfigurationSection

data class PlayerShopData (
    val limit: MutableMap<String, Int>,
    var product: MutableMap<String, Int>
){
    companion object{
        /**
         * 创建玩家商店数据
         */
        fun create(configurationSection: ConfigurationSection): PlayerShopData{
            var limit : MutableMap<String, Int> = mutableMapOf()
            var product: MutableMap<String, Int> = mutableMapOf()
            val limitYML = configurationSection.getConfigurationSection("limit")
            if (limitYML != null){
                limit = getLimit(limitYML)
            }
            val productYML = configurationSection.getConfigurationSection("product")
            if (productYML != null){
                product = getProduct(productYML)
            }
            return PlayerShopData(limit, product)
        }
        private fun getLimit(configurationSection: ConfigurationSection): MutableMap<String, Int>{
            val limit = mutableMapOf<String, Int>()
            configurationSection.getKeys(false).forEach {
                limit[it] = configurationSection.getInt(it)
            }
            return limit
        }
        private fun getProduct(configurationSection: ConfigurationSection): MutableMap<String, Int>{
            val product = mutableMapOf<String, Int>()
            configurationSection.getKeys(false).forEach {
                product[it] = configurationSection.getInt(it)
            }
            return product
        }
    }
}