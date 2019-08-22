package com.nsg.service;

import com.alibaba.fastjson.JSON;
import com.nsg.domain.*;
import com.nsg.domain.form.CartItemForm;
import com.nsg.domain.form.OrderForm;
import com.nsg.domain.form.PaymentAppIds;
import com.nsg.domain.form.ShopIds;
import com.nsg.env.UrlEnum;
import com.nsg.mapper.*;
import net.dongliu.requests.Requests;
import net.dongliu.requests.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Created by lijc on 16/3/16.
 */

@Transactional
@Service
public class OrderService {

    private Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private SkuAttrMapper skuAttrMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private CartItemMapper cartItemMapper;

    @Autowired
    private PropsMapper propsMapper;

    public Order getById(long orderId) {
        return orderMapper.getById(orderId);
    }

    public List<Order> findAll(Map<String, Object> map) {
        return orderMapper.findAll(map);
    }

    public int save(Order order) {
        return orderMapper.save(order);
    }

    public int saveSub(SubOrder subOrder) {
        return orderMapper.saveSub(subOrder);
    }

    public int getSkuStore(Long skuId) {
        Object store = orderMapper.getSkuStore(skuId);
        return store == null ? 0 : (int) store;
    }

    public Order getOrderByOrderSN(String orderSN) {
        return orderMapper.getByOrderSN(orderSN);
    }

    public List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        return orderItemMapper.getOrderItemsByOrderId(orderId);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public int savePayment(Payment payment) {

        int i = paymentMapper.save(payment);
        orderMapper.updateOrderBySN(payment);

        LOGGER.info("支付成功: " + payment.getOrderSN());

        //如果支付成功,则遍历orderitem,分虚拟物品和实体物品,虚拟物品通过接口绑定用户,实体物品调用物流接口
        //通过ordersn获取所有orderitem
        Order order = orderMapper.getByOrderSN(payment.getOrderSN());
        if (order == null) {
            LOGGER.info("支付后无法找到此订单号的订单:" + payment.getOrderSN());
            return i;
        }

        LOGGER.info("-------开始实物和虚拟卡处理流程-------");

        List<OrderItem> orderItems = orderItemMapper.getOrderItemsByOrderId(order.getOrderId());
        for (OrderItem orderItem : orderItems) {
            LOGGER.info("OrderItem:" + JSON.toJSONString(orderItem));
            if (orderItem.isVirtual()) {
                //如果是虚拟物品,通过接口绑定用户

                Long skuId = orderItem.getSkuId();
                int type = PropsWasteType.SALES.getType(); //type 1表示为售卖流水,2表示中奖流水
                if (bindUserItems(skuId, order.getUserId(), orderItem.getBuyNum(), type, "用户买入虚拟物品", order.getShopId(), null, null, null)) {
                    //绑定成功

                    //将orderitem状态置为unrate
                    Map<String, Object> typeMap = new HashMap<>();
                    typeMap.put("itemType", OrderItemType.UNRATE.getType());
                    typeMap.put("orderItemId", orderItem.getOrderItemId());
                    orderItemMapper.updateItemType(typeMap);
                    LOGGER.info("虚拟物品状态置为unrate:orderItemId:" + orderItem.getOrderItemId());

                    if (order.getShopId() == ShopIds.CSL.getShopId()) {
                        //推送虚拟物品购买成功系统消息
                        String content = "您已成功购买" + orderItem.getGoodsName() + "，前往【我的中超】-->【我的道具】即可查看使用。";
                        Map<String, Object> messageMap = new HashMap<>();
                        messageMap.put("isAll", 0);
                        messageMap.put("type", "common");
                        messageMap.put("toUserId", order.getUserId());
                        messageMap.put("content", content);
                        messageMap.put("orderId", order.getOrderId());
                        sendSystemMessage(messageMap);
                    }

                }

            } else {
                //如果是实体商品,则orderitem状态置为unship状态
                Map<String, Object> typeMap = new HashMap<>();
                typeMap.put("itemType", OrderItemType.UNSHIP.getType());
                typeMap.put("orderItemId", orderItem.getOrderItemId());
                orderItemMapper.updateItemType(typeMap);
            }

        }

        LOGGER.info("-----实体和虚拟卡流程结束-------");

        //查询此订单下的所有orderitem是否都是unrate,则此订单状态置为unrate
        List<OrderItem> orderItemList = orderItemMapper.getOrderItemsByOrderId(order.getOrderId());
        long unrateCount = orderItemList.stream().filter(it -> it.getItemType().equals(OrderItemType.UNRATE.getType())).count();
        long unshipCount = orderItemList.stream().filter(it -> it.getItemType().equals(OrderItemType.UNSHIP.getType())).count();
        LOGGER.info("------订单中unrate数量:" + unrateCount + ";unship数量:" + unshipCount);
        Boolean isAllUnrate = false;
        if (unrateCount == orderItemList.size()) {
            isAllUnrate = true;
        }
        if (isAllUnrate) {
            Map<String, Object> map = new HashMap<>();
            map.put("orderId", order.getOrderId());
            map.put("type", OrderType.UNRATE.getType());
            map.put("receivedTime", new Date().getTime());
            orderMapper.changeOrderType(map);
        }
        //如果订单中有orderitem状态为unship,则订单状态为unship
        Boolean isAnyUnship = false;
        if (unshipCount > 0) {
            isAnyUnship = true;
        }
        if (isAnyUnship) {
            Map<String, Object> map = new HashMap<>();
            map.put("orderId", order.getOrderId());
            map.put("type", OrderType.UNSHIP.getType());
            orderMapper.changeOrderType(map);
        }
        LOGGER.info("----订单状态修改完毕:orderId:" + order.getOrderId());
        return i;
    }

    public Map<String, Object> getUserInfo(long user_id) {
        String userUrl = UrlEnum.USER_INFO_URL.getUrl() + user_id;

        LOGGER.info(userUrl);
        Response<String> response = Requests.get(userUrl).verify(false).text();
        LOGGER.info("获取中超用户uuid:" + response.getBody());
        Map<String, Object> userMap = (Map<String, Object>) JSON.parse(response.getBody());
        return userMap;

    }

    public Map<String, Object> getGuoanUserInfo(long user_id) {
        String userUrl = UrlEnum.GUOAN_USER_INFO_URL.getUrl() + user_id;

        LOGGER.info(userUrl);
        Response<String> response = Requests.get(userUrl).verify(false).text();
        LOGGER.info("获取国安用户uuid:" + response.getBody());
        Map<String, Object> userMap = (Map<String, Object>) JSON.parse(response.getBody());
        return userMap;

    }

    public String getGuoanNhid(long user_id) {

        String userUrl = UrlEnum.GUOAN_USER_INFO_URL.getUrl() + user_id;
        LOGGER.info(userUrl);
        Response<String> response = Requests.get(userUrl).verify(false).text();
        LOGGER.info("获取国安用户uuid:" + response.getBody());
        Map<String, Object> userMap = (Map<String, Object>) JSON.parse(response.getBody());
        if ((Integer) userMap.get("errCode") == 0) {
            return (String) userMap.get("tag");
        } else {
            return null;
        }
    }

    public boolean sendSystemMessage(Map<String, Object> map) {
        String messageUrl = UrlEnum.SYSTEM_MESSAGE_URL.getUrl();
        LOGGER.info(messageUrl);

        long orderId = (long) map.get("orderId");
        map.remove("orderId");

        String json = JSON.toJSONString(map);
        Response<String> response = Requests.post(messageUrl).data(json).addHeader("Content-Type", "application/json").verify(false).text();
        LOGGER.info(response.getBody());
        Map<String, Object> resultMap = (Map<String, Object>) JSON.parse(response.getBody());
        String operCode = (String) resultMap.get("oper_code");
        if (operCode.equals("1")) {
            LOGGER.info("--------发送系统通知成功:" + json);

            //发送成功,订单消息发送状态置为true
            orderMapper.updateMessageStatus(orderId);

            return true;
        } else {
            LOGGER.info("--------发送系统通知失败:" + json);
            return false;
        }
    }

    public boolean bindUserItems(Long skuId, Long userId, int buyNum, int type, String description, long shopId, String itemKey, String nhId, String nickName) {

        if (itemKey == null || itemKey.equals("")) {
            itemKey = propsMapper.getBySkuId(skuId).getSn();
        }
//        Props props = propsMapper.getBySkuId(skuId);

        String url;

        if (shopId == ShopIds.CSL.getShopId()) {
            url = UrlEnum.USER_ITEM_URL.getUrl();
        } else if (shopId == ShopIds.GUOAN.getShopId()) {
            url = UrlEnum.GUOAN_USER_ITEM_URL.getUrl();
        } else {
            return false;
        }


        Map<String, Object> map = new HashMap<>();
        map.put("itemTypeKey", itemKey);
        if (nhId == null || nhId.equals("")) {
            map.put("userId", userId);
        } else {
            map.put("nhId", nhId);
        }

        map.put("count", buyNum);
        String json = JSON.toJSONString(map);
        LOGGER.info("--------绑定虚拟物品:" + url + "," + json);
        Response<String> response = Requests.post(url).data(json).addHeader("Content-Type", "application/json").verify(false).text();
        LOGGER.info(response.getBody());
        Map<String, Object> resultMap = (Map<String, Object>) JSON.parse(response.getBody());
        String operCode = resultMap.get("oper_code") == null ? "" : resultMap.get("oper_code").toString();
        String errCode = resultMap.get("errCode") == null ? "" : resultMap.get("errCode").toString();
        if (operCode.equals("1") || errCode.equals("0")) {
            LOGGER.info("--------绑定虚拟物品成功:" + json);
            //绑定成功后,更新流水单waste,记录绑定物品id,itemId

            String dataJson = JSON.toJSONString(resultMap.get("data"));
            List<Map<String, Object>> dataList = JSON.parseObject(dataJson, List.class);

            long itemId = 0;

            if (dataList != null && !dataList.isEmpty()) {
                Map<String, Object> dataMap = dataList.get(0);

                Object itemObject = dataMap.get("itemId");

                if (itemObject != null) {
                    if (itemObject instanceof String) {
                        itemId = Long.valueOf(itemObject.toString());
                    } else if (itemObject instanceof Long) {
                        itemId = (long) itemObject;
                    } else {
                        itemId = 0;
                    }
                }


            }

            if (nickName == null || nickName.equals("")) {
                //获取用户信息
                Map<String, Object> userMap = getUserInfo(userId);
                if (shopId == ShopIds.CSL.getShopId()) {
                    userMap = getUserInfo(userId);
                } else if (shopId == ShopIds.GUOAN.getShopId()) {
                    userMap = getGuoanUserInfo(userId);
                }

                Map<String, Object> data = (Map<String, Object>) userMap.get("data");
                if (data != null && !data.isEmpty()) {
                    nickName = data.get("nickName") == null ? "" : (String) data.get("nickName");
                    nhId = data.get("nhId") == null ? "" : (String) data.get("nhId");
                } else {
                    nhId = userMap.get("tag") == null ? "" : (String) userMap.get("tag");
                }
            }

            if (buyNum < 1) {
                buyNum = 1;
            }

            Map<String, Object> wasteMap = new HashMap<>();
            wasteMap.put("itemKey", itemKey);
            wasteMap.put("itemId", itemId);
            wasteMap.put("type", type);
            wasteMap.put("count", buyNum);
            wasteMap.put("userId", userId);
            wasteMap.put("nhId", nhId);
            wasteMap.put("nickName", nickName);
            wasteMap.put("wasteTime", new Date().getTime());
            wasteMap.put("description", description);
            wasteMap.put("shopId", shopId);
            propsMapper.updateWaste(wasteMap);


            if (shopId == ShopIds.CSL.getShopId()) {
                //中超购买虚拟物品后,赠送机会牌,并增加中超竞猜奖池金额
                if (operCode.equals("1")) {
                    map.remove("itemTypeKey");
                    map.put("itemTypeKey", "GuessCardForEver");
                    map.put("count", buyNum);
                    String giftJson = JSON.toJSONString(map);
                    LOGGER.info("--------赠送机会牌:" + url + "," + giftJson);
                    Response<String> giftResponse = Requests.post(url).data(giftJson).addHeader("Content-Type", "application/json").verify(false).text();
                    LOGGER.info(giftResponse.getBody());

                    Map<String, Object> guessMap = new HashMap<>();
                    guessMap.put("operType", 2);
                    guessMap.put("money", 10 * buyNum);
                    guessMap.put("userId", nhId == null ? userId : nhId);
                    guessMap.put("userNickName", nickName);
                    guessMap.put("count", buyNum);

                    String guessJson = JSON.toJSONString(guessMap);
                    LOGGER.info(String.format("------增加中超竞猜奖池金额: %s", guessJson));

                    Response<String> guessResponse = Requests.post(UrlEnum.GUESS_URL.getUrl()).data(guessJson).addHeader("Content-Type", "application/json").verify(false).text();
                    LOGGER.info(guessResponse.getBody());

                }
            }


            return true;
        } else {
            LOGGER.info("--------绑定虚拟物品失败:" + json);
            return false;
        }


    }

    public int batchSaveOrderItem(@NotNull List<OrderItem> orderItemArray) {
        return orderItemMapper.batchSave(orderItemArray);
    }

    public SkuInfo getSkuById(long skuId) {
        return skuInfoMapper.getSkuInfoById(skuId);
    }

    public boolean isSkuInPromotion(long skuId) {
        int count = skuInfoMapper.countSkuInPromotion(skuId);
        if (count > 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isUserSkuInPromotion(Map<String, Object> map) {
        int count = skuInfoMapper.countUserSkuInPromotion(map);
        if (count > 0) {
            return true;
        } else {
            return false;
        }
    }

    public Order addOrder(@NotNull OrderForm orderForm) {

        //创建订单
        LocalDateTime now = LocalDateTime.now();
        ZoneId zoneId = ZoneId.systemDefault();
        Long date = now.atZone(zoneId).toInstant().toEpochMilli();

        Long overdueTime = orderForm.getOverdueTime();
        String orderSN = orderForm.getOrderSN();
        String type = OrderType.UNPAY.getType();
        Long userId = orderForm.getUserId();
        String uuid = orderForm.getUuidUserId();

        Long addressId = orderForm.getAddressId();
        Address address = orderForm.getAddress();
        int goodsPrice = orderForm.getGoodsPrice();
        int postage = orderForm.getPostage();
        int totalPrice = orderForm.getTotalPrice();
        Long shopId = orderForm.getShopId();

        List<CartItemForm> cartItems = orderForm.getCartItems();

        String description = orderForm.getDescription();
//        String goodsName = "";
//        for (CartItemForm cartItem : cartItems) {
//            goodsName += "," + cartItem.getGoods().getGoodsName();
//        }
//        description += goodsName.replaceFirst(",", "") + "等" + cartItems.size() + "件商品";


        Order order = new Order(0, orderSN, shopId, userId, addressId, date, type, goodsPrice, postage, totalPrice, address, null, description, overdueTime, uuid, "", 0, false);
        orderMapper.save(order);

        //创建子订单,创建单个的子订单
        SubOrder subOrder = new SubOrder(0, order.getOrderId(), null, null, null, null, null, 0, null, null, null, null, order.getGoodsPrice(), order.getPostage(), order.getTotalPrice(), date);
        orderMapper.saveSub(subOrder);

        //orderitem得到子订单id,sub_order_id
        List<OrderItem> orderItemList = new ArrayList<OrderItem>();

        List<Long> cartItemIds = new ArrayList<>();

        //如果库存足够,则cartitem进入orderitem,首先得到cartitem的skuid
        for (CartItemForm it : cartItems) {

            int buyNum = it.getBuyNum();
            Long cartItemId = it.getCartItemId();

            //根据cartitemid和buyNum,查出cartitem,转为orderitem
            CartItem cartItem = cartItemMapper.getById(cartItemId);

            SkuInfo skuInfo = skuInfoMapper.getSkuInfoById(cartItem.getSkuId());
            Goods goods = goodsMapper.getById(cartItem.getGoodsId());

            orderItemList.add(new OrderItem(0, subOrder.getSubOrderId(), cartItem.getSkuId(), null, buyNum, cartItem.getGoodsId(), goods.getGoodsName(), skuInfo.getSalesPrice(), date, OrderItemType.UNSHIP.getType(), goods.isVirtual()));
            cartItemIds.add(it.getCartItemId());//减库存,加销量
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("skuId", it.getSkuId());
            map.put("buyNum", it.getBuyNum());
            orderMapper.reduceStore(map);
            orderMapper.increaseSalesNum(map);
            //如果减完库存之后库存为零,则此商品下架失效
            skuInfo = skuInfoMapper.getSkuInfoById(cartItem.getSkuId());
            if (skuInfo.getStoreNum() == 0) {

                Map<String, Object> skuMap = new HashMap<>();
                skuMap.put("skuId", cartItem.getSkuId());
                skuMap.put("state", 0);
                skuInfoMapper.updateSkuState(skuMap);
                LOGGER.info("此SKU库存已空,已下架:" + cartItem.getSkuId());
            }
        }
        //存入orderitem表
        orderItemMapper.batchSave(orderItemList);

        //删除购物车表中的cartitem
        cartItemMapper.batchDeleteCartItems(cartItemIds);

        order.setGoodsArray(orderItemList);
        return order;
    }

    @NotNull
    public List<SkuAttr> getSkuAttrs(long skuId) {

        return skuAttrMapper.getSkuAttrs(skuId);
    }

    public void changeOrderItemType(@NotNull Map<String, Object> map) {
        orderMapper.changeOrderItemType(map);
    }

    @NotNull
    public Payment getPayment(long order_id) {
        return paymentMapper.getPayStatus(order_id);
    }

    public List<Order> getOrdersByType(String orderType) {
        return orderMapper.getOrdersByType(orderType);
    }

    public Payment getPayStatus(long orderId, String payType) {
        try {

            LOGGER.info("查询订单支付状态:" + orderId + "," + payType);
            Payment payment = getPayment(orderId);
            if (payment.getPaymentId() == 0L) {
                String url;
                long shopId = payment.getShopId();
                int appId = PaymentAppIds.CSL.getAppId();
                if (shopId == ShopIds.GUOAN.getShopId()) {
                    appId = PaymentAppIds.GUOAN.getAppId(); //国安支付中心appid为3
                }
                if (payType.equals(PayType.ALIPAY.getType())) {
                    url = UrlEnum.PAYMENT_CENTER_URL.getUrl() + "/alipay/orderquery?out_trade_no=" + payment.getOrderSN() + "&appid=" + appId;
                } else if (payType.equals(PayType.WECHAT.getType())) {
                    url = UrlEnum.PAYMENT_CENTER_URL.getUrl() + "/wxpay/orderquery?out_trade_no=" + payment.getOrderSN() + "&appid=" + appId;
                } else {
                    return new Payment(0, 0, 0, "", 0, 0, "", 0, null, null, null, null, null);
                }

                LOGGER.info(url);

                Response<String> response = Requests.get(url).verify(false).text();
                LOGGER.info(response.getBody());
                Map<String, Object> map = (Map<String, Object>) JSON.parse(response.getBody());
                int code = (int) map.get("code");
                String msg = (String) map.get("msg");
                if (code != 0) {

                    //如果返回值8011,8013,则订单未支付,将订单状态置回为 unpay
                    if (code == 8011 || code == 8013) {
                        LOGGER.info("支付中心返回订单未支付,订单状态改为待支付:" + payment.getOrderSN());
                        Map<String, Object> parameterMap = new HashMap<>();
                        parameterMap.put("orderId", orderId);
                        parameterMap.put("type", OrderType.UNPAY.getType());
                        changeOrderType(parameterMap);
                        LOGGER.info("支付中心返回订单未支付:" + response.getBody());
//                    response.status = HttpServletResponse.SC_BAD_REQUEST
//                    response.addHeader("X-Err-Message", URLEncoder.encode("订单未支付", "UTF-8"))
//                    response.addHeader("X-Err-Code", code.toString())
                        return new Payment(-1, code, 0, msg, 0, 0, "", 0, null, null, null, null, null);
                    }

                    LOGGER.info("支付中心返回失败信息:" + response.getBody());
//                response.status = HttpServletResponse.SC_BAD_REQUEST
//                response.addHeader("X-Err-Message", URLEncoder.encode(msg, "UTF-8"))
//                response.addHeader("X-Err-Code", code.toString())
                    return new Payment(0, code, 0, msg, 0, 0, "", 0, null, null, null, null, null);
                }
                LOGGER.info("支付中心返回成功信息:" + response.getBody());
                int paymentPrice = Integer.parseInt(map.get("totalfee").toString());
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String payTime = (String) map.get("paytime");
                long paymentTime = df.parse(payTime).getTime();
                payment = new Payment(0, ShopIds.CSL.getShopId(), 1, payment.getOrderSN(), paymentPrice, paymentTime, payType, new Date().getTime(), null, null, null, null, null);
                savePayment(payment);
                return payment;
            } else {
                return payment;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Payment(0, 0, 0, "", 0, 0, "", 0, null, null, null, null, null);
        }
    }

    @NotNull
    public List<String> getItemTypes(long order_id) {
        return orderMapper.getItemTypes(order_id);
    }

    public void changeOrderType(@NotNull Map<String, Object> map) {

        //如果是确认收货,则记录收货时间
        if (map.get("type").equals(OrderType.UNRATE.getType())) {
            long receivedTime = new Date().getTime();
            map.put("receivedTime", receivedTime);
            LOGGER.info("---确认收货,记录收货时间:" + JSON.toJSONString(map));
        }

        orderMapper.changeOrderType(map);
        if (map.get("type").equals(OrderType.DEALCLOSE.getType())
//                || map.get("type").equals(OrderType.UNPAY.getType())
                ) {
            //如果是取消订单,则加回库存
            LOGGER.info("订单被取消或已失效,释放库存:" + JSON.toJSONString(map));
            Long orderId = (Long) map.get("orderId");

            //并重置订单支付渠道
//            orderMapper.emptyPayType(orderId);

            List<OrderItem> orderItems = orderItemMapper.getOrderItemsByOrderId(orderId);
            if (orderItems != null && orderItems.size() > 0) {
                for (OrderItem orderItem : orderItems) {
                    int buyNum = orderItem.getBuyNum();
                    Long skuId = orderItem.getSkuId();
                    Map<String, Object> skuMap = new HashMap<>();
                    skuMap.put("buyNum", buyNum);
                    skuMap.put("skuId", skuId);

                    orderMapper.increaseStore(skuMap);

                    Map<String, Object> updateMap = new HashMap<>();
                    updateMap.put("skuId", skuId);
                    updateMap.put("state", 1);
                    skuInfoMapper.updateSkuState(updateMap);
                    LOGGER.info("库存还原:" + JSON.toJSONString(skuMap));
                }
            }
        }
    }

    public int getPropsWasteCount(Map<String, Object> map) {
        return propsMapper.getPropsWasteCount(map);
    }

    public String getShippingCode(long order_id) {
        return orderMapper.getShippingCode(order_id);
    }
}
