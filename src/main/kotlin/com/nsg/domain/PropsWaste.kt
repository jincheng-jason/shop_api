package com.nsg.domain

/**
 * Created by lijc on 16/4/27.
 */

data class PropsWaste(
        var propsWasteId: Long,
        var propsSn: String,
        var createTime: Long,
        var wasteTime: Long,
        var propsId: Long,
        var description: String,
        var userItemId: Long,
        var propsName: String,
        var userId: Long,
        var userNickName: String,
        var wasteState: Int,
        var createUserName: String,
        var type: Int
)