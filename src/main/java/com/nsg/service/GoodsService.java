package com.nsg.service;

import com.nsg.domain.Goods;
import com.nsg.domain.Image;
import com.nsg.domain.SkuAttr;
import com.nsg.domain.SkuInfo;
import com.nsg.mapper.GoodsMapper;
import com.nsg.mapper.SkuAttrMapper;
import com.nsg.mapper.SkuInfoMapper;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;

/**
 * Created by lijc on 16/3/16.
 */
@Transactional
@Service
public class GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private SkuAttrMapper skuAttrMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    public List<Goods> getCommendGoodsList(Map<String, Object> map) {
        return goodsMapper.getCommendGoodsList(map);
    }

    public Goods getById(Long goodsId) {
        return goodsMapper.getById(goodsId);
    }

    public List<Image> getGoodsImgs(Long goodsId) {
        return goodsMapper.getGoodsImgs(goodsId);
    }

    @Nullable
    public List<SkuAttr> getGoodsAttrs(long goods_id) {
        return skuAttrMapper.getGoodsAttrs(goods_id);
    }

    public List<SkuInfo> getSkuInfos(long goods_id) {
        return skuInfoMapper.getSkuInfos(goods_id);
    }

    public int getGoodsStock(long goods_id) {
        Object stock = goodsMapper.getGoodsStock(goods_id);
        return stock == null ? 0 : (int) stock;
    }

    //根据goodsid查此商品所有sku的最高价和最低价,showprice和salesprice,并更新到goods表中
    public Map<String, String> getPriceSummaryStatistics(long goods_id) {
        List<SkuInfo> skuInfos = skuInfoMapper.getSkuInfos(goods_id);
        IntSummaryStatistics salesPriceSummaryStatistics = skuInfos.stream().map(SkuInfo::getSalesPrice).collect(IntSummaryStatistics::new, IntSummaryStatistics::accept, IntSummaryStatistics::combine);
        IntSummaryStatistics showPriceSummaryStatistics = skuInfos.stream().map(SkuInfo::getShowPrice).collect(IntSummaryStatistics::new, IntSummaryStatistics::accept, IntSummaryStatistics::combine);
        int maxSalesPrice = salesPriceSummaryStatistics.getMax();
        int minSalesPrice = salesPriceSummaryStatistics.getMin();
        int maxShowPrice = showPriceSummaryStatistics.getMax();
        int minShowPrice = showPriceSummaryStatistics.getMin();

        //保留两位小数
        DecimalFormat df = new DecimalFormat("######0.00");
        String minSalesPriceYuan = df.format((double) minSalesPrice / 100);
        String maxSalesPriceYuan = df.format((double) maxSalesPrice / 100);
        String minShowPriceYuan = df.format((double) minShowPrice / 100);
        String maxShowPriceYuan = df.format((double) maxShowPrice / 100);

        String salesPriceRange = maxSalesPrice == minSalesPrice ? minSalesPriceYuan : (minSalesPriceYuan + "-" + maxSalesPriceYuan);
        String showPriceRange = maxShowPrice == minShowPrice ? minShowPriceYuan : (minShowPriceYuan + "-" + maxShowPriceYuan);
        Map<String, String> map = new HashMap<>();
        map.put("salesPriceRange", salesPriceRange);
        map.put("showPriceRange", showPriceRange);
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("goodsId", goods_id);
        updateMap.put("salesPrice", salesPriceRange);
        updateMap.put("showPrice", showPriceRange);
        goodsMapper.updateGoodsPrice(updateMap);
        return map;
    }

//    public void newProps() {
//
//    }
}
