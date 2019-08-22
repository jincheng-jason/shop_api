package com.nsg.domain

/**
 * Created by lijc on 16/3/25.
 */
enum class OrderItemType(val type: String) {
    RATED("rated"),
    UNRATE("unrate"),
    UNSHIP("unship")
}