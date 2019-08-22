package com.nsg.domain.form

/**
 * Created by lijc on 16/2/25.
 */

class AddressForm{
    var addressId: Long? = 0
    var addressee: String = ""  //收件人
    var province: String? = ""   //省
    var city: String = ""       //市
    var area: String = ""       //区
    var direction: String = ""  //详细地址
    var phoneNum: String = ""
    var isDefault: Boolean? = false
    var userId: Long? = 0
}