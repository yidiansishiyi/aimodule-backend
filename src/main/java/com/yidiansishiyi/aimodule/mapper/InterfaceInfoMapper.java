package com.yidiansishiyi.aimodule.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yidiansishiyi.aimodule.model.entity.InterfaceInfo;

import java.util.Map;

/**
 * 接口信息 Mapper
 */
public interface InterfaceInfoMapper extends BaseMapper<InterfaceInfo> {

    void addInterfaceTable(Long interfaceId);

    String getInterfaceTable(Long interfaceId);

    Map<String, String> getUserInterfaceInfoTemplate();

    void addInterfaceTables(String interfaceTable);

}
