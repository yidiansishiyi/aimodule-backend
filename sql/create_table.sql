# 建表脚本
# @author sanqi

-- 创建库
create database if not exists yidiansishiyi;

-- 切换库
use yidiansishiyi;

-- 用户表
create table  if not exists user
(
    id           bigint auto_increment comment 'id'
        primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    userMailbox  varchar(40)                            null comment '邮箱',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除'
)
    comment '用户' collate = utf8mb4_unicode_ci;

create index idx_userAccount
    on yidiansishiyi.user (userAccount);

-- 图表表
create table if not exists chart
(
    id          bigint auto_increment comment 'id'
        primary key,
    goal        text                                   null comment '分析目标',
    name        varchar(128)                           null comment '图标名称',
    chartData   text                                   null comment '图表数据',
    chartType   varchar(128)                           null comment '图表类型',
    genChart    text                                   null comment '生成的图表数据',
    genResult   text                                   null comment '生成的分析结论',
    meterHeader text                                   null comment '表头',
    status      varchar(128) default 'wait'            not null comment 'wait,running,success,failure',
    execMessage text                                   null comment '执行信息',
    userId      bigint                                 null comment '创建用户 id',
    createTime  datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint      default 0                 not null comment '是否删除'
)
    comment '图表信息表' collate = utf8mb4_unicode_ci;

-- 敏感词
create table if not exists wmSensitive
(
    id          bigint auto_increment comment 'id'
        primary key,
    sensitives  varchar(128)                       null comment '敏感词',
    createdTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '创建时间'
)
    comment '铭感词表';

create index wmSensitive_id_index
    on yidiansishiyi.wmSensitive (id);

create index wmSensitive_sensitives_index
    on yidiansishiyi.wmSensitive (sensitives);



