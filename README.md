# 【聚合搜索平台】By Tomato

> 作者：张宇恒


## 项目简介

一个基于SpringBoot + ElasticStack + Vue3 的一站式信息聚合搜索平台 用户可在同一页面集中搜索出不同来源 不同类型的内容 并提供搜索建议 搜索高亮登功能 提升搜索体验。
企业也可以直接将各项目的数据接入搜索平台（实现DataSource类的方法），复用同一套搜索后端 提升开发效率 降低系统维护成本 目前该项目已应用到我的毕设中。


### 后端

- Java Spring Boot
- MySQL 数据库
- MyBatis-Plus 及 MyBatis X 自动生成
- Swagger + Knife4j 接口文档生成
- ElasticStack
- Hutool、Apache Common Utils、Gson 等工具库

## 完成的工作：

1、前端简单实现了分页

2、将搜索建议定义为DataSource的一个新的接口，后续接入本项目的数据源可以选择按规范实现搜索建议。又是一次门面模式和适配器模式的使用。

3、使用拼音分词器实现搜索建议

4、实现搜索高亮

解决的bug：
异步调用子线程请求request时处理ThreadLocal 不能保存上下文的问题

未解决的问题：

canal无法监听数据库，即使按学长的方法做了也不行，后续考虑降mysql版本再试试

解决pinyin插件讲同音的拼音也查询出来的问题

### 部署：

ES太过占用服务器资源，已关闭该项目。