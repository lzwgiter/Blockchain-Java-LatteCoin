> 这个项目是我的大学毕设设计题目--区块链中身份溯源技术的仿真与评估的一部分，这里记录一下。希望大佬们可以帮我改进这个项目。 特别感谢blockchain-java项目，本项目最初就是从该项目演化出来的。

# LatteCoin
LatteCoin是一个使用Java开发的、基于SpringBoot框架的、基于国密算法与群签名的可溯源区块链模拟系统，该系统为授权链类型。 LatteCoin为你提供了区块链的仿真环境，你可以用这个项目去测试自己密码学方案的可行性与效率。

本项目基于Spring Boot(2.4.2) 开发，并使用thymeleaf进行了web前端的展示，便于操作。后端数据库采用Spring JPA方式进行操作，并使用了多线程来进行挖矿模拟操作。

# 特性
本项目特别使用了群签名算法来进行平衡用户的匿名性以及可溯源性
本项目简单模拟了区块链中常见的行为：挖矿、交易，并添加了交易链溯源的功能
本系统中集成了国密算法SM2、SM3来进行用户公私钥生成、签名以及加密

# 说明
该系统的群签名方案来自论文Simple-Yet-Efficient Construction and Revocation of Group Signatures 出处：Ho, Tzu-Hsin, Yen, et al.
Simple-Yet-Efficient Construction and Revocation of Group Signatures.[J]. International Journal of Foundations of
Computer Science, 2015.

系统中群签名算法的实现使用了JPBC库：[JPBC](http://gas.dia.unisa.it/projects/jpbc/index.html#.YLtWSL7itEZ)

需要下载jpbc到本地，解压后，在目录下打开命令行，然后在命令行中输入：
```cmd
mvn clean package install
```

如果有报错，如jdk版本、编码问题，请修改和添加如下内容到jpbc根目录的pom.xml中：
```xml
<!-- 任意处添加 -->
<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```
```xml
<!-- 修改下面的内容为1.8 -->
<source>1.5</source>
<target>1.5</target>
```

# 用法
请保证后台mysql数据库开启，并创建chain_admin用户、lattechain数据库。sql语句如下：
1. 创建空密码用户：`create USER 'chain_admin';`
2. 创建数据库：`create database lattechain;`
3. 授权：`grant ALL on lattechain.* to chain_admin@'%';`
   无需建立数据表，本项目使用了Spring JPA，会自动根据对象创建数据表。

jar包运行方法：
1. `java -jar latteCoin.jar`
2. 访问本地9999端口即可进行操作

项目运行截图：
![整体架构](https://i.imgtg.com/2023/03/14/flZPj.png)

![发起交易](https://i.imgtg.com/2023/03/14/flKFx.png)

![交易溯源](https://i.imgtg.com/2023/03/20/9iPWl.png)

# 感谢
感谢去年在华为西研所实习时，前辈们对我耐心的悉心指导，使他们让我具备了开发这个项目的能力；感谢实验室师兄将我带进了区块链大门，毕业设计期间也对我进行了非常详细的指导。

个人主页：https://lzwgiter.github.io
