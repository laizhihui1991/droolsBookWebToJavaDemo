package org.example;

import com.businesscentarltojava.drools_wb_to_java.User;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

public class Main {

    public static void main(String[] args) {
        KieServices ks = KieServices.Factory.get();
        ReleaseId releaseId =
                ks.newReleaseId(
                        "com.businesscentarltojava",
                        "drools_wb_to_java",
                        "1.0.0-SNAPSHOT");
        KieContainer container =
                ks.newKieContainer(releaseId);
        KieSession session =
                container.newKieSession();
        User user = new User();
        user.setId("10000");
        user.setName("来家二少");
        user.setAge(30);
        session.insert(user);
        int count = session.fireAllRules();
        System.out.println("执行规则：" + count);
        session.dispose();
    }
}