package com.wuyumoom.yushop.api.type

import com.wuyumoom.yushop.api.data.DataManager
import com.wuyumoom.yushop.api.data.PlayerData
import com.wuyumoom.yushop.model.Product
import com.wuyumoom.yushop.model.Shop

enum class ShopLimit {
    Personal {
        override fun updateProduct(data: PlayerData, product: String,shop: Shop,executeCount: Int) {
            val playerShopData = data.shopData[shop.name]
            if (playerShopData != null){
                playerShopData.limit[product] = playerShopData.limit.getOrDefault(product,0)+executeCount
            }else{
                data.updateProduct()
            }
        }

        override fun getLimit(
            data: PlayerData,
            shop: Shop,
            product: Product
        ): Int {
            val playerShopData = data.shopData[shop.name] ?: return 0
            return playerShopData.limit[product.name]?: return 0
        }
    },
    Server {
        override fun updateProduct(data: PlayerData, product: String,shop: Shop,executeCount: Int) {
            DataManager.updateServerLimit(product,shop.name,executeCount)
        }

        override fun getLimit(
            data: PlayerData,
            shop: Shop,
            product: Product
        ): Int {
            return DataManager.getServerLimit(product,shop)
        }
    };
    /**
     * 更新购买次数
     */
    abstract fun updateProduct(data: PlayerData, product: String,shop: Shop,executeCount: Int)

    /**
     * 获取可购买次数
     */
    abstract fun getLimit(data: PlayerData, shop: Shop, product: Product): Int
}