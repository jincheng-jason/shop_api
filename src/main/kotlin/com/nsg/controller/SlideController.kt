package com.nsg.controller

import com.nsg.domain.Image
import com.nsg.domain.form.ShopIds
import com.nsg.service.ImageService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse

/**
 * Created by lijc on 16/2/22.
 */

@Api(basePath = "/v1/mall/csl", value = "slide", description = "跑马灯API", produces = "application/json", position = 7)
@RestController
@RequestMapping("/v1/mall/csl")
class SlideController @Autowired constructor(
        var imageService: ImageService
) {

    val shopId = ShopIds.CSL.shopId
    private val LOGGER = LoggerFactory.getLogger(SlideController::class.java)

    @ApiOperation(httpMethod = "GET", value = "获取商城跑马灯", response = Array<Image>::class)
    @RequestMapping(value = "/slide", method = arrayOf(RequestMethod.GET))
    fun getSlide(response: HttpServletResponse): List<Image> {
        //        val img1 = Image(1, "评论图片1", "评论图片1", "http://7xldo6.com2.z0.glb.qiniucdn.com/huizhang.png", 2, "csl://goods/5")
        //        val img2 = Image(2, "评论图片2", "评论图片2", "http://7xldo6.com2.z0.glb.qiniucdn.com/qiandao.png", 2, "csl://goods/6")
        //        val img3 = Image(3, "评论图片3", "评论图片3", "http://7xldo6.com2.z0.glb.qiniucdn.com/Fg0GGFpWR4zesklv-xXQAzW1rD9N", 1, "http://7xldo6.com2.z0.glb.qiniucdn.com/Fg0GGFpWR4zesklv-xXQAzW1rD9N")

        val images = imageService.getCommendImages(mapOf("shopId" to shopId))

        response.status = HttpServletResponse.SC_OK
        return images
    }

}