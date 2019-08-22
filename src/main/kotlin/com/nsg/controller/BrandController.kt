//package com.nsg.controller
//
//import com.nsg.domain.Brand
//import com.nsg.domain.Goods
//import com.nsg.domain.data.BrandData
//import com.nsg.domain.data.GoodsData
//import io.swagger.annotations.Api
//import io.swagger.annotations.ApiOperation
//import org.springframework.web.bind.annotation.*
//
///**
// * Created by lijc on 16/2/21.
// */
//
//@Api(basePath = "/v1/mall/csl", value = "brand", description = "品牌API", produces = "application/json", position = 2)
//@RestController
//@RequestMapping("/v1/mall/csl")
//class BrandController{
//
//    @ApiOperation(httpMethod = "GET", value = "品牌列表", response = Array<BrandData>::class)
//    @RequestMapping(value = "/brands", method = arrayOf(RequestMethod.GET))
//    fun getBrands(
//            @RequestParam(value = "pageNum", required = false) pageNum: Int?,
//            @RequestParam(value = "pageSize", required = false) pageSize: Int?
//            ) : Array<BrandData> {
//
//        val brand1 = Brand(1,"品牌名称1",
//                "http://7xldo6.com2.z0.glb.qiniucdn.com/Fg0GGFpWR4zesklv-xXQAzW1rD9N",
//                1,1)
//
//        val brand2 = Brand(2,"品牌名称2",
//                "http://7xldo6.com2.z0.glb.qiniucdn.com/Fg0GGFpWR4zesklv-xXQAzW1rD9N",
//                2,2)
//
//        val brand3 = Brand(3,"品牌名称3",
//                "http://7xldo6.com2.z0.glb.qiniucdn.com/Fg0GGFpWR4zesklv-xXQAzW1rD9N",
//                3,3)
//
//        return arrayOf(BrandData.of(brand1), BrandData.of(brand2), BrandData.of(brand3))
//    }
//
//    @ApiOperation(httpMethod = "GET", value = "品牌信息", response = BrandData::class)
//    @RequestMapping(value = "/brands/{brand_id}", method = arrayOf(RequestMethod.GET))
//    fun getBrandById(
//            @PathVariable(value = "brand_id") brand_id: Long
//    ) : BrandData{
//        val brand1 = Brand(1,"品牌名称1",
//                "http://7xldo6.com2.z0.glb.qiniucdn.com/Fg0GGFpWR4zesklv-xXQAzW1rD9N",
//                1,1)
//
//        return BrandData.of(brand1)
//    }
//
//    @ApiOperation(httpMethod = "GET", value = "品牌货品列表", response = Array<GoodsData>::class)
//    @RequestMapping(value = "/brands/{brand_id}/goods", method = arrayOf(RequestMethod.GET))
//    fun getGooodsByBrand(
//            @PathVariable(value = "brand_id") brand_id: Long
//    ) : Array<Goods> {
//        var goods1 = Goods(1,1,1,1,"商品名称1","商品详情1",
//                "http://7xldo6.com2.z0.glb.qiniucdn.com/Fg0GGFpWR4zesklv-xXQAzW1rD9N",
//                "http://www.baidu.com", 1, 300, 1, 1, 12345, 35, 100, null,true)
//
//        var goods2 = Goods(2,1,1,1,"商品名称2","商品详情2",
//                "http://7xldo6.com2.z0.glb.qiniucdn.com/Fg0GGFpWR4zesklv-xXQAzW1rD9N",
//                "http://www.baidu.com", 1, 300, 1, 1, 12345, 35, 100, null,true)
//
//        var goods3 = Goods(3,1,1,1,"商品名称3","商品详情3",
//                "http://7xldo6.com2.z0.glb.qiniucdn.com/Fg0GGFpWR4zesklv-xXQAzW1rD9N",
//                "http://www.baidu.com", 1, 300, 1, 1, 12345, 35, 100, null,true)
//        return arrayOf(goods1, goods2, goods3)
//    }
//
//}