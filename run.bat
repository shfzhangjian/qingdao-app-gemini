@echo off
REM 设置当前命令提示符窗口的编码为 UTF-8 (65001)
chcp 65001

REM 设置JVM的文件编码为 UTF-8，然后启动 Spring Boot JAR 包
REM 假设您的JAR包名称为 qingdao-0.0.1-SNAPSHOT.jar (根据 pom.xml)
REM 如果您的JAR包名称不同，请修改下面的文件名
java -Dfile.encoding=UTF-8 -jar qingdao-0.0.1-SNAPSHOT.jar

REM 运行结束后暂停，以便查看日志
pause