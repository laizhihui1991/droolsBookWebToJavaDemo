package com.scanner;

import com.businesscentarltojava.drools_wb_to_java.User;
import org.kie.api.KieServices;
import org.kie.api.builder.KieScanner;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

public class Main {

    public static void main(String[] args) throws Exception {

        KieServices ks = KieServices.Factory.get();

        ReleaseId releaseId = ks.newReleaseId(
                "com.businesscentarltojava",
                "drools_wb_to_java",
                "1.0.0-SNAPSHOT");

        // 创建容器
        KieContainer kContainer = ks.newKieContainer(releaseId);
        // 创建扫描器
        KieScanner scanner = ks.newKieScanner(kContainer);
        // 每5秒扫描一次
        scanner.start(5000L);
        System.out.println("开始监听 Business Central......");
        while (true) {
            KieSession kSession = kContainer.newKieSession();
            User user = new User();
            user.setId("10000");
            user.setName("来家二少");
            // 一直保持30
            user.setAge(30);
            kSession.insert(user);
            int count = kSession.fireAllRules();
            System.out.println("执行规则数量 : " + count);
            kSession.dispose();
            if (user.getAge() != 30) {
                System.out.println();
                System.out.println("检测到规则已经更新！");
                System.out.println("新的规则不再匹配 age=30");
                System.out.println("退出程序");
                break;
            }
            Thread.sleep(5000);
        }
        scanner.stop();
    }
}