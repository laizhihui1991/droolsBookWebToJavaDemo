package com.kieserver;

import com.alibaba.fastjson.JSONObject;
import com.testkieserver.kieservertest.Order;
import com.testkieserver.kieservertest.User;
import org.kie.api.KieServices;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.RuleServicesClient;

import java.util.ArrayList;
import java.util.List;

public class Main {
    // 本机映射端口，直接使用 localhost:8180
    private static final String SERVER_URL = "http://localhost:8180/kie-server/services/rest/server";
    private static final String USER = "kieserver";
    private static final String PWD = "kieserver1!";
    // 容器完整ID，严格和接口返回一致
    private static final String CONTAINER_ID = "com.testkieserver:kieServerTest:1.0.0-SNAPSHOT";
    // 容器配置好的会话名称
    private static final String SESSION_NAME = "kieServerSession";

    public static void main(String[] args) {
        KieServicesClient kieClient = null;
        try {
            KieServicesConfiguration config = KieServicesFactory
                    .newRestConfiguration(SERVER_URL, USER, PWD, 10000L);
            config.setMarshallingFormat(MarshallingFormat.JSON);
            kieClient = KieServicesFactory.newKieServicesClient(config);
            RuleServicesClient ruleClient = kieClient.getServicesClient(RuleServicesClient.class);

            // 2. 构造业务实体
            User user = new User();
            user.setId("10000");
            user.setName("来家二少");
            user.setAge(30);

            Order order = new Order();
            order.setId("10000");
            order.setUserId("10000");

            // 3. 组装批量命令
            KieCommands commandFactory = KieServices.Factory.get().getCommands();
            List<Command<?>> batchCommands = new ArrayList<>();
            // 插入事实，并绑定别名User、Order，后续用来取回修改后的对象
            batchCommands.add(commandFactory.newInsert(user, "User"));
            batchCommands.add(commandFactory.newInsert(order, "Order"));
            // 执行所有规则
            batchCommands.add(commandFactory.newFireAllRules());

            // 4. 远程调用KieServer执行规则
            ServiceResponse<ExecutionResults> resp = ruleClient.executeCommandsWithResults(
                    CONTAINER_ID,
                    commandFactory.newBatchExecution(batchCommands, SESSION_NAME)
            );

            // 5. 解析返回结果
            if (resp.getType() == ServiceResponse.ResponseType.SUCCESS) {
                ExecutionResults result = resp.getResult();
                Object orderResult = result.getValue("Order");
                if (orderResult instanceof Order) {
                    Order resOrder = (Order) orderResult;
                    System.out.println("规则执行完成，订单结果：");
                    System.out.println(JSONObject.toJSONString(resOrder, true));
                } else {
                    System.out.println("调用成功，但规则未修改Order对象，请检查DRL规则条件");
                }
            } else {
                System.err.println("调用异常：" + resp.getMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭连接
            if (kieClient != null) {
                kieClient.close();
            }
        }
    }
}