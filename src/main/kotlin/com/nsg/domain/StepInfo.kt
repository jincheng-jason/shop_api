package com.nsg.domain

/**
 * Created by lijc on 2016/10/18.
 */

data class StepInfo(
        val time: String,
        val context: String
) {
    private constructor() : this("", "")
}