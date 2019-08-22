//package com.nsg.controller
//
//import com.nsg.domain.Category
//import com.nsg.domain.Goods
//import com.nsg.domain.data.CategoryData
//import io.swagger.annotations.Api
//import io.swagger.annotations.ApiOperation
//import org.springframework.web.bind.annotation.PathVariable
//import org.springframework.web.bind.annotation.RequestMapping
//import org.springframework.web.bind.annotation.RequestMethod
//import org.springframework.web.bind.annotation.RestController
//
///**
// * Created by lijc on 16/2/21.
// */
//
//@Api(basePath = "/v1/mall/csl", value = "category", description = "分类API", produces = "application/json", position = 3)
//@RestController
//@RequestMapping("/v1/mall/csl")
//class CategoryController{
//
//    @ApiOperation(httpMethod = "GET", value = "分类列表", response = Map::class)
//    @RequestMapping(value = "/categories", method = arrayOf(RequestMethod.GET))
//    fun getCategories() : Array<CategoryData>{
//        var category1 = Category(1,"总分类1",1,0,123456)
//        var category2 = Category(2,"总分类2",1,0,123456)
//        var category3 = Category(3,"总分类3",1,0,123456)
//        var category4 = Category(4,"分类1",1,1,123456)
//        var category5 = Category(5,"分类2",1,2,123456)
//        var category6 = Category(6,"分类3",1,3,123456)
//
//        return arrayOf(
//                CategoryData.of(category1),
//                CategoryData.of(category2),
//                CategoryData.of(category3),
//                CategoryData.of(category4),
//                CategoryData.of(category5),
//                CategoryData.of(category6)
//                )
//    }
//
//    @ApiOperation(httpMethod = "GET", value = "分类信息", response = Map::class)
//    @RequestMapping(value = "/categories/{category_id}", method = arrayOf(RequestMethod.GET))
//    fun getCategoryById(
//            @PathVariable(value = "category_id") category_id: Long
//    ) : CategoryData{
//        var category1 = Category(1,"总分类1",1,0,123456)
//        return CategoryData.of(category1)
//    }
//
//    @ApiOperation(httpMethod = "GET", value = "分类产品列表", response = Map::class)
//    @RequestMapping(value = "/categories/{category_id}/goods", method = arrayOf(RequestMethod.GET))
//    fun getGooodsByCategory(
//            @PathVariable(value = "category_id") category_id: Long
//    ) : Array<Goods>{
//        val goods1 = Goods(1, 1, 1, 1, "测试商品1", "商品介绍商品介绍商品介绍商品介绍商品介绍",
//                "http://7xldo6.com2.z0.glb.qiniucdn.com/Fg0GGFpWR4zesklv-xXQAzW1rD9N",
//                "https://www.baidu.com", 1, 32, 1, 2, 123321, 35, 500, null,true)
//
//        val goods2 = Goods(2, 1, 1, 1, "测试商品2", "商品介绍商品介绍商品介绍商品介绍商品介绍",
//                "http://7xldo6.com2.z0.glb.qiniucdn.com/Fg0GGFpWR4zesklv-xXQAzW1rD9N",
//                "https://www.baidu.com", 1, 32, 1, 2, 123321, 35, 500, null,true)
//
//        val goods3 = Goods(3, 1, 1, 1, "测试商品3", "商品介绍商品介绍商品介绍商品介绍商品介绍",
//                "http://7xldo6.com2.z0.glb.qiniucdn.com/Fg0GGFpWR4zesklv-xXQAzW1rD9N",
//                "https://www.baidu.com", 1, 32, 1, 2, 123321, 35, 500, null,true)
//
//        return arrayOf(goods1, goods2, goods3)
//    }
//
//}