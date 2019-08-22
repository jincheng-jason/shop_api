package com.nsg.mapper

import com.nsg.domain.Image

/**
 * Created by lijc on 16/3/10.
 */

interface ImageMapper {

    fun getCommentImages(commentId: Long): Array<Image>

    fun save(image: Image): Int

    fun saveCommentRefImage(commentRefImage: Map<String, Any>)

    fun getCommendImages(map: Map<String, Any>): List<Image>
}