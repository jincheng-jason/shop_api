package com.nsg.data

import com.nsg.domain.Address
import com.nsg.util.dateFormatLongToString

/**
 * Created by lijc on 16/2/25.
 */

data class AddressData(
        var addressId: Long,
        var addressee: String,  //收件人
        var province: String,   //省
        var city: String,       //市
        var area: String,       //区
        var direction: String,  //详细地址
        var phoneNum: String,
        var isDefault: Boolean = false,
        var createTime: String
){
    companion object{
        fun of(source: Address) = AddressData(source.addressId,source.addressee,source.province,
                source.city, source.area, source.direction, source.phoneNum, source.isDefault, source.createTime.dateFormatLongToString())
    }
}