package com.nsg.domain.data

import com.nsg.domain.Category
import com.nsg.util.dateFormatLongToString

/**
 * Created by lijc on 16/2/26.
 */

data class CategoryData(
        var categoryId: Long,
        var categoryName: String,
        var shopId: Long,
        var parentCategoryId: Long,
        var createTime: String
){
    companion object{
        fun of(source: Category) = CategoryData(source.categoryId,source.categoryName,source.shopId,
                source.parentCategoryId, source.createTime.dateFormatLongToString())
    }
}