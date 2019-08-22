package com.nsg.domain

/**
 * Created by lijc on 2016/10/18.
 */

data class KuaidiInfo(
        var dataSource: String,
        var shippingCode: String,
        val status: String,
        val state: String,
        val message: String,
        val data: List<StepInfo>?
) {
    private constructor() : this("", "", "", "", "", null)
}