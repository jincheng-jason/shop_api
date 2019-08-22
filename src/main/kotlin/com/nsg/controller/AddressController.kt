package com.nsg.controller

import com.alibaba.fastjson.JSON
import com.nsg.data.AddressData
import com.nsg.domain.Address
import com.nsg.domain.form.AddressForm
import com.nsg.domain.form.ShopIds
import com.nsg.service.AddressService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.net.URLEncoder
import java.util.*
import javax.servlet.http.HttpServletResponse

/**
 * Created by lijc on 16/2/22.
 */

@Api(basePath = "/v1/mall/csl", value = "address", description = "用户地址API", produces = "application/json", position = 6)
@RestController
@RequestMapping("/v1/mall/csl")
class AddressController
@Autowired constructor(var addressService: AddressService) {


    val shopId = ShopIds.CSL.shopId
    private val LOGGER = LoggerFactory.getLogger(AddressController::class.java)

    @ApiOperation(httpMethod = "GET", value = "获取用户的地址列表", response = Array<AddressData>::class)
    @RequestMapping(value = "/addresses", method = arrayOf(RequestMethod.GET))
    fun getAddressByUser(@RequestParam(value = "user_id", required = true) user_id: Long,
                         response: HttpServletResponse): List<Address> {

        try {
            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("查询成功", "UTF-8"))
            return addressService.getBtUser(user_id)
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("查询失败", "UTF-8"))
            return listOf()
        }

    }

    @ApiOperation(httpMethod = "POST", value = "添加地址信息")
    @RequestMapping(value = "/addresses", method = arrayOf(RequestMethod.POST))
    fun addAddress(@RequestParam(value = "user_id", required = true) user_id: Long,
                   @RequestBody(required = true) addressForm: AddressForm,
                   response: HttpServletResponse): Address {

        try {
            LOGGER.info("----------请求参数:${JSON.toJSON(addressForm)}")
            var isDefault = addressForm.isDefault
            if (isDefault == null) {
                isDefault = false
            }
            var address = Address(0, addressForm.addressee, addressForm.province!!, addressForm.city, addressForm.area, addressForm.direction, addressForm.phoneNum, isDefault, Date().time, user_id)
            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("提交成功", "UTF-8"))
            addressService.save(address)
            return address
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("提交失败", "UTF-8"))
            return Address(0, "", "", "", "", "", "", false, 0, 0)
        }

    }

    @ApiOperation(httpMethod = "DELETE", value = "清空用户地址列表")
    @RequestMapping(value = "/addresses", method = arrayOf(RequestMethod.DELETE))
    fun emptyAddresses(@RequestParam(value = "user_id", required = true) user_id: Long,
                       response: HttpServletResponse) : Map<Any,Any> {

        try {
            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("提交成功", "UTF-8"))
            addressService.clearAddress(user_id)
            return mapOf()
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("提交失败", "UTF-8"))
            return mapOf()
        }
    }

    @ApiOperation(httpMethod = "GET", value = "获取一条用户地址信息", response = AddressData::class)
    @RequestMapping(value = "/addresses/{address_id}", method = arrayOf(RequestMethod.GET))
    fun getOneAddress(@PathVariable address_id: Long, response: HttpServletResponse): AddressData {
        try {
            val address = addressService.getById(address_id)
            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("查询成功", "UTF-8"))
            return AddressData.of(address)
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("查询失败", "UTF-8"))
            return AddressData(0, "", "", "", "", "", "", false, "")
        }
    }

    @ApiOperation(httpMethod = "PUT", value = "修改一条用户地址信息")
    @RequestMapping(value = "/addresses/{address_id}", method = arrayOf(RequestMethod.PUT))
    fun editOneAddress(@PathVariable address_id: Long,
                       @RequestBody(required = true) addressForm: AddressForm,
                 response: HttpServletResponse) : Map<Any,Any> {

        try {

            LOGGER.info("-----------请求参数:${JSON.toJSON(addressForm)}")
            var address = Address(address_id, addressForm.addressee, addressForm.province!!, addressForm.city,
                    addressForm.area, addressForm.direction, addressForm.phoneNum, addressForm.isDefault ?: false, 0, 0)
            addressService.updateAddress(address)
            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("更新成功", "UTF-8"))
            return mapOf()
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("更新失败", "UTF-8"))
            return mapOf()
        }
    }

    @ApiOperation(httpMethod = "DELETE", value = "删除一条用户地址信息")
    @RequestMapping(value = "/addresses/{address_id}", method = arrayOf(RequestMethod.DELETE))
    fun deleteOneAddress(@PathVariable address_id: Long,
                       response: HttpServletResponse) : Map<Any,Any> {
        try {
            addressService.deleteAddress(address_id)
            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("删除成功", "UTF-8"))
            return mapOf()
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("删除失败", "UTF-8"))
            return mapOf()
        }
    }

    @ApiOperation(httpMethod = "PUT", value = "设置默认用户地址信息")
    @RequestMapping(value = "/users/{user_id}/addresses/{address_id}/default", method = arrayOf(RequestMethod.PUT))
    fun letAddressDefault(
            @PathVariable user_id: String,
            @PathVariable address_id: Long,
            response: HttpServletResponse) : Map<Any,Any> {
        try {
            addressService.setAddressDefault(mapOf("userId" to user_id, "addressId" to address_id))
            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("更新成功", "UTF-8"))
            return mapOf()
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("更新失败", "UTF-8"))
            return mapOf()
        }

    }

    @ApiOperation(httpMethod = "GET", value = "获取用户的默认地址")
    @RequestMapping(value = "/users/{user_id}/addresses/default", method = arrayOf(RequestMethod.GET))
    fun getAddressDefault(
            @PathVariable user_id: Long,
            response: HttpServletResponse
    ) : AddressData {

        try {
            val address = addressService.getDefault(user_id)
            if (address == null || address.addressId.equals(0L)) {
                response.status = HttpServletResponse.SC_OK
                response.addHeader("X-Err-Message", URLEncoder.encode("该用户没有默认地址", "UTF-8"))
                return AddressData(0, "", "", "", "", "", "", false, "")
            }
            response.status = HttpServletResponse.SC_OK
            response.addHeader("X-Err-Message", URLEncoder.encode("查询成功", "UTF-8"))
            return AddressData.of(address)
        } catch(e: Exception) {
            e.printStackTrace()
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.addHeader("X-Err-Message", URLEncoder.encode("查询失败", "UTF-8"))
            return AddressData(0, "", "", "", "", "", "", false, "")
        }
    }



}