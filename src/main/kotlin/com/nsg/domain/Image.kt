package com.nsg.domain

/**
 * Created by lijc on 16/2/22.
 */

data class Image(
        var imgId: Long,
        var imgTitle: String,
        var imgIntro: String,
        var imgUrl: String,
        var linkType: Int?, //1表示跳转到H5页面，2表示跳转到商品详情
        var linkUrl: String?
        //        var createTime: Long
) {
    private constructor() : this(0, "", "", "", 0, "")
}