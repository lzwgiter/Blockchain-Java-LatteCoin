这个项目是我的大学毕设设计题目--区块链中身份溯源技术的仿真与评估的一部分，这里记录一下。希望大佬们可以帮我改进这个项目。
特别感谢[blockchain-java](https://github.com/longfeizheng/blockchain-java)项目，本项目最初就是从该项目演化出来的。

# LatteCoin
LatteCoin是一个使用Java开发的、基于SpringBoot框架的、基于国密算法与群签名的可溯源区块链模拟系统，该系统为授权链类型。
LatteCoin为你提供了区块链的仿真环境，你可以用这个项目去测试自己密码学方案的可行性与效率。

本项目基于Spring Boot(2.4.2) 开发，并使用thymeleaf进行了web前端的展示，便于操作，后端数据库采用JPA方式进行操作，并使用了多线程来进行挖矿模拟操作。

## 特性
1. 本项目特别使用了群签名算法来进行平衡用户的匿名性以及可溯源性
2. 本项目简单模拟了区块链中常见的行为：挖矿、交易，并添加了交易链溯源的功能
3. 本系统中集成了国密算法SM2、SM3来进行用户公私钥生成、签名以及加密

# 说明
该系统的群签名方案来自论文`Simple-Yet-Efficient Construction and Revocation of Group Signatures`
出处：Ho, Tzu-Hsin, Yen, et al. Simple-Yet-Efficient Construction and Revocation of Group Signatures.[J]. International Journal of Foundations of Computer Science, 2015.

系统中群签名算法的实现使用了JPBC库：[JPBC](http://gas.dia.unisa.it/projects/jpbc/index.html#.YLtWSL7itEZ)

# 用法
请保证后台mysql数据库开启，并创建chain_admin用户、lattechain数据库
1. `java -jar latteCoin.jar`
2. 访问本地9999端口即可进行操作

# 感谢
感谢去年在华为西研所实习时，师兄师姐们对我耐心的悉心指导，使他们让我具备了开发这个项目的能力；感谢我的学长，是将我带进了区块链大门，毕业设计期间也对我进行了非常详细的指导
