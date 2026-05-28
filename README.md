# 笔试题解答

> **规则**：完成编码，在本文档中标注实现逻辑及对应源码；提交至个人 GitHub 后在此填写仓库链接。  
> **GitHub 链接**：https://github.com/kyjX/Interviews

### 环境要求

| 组件 | 版本要求 | 说明 |
|------|----------|------|
| **JDK** | 17+ | Spring Boot 3 必需；Maven 编译前请确认 `JAVA_HOME` 指向 JDK 17 |
| **Maven** | 3.6+ | 构建 `spring-rw`、`fibonacci` |
| **Node.js** | 18+ | 构建/运行 `node-server`（需已安装并加入 PATH） |
| **Python** | 3.10+ | 运行 `python-project` |

Windows 若 Maven 默认用了 JDK 8，可先设置（路径按本机调整）：

```powershell
$env:JAVA_HOME = "D:\Users\DWork\DJAVA\JDK17"
```

---

## 目录

| 题号 | 内容 | 源码目录 |
|------|------|----------|
| 1 | Spring 读写分离（无第三方路由包） | `spring-rw/` |
| 2 | 自定义注解 + AOP | `spring-rw/`（`@Timed` 等） |
| 3 | SQL 获取上周一 | `sql/last_monday.sql` |
| 4 | SQL 相似度查询 | `sql/similarity_query.sql` |
| 5 | 斐波那契数列（Java） | `fibonacci/` |
| 6 | TypeScript + Node.js 服务端 | `node-server/` |
| 7 | Python 工程化 | `python-project/` |

---

## 1. Spring 读写分离（不使用 ShardingSphere 等第三方路由包）

### 实现逻辑

1. **双数据源**：配置 `masterDataSource`（写）与 `slaveDataSource`（读），演示环境用两个 H2 内存库模拟主从。
2. **动态路由**：继承 Spring 自带的 `AbstractRoutingDataSource`，在 `determineCurrentLookupKey()` 中读取 `ThreadLocal` 里的路由键（`MASTER` / `SLAVE`）。
3. **线程上下文**：`DataSourceContextHolder` 用 `ThreadLocal` 保存当前线程数据源类型，避免多线程串库。
4. **注解驱动**：自定义 `@ReadOnly`、`@WriteOnly`，由 `DataSourceAspect` 在方法执行前 `set`、执行后 `clear`。
5. **业务使用**：`UserService` 写操作用 `@WriteOnly`，查操作用 `@ReadOnly`；统一注入基于路由数据源的 `JdbcTemplate`。

```
请求 → @ReadOnly/@WriteOnly → AOP 设置 ThreadLocal
     → JdbcTemplate → RoutingDataSource → 主库/从库
     → finally 清理 ThreadLocal
```

### 核心源码

**路由数据源**

```java
// spring-rw/src/main/java/com/example/rw/datasource/RoutingDataSource.java
public class RoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        DataSourceType type = DataSourceContextHolder.get();
        return type != null ? type.name() : DataSourceType.MASTER.name();
    }
}
```

**AOP 切换数据源**

```java
// spring-rw/src/main/java/com/example/rw/aspect/DataSourceAspect.java
@Around("@annotation(com.example.rw.annotation.ReadOnly) || @annotation(com.example.rw.annotation.WriteOnly)")
public Object route(ProceedingJoinPoint joinPoint) throws Throwable {
    DataSourceContextHolder.set(resolveType(...));
    try {
        return joinPoint.proceed();
    } finally {
        DataSourceContextHolder.clear();
    }
}
```

**业务示例**

```java
// spring-rw/src/main/java/com/example/rw/service/UserService.java
@WriteOnly
public void createUser(long id, String name) { ... }

@ReadOnly
public List<Map<String, Object>> listUsers() { ... }
```

### 运行方式

```bash
cd spring-rw
mvn spring-boot:run
```

> 说明：真实主从需从库同步主库数据；本 demo 两个独立 H2 仅演示**路由机制**，从库查不到主库刚写入的数据属预期现象。

---

## 2. 自定义注解

### 实现逻辑

除读写注解外，另实现 **`@Timed`**：标记需要统计耗时的方法；`TimedAspect` 通过 `@Around("@annotation(timed)")` 拦截，在 `finally` 中打印耗时日志。体现「注解定义元数据 → 切面解析注解 → 横切逻辑」的标准 Spring 自定义注解模式。

### 核心源码

**注解定义**

```java
// spring-rw/src/main/java/com/example/rw/annotation/Timed.java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Timed {
    String value() default "";
}
```

**切面实现**

```java
// spring-rw/src/main/java/com/example/rw/aspect/TimedAspect.java
@Around("@annotation(timed)")
public Object logElapsed(ProceedingJoinPoint joinPoint, Timed timed) throws Throwable {
    long start = System.nanoTime();
    try {
        return joinPoint.proceed();
    } finally {
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        log.info("[Timed] {} finished in {} ms", label, elapsedMs);
    }
}
```

**使用**

```java
@Timed("createUser")
public void createUser(long id, String name) { ... }
```

---

## 3. SQL：获取上周一日期

### 实现逻辑

- `WEEKDAY(CURDATE())`：MySQL 中周一为 0，得到「今天距本周一」的天数。
- `CURDATE() - INTERVAL WEEKDAY(...) DAY`：得到**本周一**。
- 再减 7 天 → **上周一**。

### 源码

见 [`sql/last_monday.sql`](sql/last_monday.sql)：

```sql
SELECT DATE_SUB(
           DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY),
           INTERVAL 7 DAY
       ) AS last_monday;
```

---

## 4. SQL：相似度查询

### 实现逻辑

1. 用 `LIKE '%关键词%'` 做初筛，减少全表扫描范围。
2. 用 `LOCATE` 判断是否包含关键词；用 `CHAR_LENGTH(关键词)/CHAR_LENGTH(字段)` 作为简易相似度（完全匹配为 1.0）。
3. 对 `name`、`description` 分别打分，取 `GREATEST` 作为最终 `similarity`，按降序返回 Top N。
4. 注释中补充 PostgreSQL `pg_trgm` 的生产级写法。

### 源码

见 [`sql/similarity_query.sql`](sql/similarity_query.sql)。

---

## 5. 斐波那契数列（Java）

### 实现逻辑

采用 **Java 迭代**实现：时间复杂度 O(n)，空间 O(1)（单值 `fibonacci(n)`），避免递归栈溢出与重复子问题计算。  
`sequence(count)` 用于生成前 count 项列表，便于演示输出。

### 源码

见 [`fibonacci/src/main/java/com/example/fibonacci/Fibonacci.java`](fibonacci/src/main/java/com/example/fibonacci/Fibonacci.java)：

```java
public static long fibonacci(int n) {
    if (n <= 1) return n;
    long a = 0, b = 1;
    for (int i = 2; i <= n; i++) {
        long next = a + b;
        a = b;
        b = next;
    }
    return b;
}
```

### 运行方式

```bash
cd fibonacci
mvn -q compile exec:java
# 指定 n：mvn -q compile exec:java -Dexec.args=20
```

---

## 6. TypeScript + Node.js 服务端

### 实现逻辑

- **Express** 作为 HTTP 框架，`createApp()` 工厂函数便于测试与复用。
- **路由分层**：`routes/health.ts` 处理健康检查；版本信息在 `app.ts` 内联注册，结构清晰。
- **API**：
  - `GET /api/health`：健康检查
  - `GET /api/version`：服务名称与版本
- **TypeScript**：`strict` 模式，`NodeNext` 模块解析，ESM（`"type": "module"`）。

### 目录结构

```
node-server/
├── package.json
├── tsconfig.json
└── src/
    ├── index.ts          # 入口，监听端口
    ├── app.ts            # 组装 Express
    └── routes/
        └── health.ts
```

### 运行方式

```bash
cd node-server
npm install
npm run dev
# 访问 http://localhost:3000/api/health
# 访问 http://localhost:3000/api/version
```

### 核心源码

```typescript
// node-server/src/app.ts
export function createApp(): Express {
  const app = express();
  app.use(express.json());
  app.use("/api/health", healthRouter);
  app.get("/api/version", (_req, res) => {
    res.json({ name: "written-test-node-server", version: "1.0.0" });
  });
  return app;
}
```

---

## 7. Python 工程化

### 实现逻辑

体现常见 Python 项目规范：

| 实践 | 说明 |
|------|------|
| **src 布局** | 包在 `src/written_test/`，避免与测试目录同名冲突 |
| **pyproject.toml** | 统一构建、依赖、工具配置（PEP 621） |
| **可编辑安装** | `pip install -e ".[dev]"` |
| **CLI 入口** | `[project.scripts]` 注册 `wt-demo` 命令 |
| **配置外置** | `config.py` + 环境变量 `LOG_LEVEL`、`GREETING` |
| **测试** | `pytest` 覆盖业务模块 `greeter` |
| **代码质量** | `ruff` 配置于 `pyproject.toml` |

### 目录结构

```
python-project/
├── pyproject.toml
├── src/written_test/
│   ├── __init__.py
│   ├── config.py
│   ├── greeter.py
│   └── cli.py
└── tests/
    └── test_greeter.py
```

### 运行方式

```bash
cd python-project
pip install -e ".[dev]"
pytest
wt-demo Alice
```

---

## 提交检查清单

- [ ] 代码已 push 到个人 GitHub
- [ ] 本 README 顶部已填写仓库链接
- [ ] `JAVA_HOME` 已指向 JDK 17（见上文「环境要求」）
- [ ] Spring：`cd spring-rw && mvn spring-boot:run`（日志可见 `@Timed` 与 DemoRunner）
- [ ] 斐波那契：`cd fibonacci && mvn compile exec:java`（输出 `fib(10) = 55`）
- [ ] Node：`cd node-server && npm install && npm run dev`（访问 `/api/health`、`/api/version`）
- [ ] Python：`cd python-project && pip install -e ".[dev]" && pytest && wt-demo Alice`
- [ ] 勿提交 `target/`、`node_modules/`、`.pytest_cache/`（已配置 `.gitignore`）
