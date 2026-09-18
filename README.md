🔗 短链接生成系统
基于 SpringBoot3 + Vue3 + MySQL + Redis + ECharts 开发的企业级短链接服务系统
本项目为个人实战开源项目，从 0 搭建完整短链业务，包含短链生成、302 重定向、缓存优化、防穿透、访问数据统计可视化等全套功能。

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
```
shortlink-project
├── README.md
├── .gitignore
├── shortLinkGeneration     # SpringBoot 后端代码
│   ├── pom.xml
│   └── src
│       └── main
│           ├── java        # Java 业务代码
│           └── resources
│               ├── application.yml      # 公共配置模板（无真实密码）
│               └── application-local.yml# 本地私有配置（git 忽略，存放数据库密码）
└── short-link-admin        # Vue 前端代码
```

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


✨ 更新日志 V0.2
- ✅ 新增短链访问日志异步记录（@Async 异步落库，不阻塞重定向主流程）
- ✅ 完整多维度访问统计功能：PV总访问量、UV独立访客（IP去重）
- ✅ 时间趋势可视化：按天访问折线图
- ✅ 访问来源分布饼图（直接访问/外部链接）
- ✅ 访问设备分布饼图（PC/手机/未知设备）
- ✅ 解耦统计查询功能：支持前端任意输入短链编码查询历史数据，不再依赖生成动作
- ✅ 前端一键复制短链接功能，带复制成功提示
- ✅ 页面平滑滚动、空数据处理、加载状态优化
📌 项目整体功能
V0.1 基础功能
- 长链接转 6 位随机短链接，持久化存储 MySQL
- 302 临时重定向跳转目标网址
- Redis 热点缓存优化，提升高并发访问性能
- Guava 布隆过滤器解决短链缓存穿透问题
- 支持自定义链接过期时间
- 集成 Swagger 在线接口文档
V0.2 高级统计功能（新版核心亮点）
- 异步访问日志记录：用户访问短链时异步记录 IP、访问时间、设备、来源地址
- 多维度数据聚合统计
- 前端 ECharts 可视化展示，图表自适应窗口
- 独立统计查询接口：支持通过短链编码随时查阅历史访问数据
- 生成短链后一键查看统计，自动回填短码、自动滚动到统计区域

🚀 项目启动方式
1. 环境依赖
- JDK 17
- MySQL 8.0+
- Redis
- Node.js 18+
2. 数据库初始化
创建数据库 short_url，执行项目内 SQL 脚本，包含：
- 短链主表
- 访问日志明细表（V0.2 新增）
3. 后端启动
在 application.yml 配置自己的数据库密码，启动后端主类 `ShortLinkGenerationApplication`，默认端口 **8080**。
接口文档地址：http://localhost:8080/swagger-ui/index.html
4. 前端启动
cd short-link-admin
npm install
npm run dev


📄 版本说明
- V0.1：基础短链生成、重定向、缓存、防穿透功能
- V0.2：新增完整访问统计系统 + 数据可视化 + 前端交互优化（当前版本）
📝 安全说明
数据库账号密码等敏感信息存放在 application-local.yml，已配置 Git 忽略，不会上传至仓库，保证项目安全。
