package com.yidiansishiyi.aimodule.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.Map;

@SpringBootTest
class InterfaceInfoMapperTest {

    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;

    @Test
    void testCreatSQL(){
        Map<String, String> interfaceInfoTemplate =  interfaceInfoMapper.getUserInterfaceInfoTemplate();
        System.out.println(interfaceInfoTemplate.toString()+"张三");
        String value = interfaceInfoTemplate.get("Create Table");
        String template = value.replace("template", "123456");
        interfaceInfoMapper.addInterfaceTables(template);
        System.out.println("张三");
    }

}