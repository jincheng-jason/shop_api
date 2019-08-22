package com.nsg.mapper

import com.nsg.domain.Address

/**
 * Created by lijc on 16/2/29.
 */

interface AddressMapper{

    fun getById(addressId: Long) : Address

    fun getByUser(userId: Long): List<Address>

    fun save(address: Address)

    fun setAddressDefault(map: Map<String, Any>)

    fun setOtherAddressUndefault(map: Map<String, Any>)

    fun clearAddress(userId: Long)

    fun updateAddress(address: Address)

    fun deleteAddress(addressId: Long)

    fun getDefault(userId: Long): Address

}