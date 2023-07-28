package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.entity.Wmsensitive;


/**
 * @author zeroc
 * @description 针对表【wmSensitive(铭感词表)】的数据库操作Service
 * @createDate 2023-06-28 14:58:53
 */
public interface WmsensitiveService extends IService<Wmsensitive> {
    boolean checkSensitiveWords(String args);
}
