package com.nsg.domain

import com.nsg.util.dateFormatLongToString

/**
 * Created by lijc on 16/2/25.
 */

data class Address(
        var addressId: Long,
        var addressee: String,  //收件人
        var province: String,   //省
        var city: String,       //市
        var area: String,       //区
        var direction: String,  //详细地址
        var phoneNum: String,
        var isDefault: Boolean = false,
        var createTime: Long,
        var userId: Long
) {
    private constructor() : this(0, "", "", "", "", "", "", false, 0, 0)

    var createTimeShow: String = ""
        get() = createTime.dateFormatLongToString()

}