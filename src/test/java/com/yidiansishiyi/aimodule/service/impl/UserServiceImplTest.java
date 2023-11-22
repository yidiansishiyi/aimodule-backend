package com.yidiansishiyi.aimodule.service.impl;

import io.lettuce.core.ScriptOutputType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.io.IOException;
import java.util.HashMap;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import jodd.io.FileUtil;

@SpringBootTest
class UserServiceImplTest {

    @Resource
    private UserServiceImpl userServiceImpl;



//    @Test
//    void generateDDL() {
//        String b1 = userServiceImpl.generateDDL("CREATE TABLE `biz_entry_user` (\n" +
//                "  `id` varchar(50) NOT NULL,\n" +
//                "  `name` varchar(32) DEFAULT NULL COMMENT '姓名',\n" +
//                "  `sex` int(4) DEFAULT NULL COMMENT '性别，0：女，1：男',\n" +
//                "  `entry_time` varchar(32) DEFAULT NULL COMMENT '进驻时间',\n" +
//                "  `phone` varchar(11) DEFAULT NULL COMMENT '手机号',\n" +
//                "  `station` varchar(64) DEFAULT NULL COMMENT '工位',\n" +
//                "  `nature` varchar(255) DEFAULT NULL COMMENT '人员性质',\n" +
//                "  `work_no` varchar(64) DEFAULT NULL COMMENT '工号',\n" +
//                "  `state` int(4) DEFAULT NULL COMMENT '人员状态，1：办理中，2：已完成，3：已调离,4：待确认',\n" +
//                "  `dept_name` varchar(255) DEFAULT NULL COMMENT '所属部门名称',\n" +
//                "  `show_dept_name` varchar(255) DEFAULT NULL COMMENT '界面展示窗口部门',\n" +
//                "  `dept_id` varchar(32) DEFAULT NULL COMMENT '所属部门id',\n" +
//                "  `create_time` datetime DEFAULT current_timestamp() COMMENT '创建时间',\n" +
//                "  `create_person` varchar(32) DEFAULT NULL COMMENT '创建人',\n" +
//                "  `update_time` datetime DEFAULT NULL COMMENT '更新时间',\n" +
//                "  `update_person` varchar(32) DEFAULT NULL COMMENT '更新人',\n" +
//                "  `apply_time` varchar(32) DEFAULT NULL COMMENT '申请时间',\n" +
//                "  `grading_time` varchar(32) DEFAULT NULL COMMENT '考级时间',\n" +
//                "  `meeting_time` varchar(32) DEFAULT NULL COMMENT '上会时间',\n" +
//                "  `is_party_member` int(4) DEFAULT NULL COMMENT '是否党员',\n" +
//                "  `is_asset` int(4) DEFAULT NULL COMMENT '是否领用资产',\n" +
//                "  PRIMARY KEY (`id`) USING BTREE,\n" +
//                "  KEY `index_id` (`id`) USING BTREE\n" +
//                ") ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='进驻人员信息表'");
//        System.out.println(b1);
//    }

//    @Test
//    void extractColumns() {
//        HashMap<String, String> stringStringHashMap = userServiceImpl.extractColumns("CREATE TABLE `biz_entry_user` (\n" +
//                "  `id` varchar(50) NOT NULL,\n" +
//                "  `name` varchar(32) DEFAULT NULL COMMENT '姓名',\n" +
//                "  `sex` int(4) DEFAULT NULL COMMENT '性别，0：女，1：男',\n" +
//                "  `entry_time` varchar(32) DEFAULT NULL COMMENT '进驻时间',\n" +
//                "  `phone` varchar(11) DEFAULT NULL COMME NT '手机号',\n" +
//                "  `station` varchar(64) DEFAULT NULL COMMENT '工位',\n" +
//                "  `nature` varchar(255) DEFAULT NULL COMMENT '人员性质',\n" +
//                "  `work_no` varchar(64) DEFAULT NULL COMMENT '工号',\n" +
//                "  `state` int(4) DEFAULT NULL COMMENT '人员状态，1：办理中，2：已完成，3：已调离,4：待确认',\n" +
//                "  `dept_name` varchar(255) DEFAULT NULL COMMENT '所属部门名称',\n" +
//                "  `show_dept_name` varchar(255) DEFAULT NULL COMMENT '界面展示窗口部门',\n" +
//                "  `dept_id` varchar(32) DEFAULT NULL COMMENT '所属部门id',\n" +
//                "  `create_time` datetime DEFAULT current_timestamp() COMMENT '创建时间',\n" +
//                "  `create_person` varchar(32) DEFAULT NULL COMMENT '创建人',\n" +
//                "  `update_time` datetime DEFAULT NULL COMMENT '更新时间',\n" +
//                "  `update_person` varchar(32) DEFAULT NULL COMMENT '更新人',\n" +
//                "  `apply_time` varchar(32) DEFAULT NULL COMMENT '申请时间',\n" +
//                "  `grading_time` varchar(32) DEFAULT NULL COMMENT '考级时间',\n" +
//                "  `meeting_time` varchar(32) DEFAULT NULL COMMENT '上会时间',\n" +
//                "  `is_party_member` int(4) DEFAULT NULL COMMENT '是否党员',\n" +
//                "  `is_asset` int(4) DEFAULT NULL COMMENT '是否领用资产',\n" +
//                "  PRIMARY KEY (`id`) USING BTREE,\n" +
//                "  KEY `index_id` (`id`) USING BTREE\n" +
//                ") ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='进驻人员信息表'");
//        System.out.println(stringStringHashMap.toString());
//        System.out.println("================================================");
//    }

    @Test
    void generateDDL() throws IOException {

        String existingCreateSQL = "CREATE TABLE `biz_entry_user` (\n" +
                "  `id` varchar(50) NOT NULL,\n" +
                "  `name` varchar(32) DEFAULT NULL COMMENT '姓名',\n" +
                "  `sex` int(4) DEFAULT NULL COMMENT '性别，0：女，1：男',\n" +
                "  `entry_time` varchar(32) DEFAULT NULL COMMENT '进驻时间',\n" +
                "  `apply_time` varchar(32) DEFAULT NULL COMMENT '申请时间',\n" +
                "  `grading_time` varchar(32) DEFAULT NULL COMMENT '考级时间',\n" +
                "  `meeting_time` varchar(32) DEFAULT NULL COMMENT '上会时间',\n" +
                "  `phone` varchar(500) DEFAULT NULL COMMENT '手机号',\n" +
                "  `station` varchar(64) DEFAULT NULL COMMENT '工位',\n" +
                "  `nature` varchar(255) DEFAULT NULL COMMENT '人员性质',\n" +
                "  `work_no` varchar(64) DEFAULT NULL COMMENT '工号',\n" +
                "  `state` int(4) DEFAULT NULL COMMENT '人员状态，1：办理中，2：已完成，3：已调离,4：待确认',\n" +
                "  `dept_name` varchar(255) DEFAULT NULL COMMENT '所属部门名称',\n" +
                "  `show_dept_name` varchar(255) DEFAULT NULL COMMENT '界面展示窗口部门',\n" +
                "  `dept_id` varchar(32) DEFAULT NULL COMMENT '所属部门id',\n" +
                "  `create_time` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT '创建时间',\n" +
                "  `create_person` varchar(32) DEFAULT NULL COMMENT '创建人',\n" +
                "  `update_time` datetime DEFAULT NULL COMMENT '更新时间',\n" +
                "  `update_person` varchar(32) DEFAULT NULL COMMENT '更新人',\n" +
                "  `is_party_member` int(4) DEFAULT NULL COMMENT '是否党员',\n" +
                "  `is_asset` int(4) DEFAULT NULL COMMENT '是否领用资产',\n" +
                "  PRIMARY KEY (`id`) USING BTREE,\n" +
                "  KEY `index_id` (`id`) USING BTREE\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='进驻人员信息表'\n";
        String localSQL = "CREATE TABLE `biz_entry_user` (\n" +
                "  `id` varchar(50) NOT NULL,\n" +
                "  `name` varchar(32) DEFAULT NULL COMMENT '姓名',\n" +
                "  `sex` int(4) DEFAULT NULL COMMENT '性别，0：女，1：男',\n" +
                "  `entry_time` varchar(32) DEFAULT NULL COMMENT '进驻时间',\n" +
                "  `apply_time` varchar(32) DEFAULT NULL COMMENT '申请时间',\n" +
                "  `grading_time` varchar(32) DEFAULT NULL COMMENT '考级时间',\n" +
                "  `meeting_time` varchar(32) DEFAULT NULL COMMENT '上会时间',\n" +
                "  `zs` varchar(500) DEFAULT NULL COMMENT '手机号',\n" +
                "  `station` varchar(64) DEFAULT NULL COMMENT '工位',\n" +
                "  `nature` varchar(255) DEFAULT NULL COMMENT '人员性质',\n" +
                "  `work_no` varchar(64) DEFAULT NULL COMMENT '工号',\n" +
                "  `state` int(4) DEFAULT NULL COMMENT '人员状态，1：办理中，2：已完成，3：已调离,4：待确认',\n" +
                "  `dept_name` varchar(255) DEFAULT NULL COMMENT '所属部门名称',\n" +
                "  `dept_id` varchar(32) DEFAULT NULL COMMENT '所属部门id',\n" +
                "  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',\n" +
                "  `create_person` varchar(32) DEFAULT NULL COMMENT '创建人',\n" +
                "  `is_party_member` int(4) DEFAULT NULL COMMENT '是否是党员',\n" +
                "  `is_asset` int(4) DEFAULT NULL COMMENT '是否是领用资产',\n" +
                "  `update_time` datetime DEFAULT NULL COMMENT '更新时间',\n" +
                "  `update_person` varchar(32) DEFAULT NULL COMMENT '更新人',\n" +
                "  `show_dept_name` varchar(100) DEFAULT NULL COMMENT '窗口',\n" +
                "  `birth_time` datetime DEFAULT NULL COMMENT '出生日期',\n" +
                "  `education` varchar(100) DEFAULT NULL COMMENT '学历',\n" +
                "  `home_address` varchar(500) DEFAULT NULL COMMENT '家庭住址',\n" +
                "  `id_card` varchar(500) DEFAULT NULL COMMENT '身份证',\n" +
                "  `last_year_assess` varchar(255) DEFAULT NULL COMMENT '上年度考核情况',\n" +
                "  `mentor` varchar(100) DEFAULT NULL COMMENT '带班导师',\n" +
                "  `bank_card_number` varchar(500) DEFAULT NULL COMMENT '农商银行卡号',\n" +
                "  `entry_type` int(4) DEFAULT NULL COMMENT '派驻形式 1 新增  0替换',\n" +
                "  `replace_person` varchar(100) DEFAULT NULL COMMENT '替换人',\n" +
                "  `post` varchar(100) DEFAULT NULL COMMENT '岗位',\n" +
                "  `post_type` int(4) DEFAULT NULL COMMENT ' 岗位 1 前台  0 后台',\n" +
                "  `entry_reason` text COMMENT '派驻原因',\n" +
                "  `resume` text COMMENT '简历',\n" +
                "  `examination_time` datetime DEFAULT NULL COMMENT '考录时间',\n" +
                "  `politics_status` varchar(100) DEFAULT NULL COMMENT '政治面貌',\n" +
                "  `window_person_total` int(100) DEFAULT NULL COMMENT '窗口总人数',\n" +
                "  `window_person_in_num` int(11) DEFAULT NULL COMMENT '在编人员数',\n" +
                "  `window_person_out_num` int(11) DEFAULT NULL COMMENT '编外人员数',\n" +
                "  PRIMARY KEY (`id`) USING BTREE,\n" +
                "  KEY `index_id` (`id`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='进驻人员信息表'";
        String s = userServiceImpl.generateDDL(existingCreateSQL, localSQL);
        System.out.println("=========================");
        System.out.println(s);
        System.out.println("=========================");
    }
}