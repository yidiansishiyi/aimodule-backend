package com.yidiansishiyi.aimodule.mapper;

import com.yidiansishiyi.aimodule.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @Entity com.yidiansishiyi.aimodule.model.entity.User
 */
public interface UserMapper extends BaseMapper<User> {

    List<String> getSqlTable(String databaseName);
    Map<String,String> getSqlStructure(String tableName);
    Map<String,Object> userListzer();

}




