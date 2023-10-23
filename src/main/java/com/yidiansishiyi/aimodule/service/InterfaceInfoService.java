package com.yidiansishiyi.aimodule.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yidiansishiyi.aimodule.model.dto.interfaceinfo.InterfaceInfoAddRequest;
import com.yidiansishiyi.aimodule.model.entity.InterfaceInfo;

import javax.servlet.http.HttpServletRequest;

/**
 * 接口信息服务
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public interface InterfaceInfoService extends IService<InterfaceInfo> {

    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);

    Long addInterfaceInfo(InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request);

}
