package com.nsg.domain.form

/**
 * Created by lijc on 16/3/24.
 */

class PayStatusForm {
    var out_trade_no: String = ""
    //    var paymentName: String? = null
    //    var paymentCode: String? = null
    //    var paymentInfo: String? = null
    //    var payMassage: String? = null
    var pay_state: Int = 0    //支付状态,0:失败,1:成功,其它
    var real_fee: Int = 0
    var pay_time: String = ""
    var pay_type: String = ""     //wechat,alipay
}