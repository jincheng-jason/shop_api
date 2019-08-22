package com.nsg.mapper

import com.nsg.domain.Payment

/**
 * Created by lijc on 16/3/21.
 */

interface PaymentMapper {

    fun save(payment: Payment): Int

    fun getPayStatus(order_id: Long): Payment

}