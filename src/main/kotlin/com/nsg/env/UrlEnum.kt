package com.nsg.env

/**
 * Created by lijc on 16/4/14.
 */
enum class UrlEnum(val url: String) {

    //支付中心请求域名
    PAYMENT_CENTER_URL("https://test-pay.9h-sports.com"),
//        PAYMENT_CENTER_URL("https://pay.9h-sports.com"),

    //虚拟道具绑定用户地址
    USER_ITEM_URL("http://123.59.84.71:8888/user/v1/items/add"),
//        USER_ITEM_URL("http://10.10.152.93:8080/user/v1/items/add"),

    //购买虚拟物品增加竞猜奖池金额地址
    GUESS_URL("http://123.59.61.245:9090/v1/jackpot"),
//    GUESS_URL("http://10.10.152.93:9090/v1/jackpot"),

    //国安虚拟道具绑定用户地址
    GUOAN_USER_ITEM_URL("http://123.59.84.66:8082/users/items/add"),
//    GUOAN_USER_ITEM_URL("http://10.10.150.14:8082/users/items/add"),

    //获取用户信息地址
    USER_INFO_URL("http://123.59.61.160/v1/user/users/"),
//        USER_INFO_URL("http://10.10.152.93:8080/user/v1/users/"),

    //获取国安用户信息地址
    GUOAN_USER_INFO_URL("http://123.59.84.66:8082/users/user-swap-nhid/"),
//    GUOAN_USER_INFO_URL("http://10.10.150.14:8082/users/user-swap-nhid/"),

    //发送系统通知地址
    SYSTEM_MESSAGE_URL("http://123.59.84.71:8888/user/v1/messages/internal"),
//    SYSTEM_MESSAGE_URL("http://10.10.152.93:8080//user/v1/messages/internal"),

    //快递100 身份Key值
    KUAIDI100KEY("e5281ae73070caf3")

}