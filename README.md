# 短链接系统 V0.1
基于 SpringBoot3 + Vue3 + MySQL + Redis 开发的单机版短链接演示项目。
实现长链接转短链接、302临时重定向、Redis热点缓存、Guava布隆过滤器防缓存穿透，用于Java后端面试演示。

## ✨ 项目功能
1. 前端输入原始长链接，自动生成6位短链接，映射关系持久化存入MySQL
2. 访问短链地址，后端校验合法后通过 **302临时重定向** 跳转第三方目标网站
3. Redis缓存热点短链数据，提升访问性能，同时记录链接点击PV
4. Guava单机布隆过滤器，拦截不存在的短码请求，有效解决缓存穿透问题
5. Vue3 + Element Plus可视化前端页面，支持生成短链、一键复制短链接
6. 集成SpringDoc OpenAPI（Swagger）接口文档，方便接口调试

## 🛠️ 技术栈
### 后端
- Spring Boot 3
- MyBatis-Plus
- Redis
- MySQL 8.x
- Guava（单机布隆过滤器）
- SpringDoc OpenAPI（Swagger）

### 前端
- Vue3 (JavaScript)
- Element Plus
- Axios
- Vite

## 📁 项目目录结构

short-link-generation-system
├── README.md
├── .gitignore
├── backend                 # SpringBoot 后端代码
│   ├── pom.xml
│   └── src
│       └── main
│           ├── java        # Java 业务代码
│           └── resources
│               ├── application.yml      # 公共配置模板（无真实密码）
│               └── application-local.yml# 本地私有配置（git 忽略，存放数据库密码）
└── frontend                # Vue 前端代码

## 🚀 本地环境启动指南
### 前置环境准备
- JDK 17
- MySQL 8.0+
- Redis（Windows推荐Memurai）
- Node.js 18+

### 1. 数据库初始化
1. 新建数据库 `short_url`
2. 执行 `sql/short_url.sql` 脚本完成建表

### 2. 后端启动步骤
1. 进入 `backend/src/main/resources` 目录
2. 创建 `application-local.yml`，填写本地MySQL密码（示例如下）
```yaml
spring:
  datasource:
    password: 你的本地MySQL密码
```

1. 启动后端主类 `ShortLinkGenerationApplication`，默认端口 **8080**
2. 访问 Swagger 接口文档：`http://localhost:8080/swagger-ui/index.html`

### 3. 前端启动步骤

```
# 进入前端项目目录
cd frontend
# 安装依赖
npm install
# 启动开发服务
npm run dev
```

前端访问地址：`http://localhost:5173`

## 📖 核心业务流程

1. 用户在 Vue 前端页面输入长链接，前端通过 Axios 调用后端接口生成短码，短链映射存入 MySQL。
2. 用户在浏览器访问短链地址 `http://localhost:8080/{shortCode}`。
3. 请求进入后端，首先经过布隆过滤器校验：
   - 返回 false：代表短码一定不存在，直接返回 404，不会查询 Redis 和 MySQL，防止缓存穿透。
   - 返回 true：代表短码可能存在，继续向下执行。
4. 优先查询 Redis 缓存：
   - 缓存命中：取出原始长链接，PV 点击计数器自增。
   - 缓存未命中：查询 MySQL，校验短链是否过期、是否逻辑删除；校验通过后回填 Redis。
5. 后端校验全部通过，返回 HTTP 302 Found 响应，响应头携带原始长链接。
6. 浏览器收到 302 重定向，自动发起新请求访问第三方目标网站，完成页面跳转。

## 📝 安全说明

- 敏感数据库密码放置在 `application-local.yml`，该文件已经配置在 `.gitignore`，不会提交到代码仓库，避免密钥泄露。
- 本项目为演示 Demo，不建议直接部署到生产环境使用。

```
### 使用操作
1. 在GitHub仓库根目录新建文件，文件名填写：`README.md`
2. 把上面全部内容一次性粘贴进去，保存。
3. 把sql文件夹、short_url.sql建表脚本一并上传到仓库。
```