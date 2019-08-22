package com.nsg.controller

import com.nsg.domain.data.CommentData
import com.nsg.domain.data.GoodsData
import com.nsg.service.CommentService
import com.nsg.service.GoodsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

/**
 * Created by lijc on 16/4/7.
 */

@Controller
@RequestMapping("/v1/share/mall/csl")
class ShareController
@Autowired constructor(
        var goodsService: GoodsService,
        var commentService: CommentService
) {

    @RequestMapping(value = "/test.html", method = arrayOf(RequestMethod.GET))
    fun getIndex(model: Model): String {
        model.addAttribute("message", "HELLO!")
        return "test"
    }

    @RequestMapping(value = "goods/{goods_id}/share", method = arrayOf(RequestMethod.GET))
    fun shareGoods(model: Model,
                   @PathVariable goods_id: Long): String {

        try {
            var goods = goodsService.getById(goods_id)
            var goodsData: GoodsData
            var goodPercent: Int = 0
            if (goods == null) {
                goodsData = GoodsData(0, 0, 0, "", "", "", "", "", "", 0, 0, 0, 0, 0, 0, 0, null, null, 0, 0, 0, 0, 0, "")
            } else {
                var goodsImgs = goodsService.getGoodsImgs(goods_id)

                var map = mapOf("goodsId" to goods_id, "timestamp" to null, "limit" to 5)
                var comments = commentService.getCommentsByGoods(map)
                var commentCounts: List<Int>? = commentService.getGoodsCommentCounts(goods_id)

                var totalCommentsNum = when {
                    (commentCounts == null || commentCounts.isEmpty()) -> 0
                    else -> commentCounts[0]
                }
                var goodCommentsNum = when {
                    (commentCounts == null || commentCounts.size < 2) -> 0
                    else -> commentCounts[1]
                }
                var normalCommentsNum = when {
                    (commentCounts == null || commentCounts.size < 3) -> 0
                    else -> commentCounts[2]
                }
                var badCommentsNum = when {
                    (commentCounts == null || commentCounts.size < 4) -> 0
                    else -> commentCounts[3]
                }
                var picCommentsNum = when {
                    (commentCounts == null || commentCounts.size < 5) -> 0
                    else -> commentCounts[4]
                }
                var specUrl = ""

                //根据goodsid查此商品所有sku的最高价和最低价,showprice和salesprice

                goodPercent = (goodCommentsNum.toDouble() / totalCommentsNum.toDouble() * 100 ).toInt()

                goodsData = GoodsData.of(goods, goodsImgs, CommentData.array(comments),
                        totalCommentsNum, goodCommentsNum, normalCommentsNum, badCommentsNum, picCommentsNum)

            }


            model.addAttribute("goods", goodsData)
            model.addAttribute("goodPercent", goodPercent)

        } catch(e: Exception) {
            e.printStackTrace()
        }
        return "shareGoods"
    }

}