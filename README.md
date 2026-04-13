# 魔术社团网站系统

基于 Spring Boot + MySQL + JWT 的魔术社团网站后端系统。

## 🚀 功能模块

- ✅ 用户注册/登录 (JWT认证)
- ✅ 人员信息管理
- ✅ 社团论坛 (发帖/回帖/点赞)
- ✅ 资源区 (上传/下载)
- ✅ 纪念墙 (毕业成员展示)
- ✅ 开发维护名单

## 📋 技术栈

| 技术 | 版本 |
|------|------|
| Java | 17 |
| Spring Boot | 3.2.0 |
| Spring Security | 6.2 |
| MyBatis Plus | 3.5.5 |
| MySQL | 8.0+ |
| JWT | 0.12.3 |

## 🗄️ 数据库配置

1. 创建数据库
```sql
CREATE DATABASE magic_club CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本
```sql
USE magic_club;
-- 执行 db/init.sql
```

## 🔧 配置文件

修改 `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/magic_club
    username: root
    password: root
```

## 🚀 启动项目

### 方式1：IDEA运行
1. 打开项目
2. 等待Maven下载依赖
3. 运行 `MagicClubApplication`

### 方式2：命令行
```bash
mvn clean install
mvn spring-boot:run
```

## 📚 API文档

启动后访问：
- API基础地址: `http://localhost:8080/api`

### 主要接口

| 接口 | 说明 | 认证 |
|------|------|------|
| POST /user/register | 注册 | 否 |
| POST /user/login | 登录 | 否 |
| GET /user/info | 获取用户信息 | 是 |
| GET /forum/posts | 帖子列表 | 否 |
| POST /forum/post | 发帖 | 是 |
| GET /memorial/timeline | 纪念墙 | 否 |
| GET /config/departments | 部门列表 | 否 |

## 🔐 默认账号

- 用户名: `admin`
- 密码: `admin123`

## 📁 项目结构

```
magic-club/
├── src/main/java/cn/magicclub/
│   ├── MagicClubApplication.java
│   ├── config/          # 配置类
│   ├── controller/      # 控制器
│   ├── service/         # 服务层
│   ├── mapper/          # 数据访问层
│   ├── entity/          # 实体类
│   ├── security/        # 安全配置
│   └── common/          # 通用工具
├── src/main/resources/
│   └── application.yml
└── db/
    └── init.sql         # 数据库脚本
```

## 📝 开发日志

- 2025-04-13: 项目初始化，完成基础架构和6大功能模块
