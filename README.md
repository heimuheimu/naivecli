# NaiveCli: 为 Jar 项目提供基于文本交互的简单命令行工具。

[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/heimuheimu/naivecli.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/heimuheimu/naivecli/context:java)

## 使用要求
* JDK 版本：1.8+ 
* 依赖类库：
  * [slf4j-log4j12 1.7.5+](https://mvnrepository.com/artifact/org.slf4j/slf4j-log4j12)

## Maven 配置
```xml
    <dependency>
        <groupId>com.heimuheimu</groupId>
        <artifactId>naivecli</artifactId>
        <version>1.0</version>
    </dependency>
```

### Spring 配置
```xml
    <!-- 命令行工具配置 -->
    <bean id="naiveCommandLineUtilities" class="com.heimuheimu.naivecli.NaiveCommandLineUtilities"
          init-method="init" destroy-method="close">
        <constructor-arg index="0" value="4183" /> <!-- Socket 监听端口 -->
        <constructor-arg index="1">
            <util:list>
                <bean class="com.heimuheimu.naivecli.demo.VersionCommand" /> <!-- 具体命令实现，请参考下面的示例代码实现-->
            </util:list>
        </constructor-arg>
        <constructor-arg index="2" value="5" /> <!-- 最大连接数量，建议为 5 个 -->
        <constructor-arg index="3" value="90" /> <!-- 连接最大闲置秒数，建议为 90 秒，超过该时间的未使用连接将会被自动关闭 -->
    </bean>
```

### 示例代码

显示当前版本号命令：
```java
    public class VersionCommand implements NaiveCommand {
    
        @Override
        public String getName() {
            return "version"; // 命令名称
        }
    
        @Override
        public List<String> execute(String[] strings) {
            List<String> outputList = new ArrayList<>(); // 命令输出内容，每个元素一行
            outputList.add("demo v1.0-SNAPSHOT");
            return outputList;
        }
    }
```

### 使用说明
在项目启动后，可通过 "telnet 127.0.0.1 4183" 进行 Socket 连接，输入 "version" 命令后回车，可得到版本号信息输出 "demo v1.0-SNAPSHOT"，输入 "quit" 命令退出 NaiveCli 命令行工具。

## 更多信息
* [NaiveCli v1.0 API Doc](https://heimuheimu.github.io/naivecli/api/v1.0/)
* [NaiveCli v1.0 源码下载](https://heimuheimu.github.io/naivecli/download/naivecli-1.0-sources.jar)
* [NaiveCli v1.0 Jar包下载](https://heimuheimu.github.io/naivecli/download/naivecli-1.0.jar)