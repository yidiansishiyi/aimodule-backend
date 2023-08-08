package com.yidiansishiyi.aimodule.manager;

import com.yidiansishiyi.zelinaiclientsdk.model.ZelinAIRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class AiManagerTest {

    @Resource
    private AiManager aiManager;

    @Test
    void doChat() {
        String answer = aiManager.doChat(1659171950288818178L, "分析需求：\n" +
                "分析网站用户的增长情况\n" +
                "原始数据：\n" +
                "日期,用户数\n" +
                "1号,10\n" +
                "2号,20\n" +
                "3号,30\n");
        System.out.println(answer);
    }
    @Test
    void doChatZelin() {
        // 成功测试,但是对面好像对输入有限制, 输入字符串好像有时候不合法, https://kaokaofs.feishu.cn/docx/Mq8Hdh1YqoZorEx2cgqcnGOPnof 暂时引入测试,
        // todo 现存问题,不一致问题,ai引入不同sdk 命名冲突,提取公因式自己从新编写一套公共的,两面不同的做适配(可能1) 适配器模式调用处解决 2
        // todo 不同 ai 的调用机制 专门弄一个配置中心(不知道怎么说,专门弄一个东西维护)
        ZelinAIRequest zelinAIRequest = new ZelinAIRequest();
        zelinAIRequest.setApp_id("nBoA5U7hJtQzqNMwLfLJTi");
        zelinAIRequest.setRequest_id("15464548417");
        zelinAIRequest.setUid("841894874897");
        zelinAIRequest.setContent("分析需求：分析增长情况，请使用折线图 原始数据：日期,用户数1号,10 2号,20 3号,30 4号,90 5号,0 6号,10 号,20");
        String answer = aiManager.doChat(zelinAIRequest);
        System.out.println(answer);
    }
}