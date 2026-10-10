package com.example.loan;

import com.example.loan.model.LoanApplication;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

public class LoanClient {

    public static void main(String[] args) {

        KieServices kieServices = KieServices.Factory.get();
        // 使用 KJAR 的 Maven 坐标创建 ReleaseId
        ReleaseId releaseId =
                kieServices.newReleaseId(
                        "com.example.drools",
                        "loan-credit-score-kjar",
                        "1.0.0"
                );
        // 根据 ReleaseId 加载 KJAR
        KieContainer kieContainer = kieServices.newKieContainer(releaseId);
        // 获取 KJAR 中 kmodule.xml 定义的 KieSession
        KieSession kieSession = kieContainer.newKieSession("loanKSession");
        try {

            LoanApplication loan = new LoanApplication(
                    "来家二少",
                    35,
                    8,
                    500000,
                    true,
                    0.30
            );
            // 插入业务 Fact
            kieSession.insert(loan);
            // 执行规则
            int count = kieSession.fireAllRules();
            System.out.println("执行规则：" + count);
            System.out.println("申请人：" + loan.getApplicantName());
            System.out.println("可信积分：" + loan.getCreditScore());
            System.out.println("可信等级：" + loan.getCreditLevel());
        } finally {
            kieSession.dispose();
        }
    }
}