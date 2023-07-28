package com.yidiansishiyi.aimodule.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yidiansishiyi.aimodule.model.entity.Post;
import com.yidiansishiyi.aimodule.model.entity.PostFavour;
import com.yidiansishiyi.aimodule.model.entity.User;
import com.yidiansishiyi.aimodule.model.entity.Wmsensitive;


/**
 * 帖子收藏服务
 *
 * @author sanqi
 *   
 */
public interface PostFavourService extends IService<PostFavour> {

    /**
     * 帖子收藏
     *
     * @param postId
     * @param loginUser
     * @return
     */
    int doPostFavour(long postId, User loginUser);

    /**
     * 分页获取用户收藏的帖子列表
     *
     * @param page
     * @param queryWrapper
     * @param favourUserId
     * @return
     */
    Page<Post> listFavourPostByPage(IPage<Post> page, Wrapper<Post> queryWrapper,
                                    long favourUserId);

    /**
     * 帖子收藏（内部服务）
     *
     * @param userId
     * @param postId
     * @return
     */
    int doPostFavourInner(long userId, long postId);

    /**
     * @author zeroc
     * @description 针对表【wmSensitive(铭感词表)】的数据库操作Service
     * @createDate 2023-06-28 14:58:53
     */
    interface WmsensitiveService extends IService<Wmsensitive> {

    }
}
