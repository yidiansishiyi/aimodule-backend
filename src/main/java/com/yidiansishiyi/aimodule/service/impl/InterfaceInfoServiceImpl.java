package com.yidiansishiyi.aimodule.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.yidiansishiyi.aimodule.common.ErrorCode;
import com.yidiansishiyi.aimodule.exception.BusinessException;
import com.yidiansishiyi.aimodule.mapper.InterfaceInfoMapper;
import com.yidiansishiyi.aimodule.model.dto.interfaceinfo.InterfaceInfoAddRequest;
import com.yidiansishiyi.aimodule.model.entity.InterfaceInfo;
import com.yidiansishiyi.aimodule.model.entity.User;
import com.yidiansishiyi.aimodule.service.InterfaceInfoService;
import com.yidiansishiyi.aimodule.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 接口信息服务实现类
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Slf4j
@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo>
    implements InterfaceInfoService {

    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;

    @Resource
    private UserService userService;


    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add) {
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String name = interfaceInfo.getName();
        // 创建时，所有参数必须非空
        if (add) {
            if (StringUtils.isAnyBlank(name)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
        if (StringUtils.isNotBlank(name) && name.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "名称过长");
        }
    }

//    @Transactional
//    @Override
//    public Long addInterfaceInfo(InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
//        InterfaceInfo interfaceInfo = new InterfaceInfo();
//        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);
//        // 校验
//        validInterfaceInfo(interfaceInfo, true);
//        User loginUser = userService.getLoginUser(request);
//        interfaceInfo.setUserId(loginUser.getId());
//        Long interfaceInfoId = interfaceInfo.getId();
//        // 创建用户接口表
//        addInterfaceTable(interfaceInfoId);
//        // 插入接口信息到接口表
//        boolean result = save(interfaceInfo);
//        if (!result) {
//            throw new BusinessException(ErrorCode.OPERATION_ERROR);
//        }
//        long newInterfaceInfoId = interfaceInfo.getId();
//        return null;
//    }
//

    @Transactional
    @Override
    public Long addInterfaceInfo(InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);
        // 校验
        validInterfaceInfo(interfaceInfo, true);
        User loginUser = userService.getLoginUser(request);
        interfaceInfo.setUserId(loginUser.getId());

        // 插入接口信息到接口表
        boolean result = save(interfaceInfo);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }

        Long interfaceInfoId = interfaceInfo.getId();

        // 创建用户接口表
        boolean success = addInterfaceTable(interfaceInfoId);
        if (!success) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口信息表创建失败");
        }

        return interfaceInfoId;
    }
    boolean addInterfaceTable(Long id){
        // 根据模板创建对应接口表格
        interfaceInfoMapper.addInterfaceTable(id);
        // 检查是否创建成功如果返回表名创建成功否则创建失败
        String interfaceTable = interfaceInfoMapper.getInterfaceTable(id);
        return !interfaceTable.isEmpty();
    }

}




