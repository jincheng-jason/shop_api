package com.nsg.domain

/**
 * Created by lijc on 16/2/23.
 */

data class Category(
        var categoryId: Long,
        var categoryName: String,
        var shopId: Long,
        var parentCategoryId: Long,
        var createTime: Long
)