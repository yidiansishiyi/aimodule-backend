package com.yidiansishiyi.aimodule.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yidiansishiyi.aimodule.common.DeleteRequest;
import com.yidiansishiyi.aimodule.common.ErrorCode;
import com.yidiansishiyi.aimodule.constant.CommonConstant;
import com.yidiansishiyi.aimodule.exception.BusinessException;
import com.yidiansishiyi.aimodule.mapper.ChartMapper;
import com.yidiansishiyi.aimodule.mapper.UserMapper;
import com.yidiansishiyi.aimodule.model.dto.user.UserQueryRequest;
import com.yidiansishiyi.aimodule.model.entity.Chart;
import com.yidiansishiyi.aimodule.model.entity.User;
import com.yidiansishiyi.aimodule.model.enums.UserRoleEnum;
import com.yidiansishiyi.aimodule.model.vo.LoginUserVO;
import com.yidiansishiyi.aimodule.model.vo.UserVO;
import com.yidiansishiyi.aimodule.service.UserService;
import com.yidiansishiyi.aimodule.utils.SqlUtils;
import jodd.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.yidiansishiyi.aimodule.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现
 *
 * @author sanqi
 *   
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "yidiansishiyi";

    @Resource
    private UserMapper userMapper;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);

            Long userId = user.getId();

            // 3. 分配 accessKey, secretKey
            String accessKey = DigestUtil.md5Hex(SALT  + userId + RandomUtil.randomNumbers(5));
            String secretKey = DigestUtil.md5Hex(SALT + userId + RandomUtil.randomNumbers(8));
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);

            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public Boolean resetAccessKey(DeleteRequest deleteRequest, HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        Long loginUserId = loginUser.getId();
        String userRole = loginUser.getUserRole();
        UserRoleEnum enumByValue = UserRoleEnum.getEnumByValue(userRole);

        if (loginUserId.equals(deleteRequest.getId()) && !UserRoleEnum.ADMIN.equals(enumByValue)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        QueryWrapper<User> userUpdateWrapper = new QueryWrapper<>();
        userUpdateWrapper.eq("id", loginUserId);

        User updateUser = getOne(userUpdateWrapper);
        if (updateUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        Long userId = updateUser.getId();
        String accessKey = DigestUtil.md5Hex(SALT  + userId + RandomUtil.randomNumbers(5));
        String secretKey = DigestUtil.md5Hex(SALT + userId + RandomUtil.randomNumbers(8));
        updateUser.setAccessKey(accessKey);
        updateUser.setSecretKey(secretKey);

        boolean flage = updateById(updateUser);
        return flage;
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        return this.getById(userId);
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String unionId = userQueryRequest.getUnionId();
        String mpOpenId = userQueryRequest.getMpOpenId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(unionId), "unionId", unionId);
        queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mpOpenId", mpOpenId);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Resource
    private ChartMapper chartMapper;

    void setChartMapper() {

        LambdaQueryWrapper<Chart> chartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chartLambdaQueryWrapper.eq(Chart::getCreateTime,"createTime");
        User testUser = User.builder()
                .userName("测试回滚")
                .userAvatar("zzzz")
                .userPassword("password")
                .userAccount("test")
                .build();
        int insert1 = userMapper.insert(testUser);
        Chart testChart = Chart.builder()
                .userId(00000000L)
                .name("测试图标实现")
                .build();
        int insert = chartMapper.insert(testChart);
    }

    @Override
    public boolean testTransactional() {
        setChartMapper();

        try {
            ArrayList<User> charts = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                charts.add(User.builder()
                        .userName("测试回滚" + i)
                        .userAvatar("zzzz")
                        .userPassword("password")
                        .userAccount("test")
                        .build());
            }
            this.saveBatch(charts,10);


        } catch (Exception e) {
            log.error("回滚测试" + e.getMessage());
//            throw new RuntimeException();
            throw new BusinessException(200,"除零错误");
//            return false;
        }

        return true;
    }

    @Override
    public String generateDDL(String existingCreateSQL, String localSQL) throws IOException {

        HashMap<String, String> extractColumns = extractColumns(existingCreateSQL);
        // 根据表明差数据库获取创表语句
        String tableName = extractColumns.get("TableName");
//        String selectCreateSQL = screenServicePortraitDao.getCreateSQL(tableName);
        String addDDL = generateDDLze(extractColumns, localSQL);
        // 新增文档写入数据, 写入查询 sql 传入 sql 和 ddl sql
        // 新增接口
        String DDL = addDDL + "\n" + "\n" + existingCreateSQL + "\n" + "\n" + localSQL;
        File file = new File("D:\\files\\" + tableName + ".txt");
        FileUtil.writeString(file, DDL);
        String s1 = FileUtil.readString(new File("D:\\files\\creatSql.txt"));

        return file.getPath();
    }

    public String generateDDLze(Map columns,String sql){
        StringBuilder create = new StringBuilder();
        Object tableName = columns.get("TableName");
        // 找到创表语句主体部分
        String tableBody = extractBetweenSymbols(sql, "(", ") ENGINE");
        String[] lines = tableBody.split(",\n");
        for (String line : lines) {
            String columnName = extractBetweenSymbols(line, "`", "`");
            boolean b = columns.containsKey(columnName);
            if (columns.containsKey(columnName)) {
                String o = (String)columns.get(columnName);
                if (o.equals(line)) {
                    continue;
                }
                create.append("alter table ").append(tableName).append(" change ").append(columnName).append(" ").append(line);
//                create.deleteCharAt(create.length() - 1);
                create.append(";\n");
                continue;
            }
            create.append("alter table ").append(tableName).append(" add ").append(line);
//            create.deleteCharAt(create.length() - 1);
            create.append(";\n");
        }
        return create.toString();
    }

    public HashMap<String, String> extractColumns(String existingCreateSQL) {
        HashMap<String, String> tableInformation = new HashMap<>();

        // 找到表名
        String tableNameRegex = "CREATE TABLE `(.*?)`";
        Pattern tableNamePattern = Pattern.compile(tableNameRegex);
        Matcher tableNameMatcher = tableNamePattern.matcher(existingCreateSQL);
        if (tableNameMatcher.find()) {
            String tableName = tableNameMatcher.group(1);
            tableInformation.put("TableName", tableName);
        }

        // 找到创表语句主体部分
        String tableBody = extractBetweenSymbols(existingCreateSQL, "(", ") ENGINE");
        String[] lines = tableBody.split(",\n");
        for (String line : lines) {
            String columnName = extractBetweenSymbols(line, "`", "`");
            if (tableInformation.containsKey(columnName)) {
                continue;
            }
            tableInformation.put(columnName, line);
        }

        return tableInformation;
    }

    public String extractBetweenSymbols(String input, String startSymbol, String endSymbol) {
        int startIndex = input.indexOf(startSymbol);
        int endIndex = input.indexOf(endSymbol, startIndex + 1);

        if (startIndex != -1 && endIndex != -1) {
            return input.substring(startIndex + 1, endIndex);
        } else {
            return null;
        }
    }


}
