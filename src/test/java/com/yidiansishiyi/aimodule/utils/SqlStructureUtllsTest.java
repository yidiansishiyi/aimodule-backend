package com.yidiansishiyi.aimodule.utils;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yidiansishiyi.aimodule.mapper.ScreenApprovalPerformanceDao;
import com.yidiansishiyi.aimodule.mapper.ScreenOverviewDao;
import com.yidiansishiyi.aimodule.mapper.UserMapper;
import com.yidiansishiyi.aimodule.model.entity.User;
import com.yidiansishiyi.aimodule.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.*;
import java.util.concurrent.CompletableFuture;


@SpringBootTest
class SqlStructureUtllsTest {

    @Resource
    private UserMapper userMapper;

    @Resource
    ScreenOverviewDao screenOverviewDao;

//    @Test
//    public void getPerformance() {
//        Map<String, Object> centralMatters = screenOverviewDao.getPerformance();
//        if (centralMatters == null) {
//            throw new IllegalStateException("Invalid");
//        }
//        Map<String, Object> modifiedMatters = new LinkedHashMap<>();
//
//        centralMatters.keySet().forEach(res -> {
//            String modifiedKey = res.replaceAll("_", ".");
//            Object value = centralMatters.get(res);
//            modifiedMatters.put(modifiedKey, value);
//        });
//        System.out.println(modifiedMatters.toString());
//    }
//
//    @Test
//    public void testGetDDL() throws Exception {
//        ArrayList<Map<String,String>> maps = new ArrayList<>();
//        List<String> sqlTable = userMapper.getSqlTable("yidiansishiyi");
//
//        Iterator<String> iterator = sqlTable.iterator();
//        while (iterator.hasNext()) {
//            String res = iterator.next();
//            if (res.contains("copy")) {
//                iterator.remove();
//            }
//        }
//
//        System.out.println(sqlTable.toString());
//        for (int i = 0; i < sqlTable.size(); i++) {
//            String s = sqlTable.get(i);
//            Map<String, String> sqlStructure = userMapper.getSqlStructure(s);
//            maps.add(sqlStructure);
//        }
//        StringBuilder content = new StringBuilder();
//        for (Map<String, String> map : maps) {
//            content.append(map.toString()).append("\n");
//        }
//
//        FileUtil.writeString(new File("D:\\files\\creatSql.txt"), content.toString());
//    }
//
//    @Test
//    public void testFH() throws IOException {
////        List<Map<String, Object>> maps = userMapper.userListzer();
//        Map<String, Object> maps = userMapper.userListzer();
//        List<Object> datas = new ArrayList<>();
//        List<Object> headers = new ArrayList<>();
//
//        for (Map.Entry<String, Object> stringObjectEntry : maps.entrySet()) {
//            String result = stringObjectEntry.getKey();
//            String nameKey = result.replaceAll("\\_", ".");
//            headers.add(nameKey);
//            datas.add(stringObjectEntry.getValue());
//        }
//
//        HashMap<String, Object> rest = new HashMap<>();
//        rest.put("headers", headers);
//        rest.put("datas", datas);
//        System.out.println(rest.toString());
//    }
//
////    @Test
////    public void testCent(){
////
////        Map<String, Object> centralMatters = userMapper.getCentralMatters();
////        List<Object> datas = new ArrayList<>();
////        List<Object> headers = new ArrayList<>();
////
////        if (centralMatters.isEmpty()) {
////            throw new IllegalStateException();
////        }
////
////        ArrayList<Map<String, Object>> res = new ArrayList<>();
////
////        for (Map.Entry<String, Object> stringObjectEntry : centralMatters.entrySet()) {
////            String result = stringObjectEntry.getKey();
////            HashMap<String, Object> middle = new HashMap<>();
////            middle.put("name",stringObjectEntry.getKey());
////            middle.put("value",stringObjectEntry.getValue());
////            res.add(middle);
////        }
////        for (int i = 0; i < res.size(); i++) {
////            System.out.println(res.get(i).toString());
////        }
////    }
//
//    @Test
//    public void testGetServicePackageVillageTown(){
//        List<Map<String, Object>> listVillageTown = screenOverviewDao.getServicePackageVillageTown();
//        ArrayList<String> heads = new ArrayList<>();
//        ArrayList<Object> datas = new ArrayList<>();
//        for (Map<String, Object> villageTown : listVillageTown) {
//            heads.add((String) villageTown.get("村镇"));
//            datas.add(villageTown.get("数量"));
//        }
//        HashMap<String, Object> res = new HashMap<>();
//        res.put("heads",heads);
//        res.put("datas",datas);
//        System.out.println(res.toString());
//    }
//
//    @Test
//    public void testgetWorkLineChart(){
//        List<Map<String, Object>> listVillageTown = userMapper.getWorkLineChart();
//        if (listVillageTown == null) {
//            throw new IllegalStateException("数据错误请练习管理员");
//        }
//
//        ArrayList<String> heads = new ArrayList<>();
//        ArrayList<Object> datas = new ArrayList<>();
//        ArrayList<Object> data2 = new ArrayList<>();
//        Long max = new Long(0);
//
//        for (Map<String, Object> villageTown : listVillageTown) {
//            heads.add((String) villageTown.get("月份"));
//            Long oneThingNumber = (Long)villageTown.get("一件事");
//            Long handlingItemsNumber = (Long)villageTown.get("办件总量");
//            if (oneThingNumber != null && handlingItemsNumber != null) {
//                max = oneThingNumber > handlingItemsNumber ? oneThingNumber:handlingItemsNumber;
//            }
//            datas.add(oneThingNumber);
//            data2.add(handlingItemsNumber);
//        }
//
//        max = (long)Math.floor(max * 1.2);
//        HashMap<String, Object> res = new HashMap<>();
//        res.put("heads",heads);
//        res.put("datas",datas);
//        res.put("data2",data2);
//        res.put("max",max);
//        System.out.println(res.toString());
//    }
//
//    @Test
//    public void testgetDepartment(){
//        List<Map<String, Object>> listVillageTown = userMapper.getDepartment();
//        if (listVillageTown.size() == 0){
//            throw new IllegalStateException("数据错误请练习管理员");
//        }
//        Map<Object, List<Map<String, Object>>> groupedData = listVillageTown.stream()
//                .collect(Collectors.groupingBy(map -> map.get("事项类型")));
//
//        System.out.println(groupedData.toString());
//        HashMap<String, Object> res = new HashMap<>();
//    }
//
//    @Test
//    public void getVerify(){
//        Map<String, Object> listVillageTown = userMapper.getVerify();
//        if (listVillageTown == null){
//            throw new IllegalStateException("数据错误请练习管理员");
//        }
//
//        HashMap<String, Object> res = new HashMap<>();
//        System.out.println(listVillageTown.toString());
//    }
//
//    @Test
//    public void getConvenienceNetwork(){
//        Map<String, Object> convenienceNetwork = userMapper.getConvenienceNetwork();
//        if (convenienceNetwork == null){
//            throw new IllegalStateException("数据错误请练习管理员");
//        }
//
//        HashMap<String, Object> res = new HashMap<>();
//        System.out.println(convenienceNetwork.toString());
//    }
//
//    @Test
//    public void getSmartCommunity(){
//        List<Map<String, Object>> listSmartCommunity = userMapper.getSmartCommunity();
//
//        if (listSmartCommunity.size() == 0){
//            throw new IllegalStateException("数据错误请练习管理员");
//        }
//        ArrayList<String> heads = new ArrayList<>();
//        ArrayList<Object> datas = new ArrayList<>();
//        ArrayList<Object> data2 = new ArrayList<>();
//        Long maxPrincipalAxis = new Long(0);
//        Long maxSecondaryAxis = new Long(0);
//
//        for (Map<String, Object> smartCommunity : listSmartCommunity) {
//            heads.add((String) smartCommunity.get("月份"));
//            Long onlineMatter = (Long)smartCommunity.get("上线事项(主轴)");
//            Long departmentNumber = (Long)smartCommunity.get("办件量(副轴)");
//            if (onlineMatter != null) {
//                maxPrincipalAxis = maxPrincipalAxis > onlineMatter ? maxPrincipalAxis : onlineMatter;
//            }
//
//            if (onlineMatter != null) {
//                maxSecondaryAxis = departmentNumber > maxSecondaryAxis ? departmentNumber : maxSecondaryAxis;
//            }
//
//            datas.add(onlineMatter);
//            data2.add(departmentNumber);
//        }
//
//        maxPrincipalAxis = (long)Math.floor(maxPrincipalAxis * 1.2);
//        maxSecondaryAxis = (long)Math.floor(maxSecondaryAxis * 1.2);
//        HashMap<String, Object> res = new HashMap<>();
//        res.put("heads",heads);
//        res.put("datas",datas);
//        res.put("data2",data2);
//        res.put("maxPrincipalAxis",maxPrincipalAxis);
//        res.put("maxSecondaryAxis",maxSecondaryAxis);
//        System.out.println(res.toString());
//    }

//    @Test
//    public void test11() {
//        List<Map<String, Object>> listConvenienceNetwork = screenOverviewDao.getMassApplicationPortrait();
//        // todo 查询数据时异常处理
//
//        ArrayList<String> xAxis = new ArrayList<>();
//        ArrayList<Object> listOne = new ArrayList<>();
//        ArrayList<Object> listTwo = new ArrayList<>();
//        ArrayList<Object> listThree = new ArrayList<>();
//        Long maxYAxis = new Long(0);
//
//        for (Map<String, Object> convenienceNetwork : listConvenienceNetwork) {
//            xAxis.add((String) convenienceNetwork.get("月份"));
//            Long enterpriseQuery = (Long)convenienceNetwork.get("企证查");
//            Long onlineLobby = (Long)convenienceNetwork.get("网上大厅");
//            Long cloudSteward = (Long)convenienceNetwork.get("云管家");
//            if (enterpriseQuery != null || onlineLobby != null || cloudSteward != null) {
//                // 找到最大值
//                maxYAxis = enterpriseQuery > maxYAxis ? enterpriseQuery : maxYAxis;
//                maxYAxis = onlineLobby > maxYAxis ? onlineLobby : maxYAxis;
//                maxYAxis = cloudSteward > maxYAxis ? cloudSteward : maxYAxis;
//            }
//            listOne.add(enterpriseQuery);
//            listTwo.add(onlineLobby);
//            listThree.add(cloudSteward);
//        }
//        System.out.println(maxYAxis);
//
//        maxYAxis = (long)Math.floor(maxYAxis * 1.2);
//        HashMap<String, Object> res = new HashMap<>();
//        // todo 统一数据
//        res.put("xAxis",xAxis);
//        res.put("listOne",listOne);
//        res.put("listTwo",listTwo);
//        res.put("maxYAxis",maxYAxis);
//
//        System.out.println(res.toString());
//
//    }
//
//    @Test
//    public void test12() {
//
//        Long governmentCloud = screenOverviewDao.getGovernmentCloud();
//        System.out.println(governmentCloud);
//
//        List<Map<String, Object>> listProblemType = screenOverviewDao.getProblemType();
//
//        Map<Long, Integer> frequencyMap = new HashMap<>();
//        for (Map<String, Object> stringObjectMap : listProblemType) {
//            Long problemType = Long.parseLong((String) stringObjectMap.get("problemType"));
//            frequencyMap.put(problemType, frequencyMap.getOrDefault(problemType, 0) + 1);
//        }
//        System.out.println(frequencyMap.toString());
//        HashMap<String, Object> res = new HashMap<>();
//        res.put("服务人次", governmentCloud);
//        res.put("咨询", frequencyMap.get(1L));
//        res.put("建议", frequencyMap.get(2L));
//        res.put("投诉", frequencyMap.get(4L));
//
//
//        System.out.println(res.toString());
//    }
//
//    @Test
//    public void test13() {
//        List<Map<String, Object>> proposalList = screenOverviewDao.getProposalList();
//        System.out.println(proposalList.toString());
//    }
//
//    @Test
//    public void test14() {
//        HashMap<Object, Object> dictMap = new HashMap<>();
//        List<Map<String, Object>> problemTypeTODict = screenOverviewDao.getProblemTypeTODict();
//        for (Map<String, Object> stringObjectMap : problemTypeTODict) {
//            dictMap.put(stringObjectMap.get("code"), stringObjectMap.get("name"));
//        }
//        Object o = dictMap.get(1);
//        System.out.println(o);
//        System.out.println(dictMap.toString());
//    }


//    @Test
//    public void test15() {
//        IPage<Map<String, Object>> mapIPage = new Page<>();
//        IPage<Map<String, Object>> proposalList = screenOverviewDao.getProposalList();
//
//        List<Map<String, Object>> problemTypeTODict = screenOverviewDao.getProblemTypeTODict();
//        HashMap<Integer, Object> dictMap = new HashMap<>();
//        for (Map<String, Object> stringObjectMap : problemTypeTODict) {
//            dictMap.put((Integer)stringObjectMap.get("code"), stringObjectMap.get("name"));
//        }
//
//        List<Map<String, Object>> records = proposalList.getRecords();
//        for (Map<String, Object> record : records) {
//            record.put("problemType",dictMap.get(Integer.parseInt((String)record.get("problemType"))));
//        }
//
//        System.out.println(records.toString());
//    }


//    @Test
//    public void tests(){
//        List<Map<String, Object>> listProblemTypeAndCount = screenOverviewDao.getProblemTypeAndCount();
//        HashMap<String, Object> frequencyMap = new HashMap<>();
//        for (Map<String, Object> problemTypeAndCount : listProblemTypeAndCount) {
//            frequencyMap.put((String) problemTypeAndCount.get("problemType"),problemTypeAndCount.get("count"));
//        }
//    }

    @Resource
    ScreenApprovalPerformanceDao screenApprovalPerformanceDao;

    @Test
    public void test12(){
        List<Map<String, Object>> listProblemTypeAndCount = screenApprovalPerformanceDao.getAdventNumber();
        listProblemTypeAndCount.forEach(System.out::println);
//        HashMap<String, Object> frequencyMap = new HashMap<>();
//        for (Map<String, Object> problemTypeAndCount : listProblemTypeAndCount) {
//            frequencyMap.put((String) problemTypeAndCount.get("problemType"),problemTypeAndCount.get("count"));
//        }
    }

    @Test
    public void test1(){
        List<Map<String, Object>> adventList = screenApprovalPerformanceDao.getAdventList();
        adventList.forEach(System.out::println);
//        HashMap<String, Object> frequencyMap = new HashMap<>();
//        for (Map<String, Object> problemTypeAndCount : listProblemTypeAndCount) {
//            frequencyMap.put((String) problemTypeAndCount.get("problemType"),problemTypeAndCount.get("count"));
//        }
    }

    @Test
    public void test148(){
        Page<Map<String, Object>> mapPage = new Page<>();
        IPage<Map<String, Object>> handleRatio = screenApprovalPerformanceDao.getListWorkEfficiency(mapPage);
        handleRatio.getRecords().forEach(System.out::println);
//        HashMap<String, Object> frequencyMap = new HashMap<>();
//        for (Map<String, Object> problemTypeAndCount : listProblemTypeAndCount) {
//            frequencyMap.put((String) problemTypeAndCount.get("problemType"),problemTypeAndCount.get("count"));
//        }
    }


    @Resource
    private RedissonClient redissonClient;

    @Test
    public void test19(){
        Date initialTime = new Date();
        RMap<Object, Date> rMap = redissonClient.getMap("aimodule:job:operationTime");
        rMap.put("startTime",initialTime);
        rMap.put("endTime",initialTime);
    }


//    @Test
//    public void test13() {
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("(")
//        for (int i = 0; i < 100; i++) {
//            StringUtils.
//            stringBuilder.append("'项目'")
//        }
//
//
//        screenApprovalPerformanceDao.insert();
//
//    }

    @Test
    public void test13() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            if (i == 0) {
                stringBuilder.append("('项目', '0', '30')");
            }
            stringBuilder.append(",('项目', '0', '30')"); // Adjust with your actual values
        }

        screenApprovalPerformanceDao.insert(stringBuilder.toString());
    }


    @Resource
    private static UserService userService;

    private static final String SALT = "yidiansishiyi";

    @Test
    public void texe1245() {
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.select("id");

        List<User> list = userService.list(userQueryWrapper);

        List<User> userListUsers = new ArrayList<>();
        list.forEach(user -> {
            User updateUser = new User();
            Long userId = user.getId();
            String accessKey = DigestUtil.md5Hex(SALT  + userId + RandomUtil.randomNumbers(5));
            String secretKey = DigestUtil.md5Hex(SALT + userId + RandomUtil.randomNumbers(8));

            updateUser.setAccessKey(accessKey);

            updateUser.setSecretKey(secretKey);
            updateUser.setId(userId);
            userListUsers.add(updateUser);
        });

        userService.updateBatchById(userListUsers);

    }


}