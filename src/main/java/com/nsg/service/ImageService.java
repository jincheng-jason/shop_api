package com.nsg.service;

import com.nsg.domain.Image;
import com.nsg.mapper.ImageMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Created by lijc on 16/3/23.
 */
@Transactional
@Service
public class ImageService {

    @Autowired
    private ImageMapper imageMapper;

    public int save(Image image) {
        return imageMapper.save(image);
    }

    public void saveCommentRefImage(@NotNull Map<String, Object> commentRefImage) {
        imageMapper.saveCommentRefImage(commentRefImage);
    }

    public List<Image> getCommendImages(Map<String, Object> map) {
        return imageMapper.getCommendImages(map);
    }
}
