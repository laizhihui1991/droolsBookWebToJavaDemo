# droolsBookWebToJavaDemo

Drools 7 客户端调用示例工程：演示 Java 程序如何使用 Business Central（Workbench）或 KIE Server 中开发、部署的规则。

工程里有 4 个独立的 `main` 入口，对应 4 种调用方式：

| 入口类 | 调用方式 | 依赖的规则工程（KJAR） |
| --- | --- | --- |
| `org.example.Main` | 按 Maven 坐标加载 KJAR，本地执行规则 | `com.businesscentarltojava:drools_wb_to_java:1.0.0-SNAPSHOT` |
| `com.scanner.Main` | 本地执行 + `KieScanner` 定时扫描，规则热更新 | `com.businesscentarltojava:drools_wb_to_java:1.0.0-SNAPSHOT` |
| `com.kieserver.Main` | 通过 REST 远程调用 KIE Server 执行规则 | KIE Server 容器 `com.testkieserver:kieServerTest:1.0.0-SNAPSHOT` |
| `com.example.loan.LoanClient` | 按 Maven 坐标加载 KJAR，执行贷款信用评分规则 | `com.example.drools:loan-credit-score-kjar:1.0.0` |

## 技术栈

- JDK 1.8
- Maven
- Drools / KIE `7.40.0.Final`（`kie-api`、`kie-ci`、`drools-core`、`drools-compiler`、`kie-server-client`）
- fastjson `1.2.83`（打印结果 JSON）
- slf4j-simple `1.7.26`（日志输出）

## 目录结构

```
droolsBookWebToJavaDemo
├── pom.xml
└── src/main/java
    ├── org/example/Main.java                          # 示例1：加载 KJAR 本地执行
    ├── com/scanner/Main.java                          # 示例2：KieScanner 动态更新规则
    ├── com/kieserver/Main.java                        # 示例3：远程调用 KIE Server
    ├── com/example/loan
    │   ├── LoanClient.java                            # 示例4：贷款信用评分
    │   └── model/LoanApplication.java                 # 贷款申请 Fact
    ├── com/businesscentarltojava/drools_wb_to_java
    │   └── User.java                                  # 示例1、2 使用的 Fact（Data Modeler 生成）
    └── com/testkieserver/kieservertest
        ├── User.java                                  # 示例3 使用的 Fact（Data Modeler 生成）
        └── Order.java
```

> Fact 类的包名、类名、字段必须与 KJAR 中的数据模型完全一致，否则规则无法匹配，远程调用时也无法正确反序列化。

## 前置条件

1. 安装 JDK 1.8 和 Maven。
2. 示例 1、2、4 通过 `kie-ci` 从 Maven 仓库解析 KJAR，需要保证对应 KJAR 已经在本地仓库（`~/.m2/repository`）中，或者在 Maven `settings.xml` 中配置了 Business Central 的 Maven 仓库地址（一般为 `http://<host>:<port>/business-central/maven2/`）及认证信息。
3. 示例 3 需要一个可访问的 KIE Server，并已部署容器 `com.testkieserver:kieServerTest:1.0.0-SNAPSHOT`，且 `kmodule.xml` 中定义了名为 `kieServerSession` 的 KieSession。

## 构建

```bash
mvn clean compile
```

工程没有配置打包插件，可直接在 IDE 中运行各个入口类，或使用 exec 插件：

```bash
mvn compile exec:java -Dexec.mainClass="org.example.Main"
```

## 示例说明

### 示例1：加载 KJAR 本地执行规则（`org.example.Main`）

1. 用 `KieServices.newReleaseId` 指定 KJAR 坐标 `com.businesscentarltojava:drools_wb_to_java:1.0.0-SNAPSHOT`。
2. `newKieContainer(releaseId)` 从 Maven 仓库加载 KJAR。
3. 创建默认 KieSession，插入 `User(id=10000, name=来家二少, age=30)`，执行 `fireAllRules()`。
4. 输出命中的规则数量。

### 示例2：KieScanner 规则热更新（`com.scanner.Main`）

1. 与示例1 一样加载 `drools_wb_to_java` KJAR。
2. 创建 `KieScanner`，每 5 秒扫描一次 Maven 仓库，发现 KJAR 新版本后自动更新 `KieContainer`。
3. 循环中每 5 秒新建一次 KieSession，插入 `age=30` 的 `User` 并执行规则，打印命中数量。
4. 执行规则后如果 `User.age` 不再是 30（即规则已在 Business Central 中修改并重新构建部署），程序打印提示后退出，并停止扫描器。

使用方式：启动程序后，在 Business Central 中修改规则并执行 Build & Deploy，观察控制台输出变化。由于版本是 `SNAPSHOT`，扫描器可以识别同版本的更新。

### 示例3：远程调用 KIE Server（`com.kieserver.Main`）

连接配置（硬编码在类的常量中，按实际环境修改）：

| 常量 | 默认值 | 说明 |
| --- | --- | --- |
| `SERVER_URL` | `http://localhost:8180/kie-server/services/rest/server` | KIE Server REST 地址 |
| `USER` / `PWD` | 见源码 | KIE Server 用户名和密码 |
| `CONTAINER_ID` | `com.testkieserver:kieServerTest:1.0.0-SNAPSHOT` | 容器 ID，需与服务端一致 |
| `SESSION_NAME` | `kieServerSession` | 容器中定义的 KieSession 名称 |

执行流程：

1. 使用 `KieServicesFactory.newRestConfiguration` 创建 REST 配置（超时 10 秒，JSON 序列化），获取 `RuleServicesClient`。
2. 构造 `User(id=10000, name=来家二少, age=30)` 和 `Order(id=10000, userId=10000)`。
3. 组装批量命令：插入 `User`、`Order`（分别绑定别名 `User`、`Order`）+ `FireAllRules`。
4. 调用 `executeCommandsWithResults` 远程执行。
5. 成功时通过别名 `Order` 取回规则处理后的订单对象并以 JSON 输出；否则打印错误信息。最后关闭客户端。

> 账号密码目前写在源码中，仅适用于本地演示环境，正式使用时建议改为从配置文件或环境变量读取。

### 示例4：贷款信用评分（`com.example.loan.LoanClient`）

1. 加载 KJAR `com.example.drools:loan-credit-score-kjar:1.0.0`。
2. 使用 `kmodule.xml` 中定义的 KieSession `loanKSession`。
3. 插入贷款申请 `LoanApplication`：

   | 字段 | 值 | 说明 |
   | --- | --- | --- |
   | `applicantName` | 来家二少 | 申请人 |
   | `age` | 35 | 年龄 |
   | `workYears` | 8 | 工作年限 |
   | `annualIncome` | 500000 | 年收入 |
   | `goodCredit` | true | 是否信用良好 |
   | `debtRatio` | 0.30 | 负债率 |

4. 执行规则后输出命中规则数，以及规则计算出的 `creditScore`（可信积分）和 `creditLevel`（可信等级）。
5. 在 `finally` 中释放 KieSession。

## 常见问题

- 找不到 KJAR：检查 Maven 本地仓库或 `settings.xml` 中的远程仓库配置，确认坐标和版本正确。
- 规则执行数量为 0：确认 Fact 类的包名与 KJAR 中的数据模型一致，并检查规则条件是否满足。
- 示例3 提示"规则未修改 Order 对象"：检查 DRL 规则条件，或确认插入命令的别名与 `result.getValue("Order")` 一致。
- 示例3 连接失败：确认 KIE Server 已启动、端口映射正确（默认 `8180`）、账号具有 `kie-server` 角色。
