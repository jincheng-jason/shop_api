package com.nsg.domain

/**
 * Created by lijc on 16/3/25.
 */
enum class OrderType(val type: String) {
    //type={unpay,unship,unrecv(未收货),unrate,rated,dealclose,deleted} 默认 unpay
    ALL("all"),
    UNPAY("unpay"),
    CHECKING("checking"),
    UNSHIP("unship"),
    UNRECV("unrecv"),
    UNRATE("unrate"),
    RATED("rated"),
    DEALCLOSE("dealclose"),
    DELETED("deleted")
}