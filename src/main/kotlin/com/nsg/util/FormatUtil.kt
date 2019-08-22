package com.nsg.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by lijc on 16/3/2.
 */
fun Long.dateFormatLongToString(): String {
    val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    return df.format(Date(this))
}
