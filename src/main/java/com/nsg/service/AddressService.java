package com.nsg.service;

import com.nsg.domain.Address;
import com.nsg.mapper.AddressMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Created by lijc on 16/3/29.
 */

@Transactional
@Service
public class AddressService {

    @Autowired
    private AddressMapper addressMapper;

    public Address getById(long addressId) {
        return addressMapper.getById(addressId);
    }

    public List<Address> getBtUser(long userId) {
        return addressMapper.getByUser(userId);
    }

    @NotNull
    public void save(@NotNull Address address) {
        addressMapper.save(address);
    }

    public void setAddressDefault(@NotNull Map<String, Object> map) {
        addressMapper.setAddressDefault(map);
        addressMapper.setOtherAddressUndefault(map);
    }

    public void clearAddress(@NotNull Long user_id) {
        addressMapper.clearAddress(user_id);
    }

    public void updateAddress(@NotNull Address address) {
        addressMapper.updateAddress(address);
    }

    public void deleteAddress(long address_id) {
        addressMapper.deleteAddress(address_id);
    }

    @NotNull
    public Address getDefault(@NotNull Long user_id) {
        return addressMapper.getDefault(user_id);
    }
}
