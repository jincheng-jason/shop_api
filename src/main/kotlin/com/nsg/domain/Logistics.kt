package com.nsg.domain

/**
 * Created by lijc on 16/3/14.
 */

data class Logistics(
        var dataSource: String, //信息来源
        var orderNo: String, //客户单号
        var orderStatus: String, //物流订单当前状态
        var statusTime: String, //物流当前时间
        var error: String?, //原因说明
        var remark: String?, //备注
        var signinPer: String?, //签收人
        var steps: Array<Step>?,
        var order: Order?
)