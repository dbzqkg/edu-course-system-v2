
✳以下架构基于ai生成，仅供参考
---

### 一、 战略布局：四大领域限界上下文 (Bounded Contexts)

[cite_start]你设计的四个域是系统的灵魂，决定了代码的解耦边界 [cite: 1]：

* [cite_start]**基础域 (Identity & Org)**：管理学生（学号、分级、学费状态）、教师、专业关系。它是系统的准入源 [cite: 1]。
* [cite_start]**教学域 (Academic Metadata)**：定义课程元数据、标签体系和前置课依赖。它决定了“有什么课”以及“谁能选” [cite: 1]。
* [cite_start]**执行域 (Scheduling & Stock)**：管理具体的“教学班”实例、库存与时间空间分配。这是流量压力的核心 [cite: 1]。
* [cite_start]**选课域 (Transaction & Interaction)**：处理选课动作、意向轮权重计算、抢课轮原子操作，并生成最终结果 [cite: 1]。

---

### 二、 战术实现：工业级设计模式与落地

#### 1. 规格模式 (Specification Pattern) —— 解决规则地狱

* **实现细节**：将“英语 A 层限选”、“大一限选”等业务规则封装为独立的 `CourseSpec` 类。
* [cite_start]**逻辑流转**：系统查询课程关联的 `tag_set` [cite: 1][cite_start]，通过工厂根据 TagType 获取对应的 `Spec` 实例 [cite: 1][cite_start]。每个 `Spec` 仅负责一个判断（如 `student.level == 'A'`），实现高度内聚 [cite: 1]。

#### 2. 管道/责任链模式 (Validation Pipeline) —— 编排校验流

* **实现细节**：选课请求像流水线一样流过：准入校验 -> 资格校验 -> 冲突校验 -> 容量校验。
* [cite_start]**优势**：这种“插件化”设计方便随时增加规则（如临时增加“双创周专项规则”）而无需改动核心引擎 [cite: 1]。

#### 3. 物化路径模式 (Materialized Path) —— 标签层级继承

* **实现细节**：在 `tag` 表中使用 `path` 字段存储层级（如 `01-05-12`）。
* **逻辑**：选课时，系统通过 `path` 向上追溯所有父节点的约束条件（例如选“高阶英语”必须满足其父类“英语模块”的通用缴费规则）。

---

### 三、 性能架构：漏斗式多级防御

* **L1：本地缓存 (Caffeine)**：存储极度静态且高频访问的数据（如全校通用的选课准入规则），实现零 I/O 延迟。
* **L2：分布式缓存 (Redis)**：
  * **库存 (Stock)**：使用 Lua 脚本保证“读库存-扣库存”的原子性。
  * [cite_start]**极致位图 (Time Bitmap)**：将一周划分为 1024 个时间片 [cite: 1]。冲突校验简化为一次二进制 `&` 运算：$Student\_Bitmap \ \& \ Class\_Bitmap \neq 0$。
* **L3：持久层 (MySQL)**：负责最终的静态资格校验和结果持久化。

---

### 四、 工业级缺陷补全：从“能跑”到“不崩”

目前的架构虽然在“快”上做到了极致，但在大厂 P7 级别的**鲁棒性（可靠性）**上还存在三个关键短板：

#### 1. 最终一致性与“可靠性闭环”

* [cite_start]**问题**：如果 Redis 扣完库存后 Java 进程突然宕机，异步线程没来得及写 MySQL，数据就丢了 [cite: 5]。
* [cite_start]**详细实现**：引入**本地消息表**。在 Redis 操作的同时，在本地数据库记录一条“处理中”状态。通过定时任务扫描超时未完成的记录进行“补偿”写入或回滚 Redis，确保数据不丢失 [cite: 5]。

#### 2. 后端幂等性校验 (Idempotency)

* **问题**：学生疯狂点击“选课”按钮，可能导致同一个请求被发送多次。
* [cite_start]**详细实现**：在接口入口处，利用 `Redis SetNX` 实现一个基于 `studentId + classId` 的**业务分布式锁**，有效期设为 3-5 秒，确保并发下的幂等性 [cite: 5]。

#### 3. 全链路可观测性 (Observability)

* **问题**：系统报错时，难以追踪是哪个环节（准入、冲突、库存）出的错。
* **详细实现**：
  * [cite_start]**TraceID**：给每个请求分配唯一标识塞入 `ThreadLocal` [cite: 5]。
  * **UserContext Record**：使用 Java Record 存储不可变的学生特征快照（包含 ID、分级、位图），避免参数在 Service 间透传导致的逻辑混乱。

---

### 一、 为什么 V2.0 架构优于“废稿”？

你之前的废稿（基于黑马苍穹外卖风格）是典型的 **“过程式三层架构”**。而 V2.0 是 **“领域驱动的规格引擎架构”**。

| 维度 | 废稿思路 (MVC + Service 堆砌) | V2.0 架构 (DDD + 规则引擎) | 为什么选课系统更需要后者？ |
| :--- | :--- | :--- | :--- |
| **逻辑解耦** | 选课逻辑全部塞在 `ClassServiceImpl` 中，各种 `if-else` 判断英语、体育、年级。 | 逻辑分散在各个 `Specification` 类中，选课引擎只负责编排管道。 | 西电的规则年年变（如双创周、新分层），**引擎不动，规则动态加载**才是工业级标准。 |
| **分类扩展** | 简单的分类字段，稍微复杂一点就得重构数据库。 | **物化路径 (Materialized Path)** + 标签体系，支持无限级分类继承。 | 课程有“通识”、“必修”、“选修”、“双创”等多重身份，**“图”结构**比单一字段更能描述真实业务。 |
| **并发防御** | 仅仅靠 Redis 存储简单字段，缺乏动静分离。 | **多级缓存 (Caffeine + Redis)** + **Lua 原子扣减**。 | 选课是典型的“读多写少且写瞬间爆炸”，本地缓存拦截 90% 的静态查询是保命的关键。 |

---

### 二、 工业级模块构建方案

你现在的项目结构是 `common`, `pojo`, `server`。对于 V2.0，我们需要更清晰的**职责划分**，建议采用以下模块化方案：

#### 1. 模块定义 (Maven 多模块)

* `edu-common`: 基础工具类、JWT、雪花 ID、通用常量。
* `edu-pojo`: 纯粹的领域实体 (Entity)、数据传输对象 (DTO)、视图对象 (VO)。
* `edu-infrastructure`: 基础设施层。存放 Mapper、Redis 配置、第三方集成（Redisson）。
* `edu-domain`: **核心领域层（地基所在）**。不依赖 Controller，只包含业务逻辑：
  * `spec`: 存放各种 `CourseSpec` 校验逻辑。
  * `pipeline`: 存放选课执行管道。
  * `service`: 领域服务（处理跨表逻辑）。
* `edu-application`: 应用层。负责编排领域服务，处理事务。
* `edu-interfaces`: 接口层。Controller 所在，区分 `admin` 和 `student`。

---

### 三、 核心实现：如何从“第一行代码”开始？

不要被复杂的名词吓到，我们按照“从内向外”的顺序构建。

#### 1. 定义规格接口 (Specification)

在 `edu-domain` 模块中创建，这是你解耦 `if-else` 的第一步。

```java
public interface CourseSpec {
    /**
     * 是否满足选课条件
     * @param context 包含学生信息、课程标签、当前系统状态
     */
    boolean isSatisfiedBy(SelectionContext context);
    
    /**
     * 获取校验失败后的错误枚举
     */
    AppResultCode getErrorCode();
}
```

#### 2. 实现物化路径工具 (MaterializedPathUtil)

处理你手绘图中提到的 `01-05-12` 路径继承逻辑。

```java
public class MaterializedPathUtil {
    /**
     * 输入 01-05-12，返回 [01, 01-05, 01-05-12]
     * 用于一次性查出该分类及其所有父类的所有规则标签
     */
    public static List<String> getPathNodes(String path) {
        // ... 实现字符串切割逻辑
    }
}
```

#### 3. 选课核心引擎 (Pipeline)

```java
@Service
public class SelectionEngine {
    // 注入所有的规则校验器
    private final List<CourseSpec> specs; 

    public void execute(SelectionRequest request) {
        // 1. 加载上下文 (ThreadLocal 中的 UserContext)
        // 2. 遍历管道中所有相关的 Spec
        for (CourseSpec spec : specs) {
            if (!spec.isSatisfiedBy(context)) {
                throw new BusinessException(spec.getErrorCode());
            }
        }
        // 3. 执行 Redis Lua 原子扣减库存与位图
        // 4. 异步落库
    }
}
```

---

### 四、 “地基”建议

1. **先跑通“静态校验”**：今晚不要急着写 Redis。先在本地 MySQL 建立 `edu_course`、`edu_tag`、`edu_course_tag_mapping` 表。
2. **实现一个简单的 Spec**：比如 `GradeSpec`（年级校验）。尝试通过查询课程的标签，动态唤醒这个 Spec 运行。
3. **对齐 UserContext**：把你之前的 `ThreadLocalUtil` 升级为使用 `record UserContext`，并确保拦截器能在选课前把 `scheduleBitmap` 预热进去。

单体项目的“工业级”模块构建
既然是单体项目，我们不需要搞微服务那套复杂的网络调用，但我们要搞 “模块化单体 (Modular Monolith)”。这样等你以后要升级到 SpringCloud 时，只需要拆分模块，不需要重写代码。

## 建议目录结构（对应你上传的项目结构）

``` mermaid
graph TD
    %% 全局定义
    subgraph Client_Layer [客户端层]
        Student["学生端 (Web/App)"]
        Admin["管理端 (PC)"]
    end

    %% 选课端模块
    subgraph edu_server [模块: edu-server / 选课执行子系统]
        direction TB
        Interceptor["LoginInterceptor<br/>(JWT解析 / UserContext载入)"]
        AppService["SelectionAppService<br/>(业务流程编排)"]
        
        subgraph edu_domain [模块: edu-domain / 领域核心层]
            Engine["SelectionEngine<br/>(选课引擎)"]
            Pipeline["ValidationPipeline<br/>(校验管道)"]
            subgraph Specs [规格模式实现]
                S1["EnglishSpec"]
                S2["MajorSpec"]
                S3["GpaSpec (JSON配置)"]
            end
            BitmapLogic["BitmapUtil<br/>(1024位冲突计算)"]
        end
    end

    %% 管理端模块
    subgraph edu_admin [模块: edu-admin / 后台管理子系统]
        Config["RuleConfigService<br/>(规则配置/物化路径维护)"]
        Warmup["WarmupTask<br/>(数据预热到L1/L2)"]
        Excel["ExcelProcessor<br/>(批量导入学生/课程)"]
    end

    %% 基础设施层
    subgraph edu_infrastructure [模块: edu-infrastructure / 基础设施层]
        direction LR
        subgraph L1_Cache [L1: 本地缓存 Caffeine]
            C_Tag["TagRule (全量)"]
            C_Stu["ActiveUser (LRU)"]
        end
        
        subgraph L2_Cache [L2: 分布式缓存 Redis]
            R_Stock["Stock (Lua原子扣减)"]
            R_Bit["Bitmap (实时)"]
            R_PubSub["Pub/Sub (同步指令)"]
        end
        
        subgraph L3_Storage [L3: 持久层 MySQL]
            M_Course["Course (元数据)"]
            M_Tag["Tag (物化路径)"]
            M_Result["SelectionResult (真理记录)"]
        end
    end

    %% 公共模块
    subgraph edu_common [模块: edu-common]
        Util["Jwt/Snowflake/Record UserContext"]
        Ex["BusinessException / Result"]
    end

    %% 核心流转关系
    Student -->|选课请求| Interceptor
    Interceptor -->|填充| AppService
    AppService -->|调用| Engine
    Engine -->|驱动| Pipeline
    Pipeline -->|获取规则对象| L1_Cache
    Pipeline -->|执行校验| Specs
    Specs -.->|位图运算| BitmapLogic
    Engine -->|原子操作| R_Stock
    Engine -->|异步同步| L3_Storage
    
    Admin -->|管理动作| Config
    Config -->|同步| L3_Storage
    Config -->|下发失效指令| R_PubSub
    R_PubSub -->|清空旧缓存| L1_Cache
    Warmup -->|载入| L1_Cache & L2_Cache
```

``` plaintext
edu-course-system (Project Root)
├── pom.xml                                 // 父工程依赖管理，定义全系统的版本号
├── edu-common                              // 基础公共模块 (全系统共享)
│   ├── src/main/java/com/lzh/common
│   │   ├── constants/                      // 业务常量 (RedisKey, 业务状态)
│   │   ├── enums/                          // 状态码枚举 (AppResultCode)
│   │   ├── exception/                      // 业务异常 (BusinessException)
│   │   ├── result/                         // 统一返回对象 (Result, PageResult)
│   │   ├── context/                        // 关键：UserContextHolder (ThreadLocal封装)
│   │   └── util/                           // 核心工具 (Jwt, SnowflakeId, BitMapUtils)
│   └── pom.xml
├── edu-pojo                                // 领域模型模块 (纯粹的数据载体)
│   ├── src/main/java/com/lzh/pojo
│   │   ├── entity/                         // 数据库映射对象 (PO)
│   │   ├── dto/                            // 传输对象 (用于Service层入参)
│   │   ├── vo/                             // 视图对象 (用于接口返回)
│   │   └── context/                        // 核心：record UserContext (全链路快照)
│   └── pom.xml
├── edu-infrastructure                      // 基础设施模块 (屏蔽底层实现)
│   ├── src/main/java/com/lzh/infra
│   │   ├── mapper/                         // MyBatis-Plus 接口
│   │   ├── config/                         // 核心：Caffeine, Redis, Redisson 配置
│   │   ├── handler/                        // 类型处理器 (如 JSON 转 Object)
│   │   └── repository/                     // 仓库实现类 (处理DB与缓存的一致性逻辑)
│   ├── src/main/resources/mapper/          // XML 映射文件
│   └── pom.xml
├── edu-domain                              // 领域核心模块 (系统的大脑)
│   ├── src/main/java/com/lzh/domain
│   │   ├── spec/                           // 规格模式接口与实现 (EnglishSpec, GradeSpec等)
│   │   ├── pipeline/                       // 校验管道 (ValidationPipeline)
│   │   ├── model/                          // 领域聚合 (位图冲突计算逻辑、物化路径工具)
│   │   └── service/                        // 领域服务 (纯业务逻辑，不带事务和DB操作)
│   └── pom.xml
├── edu-server                              // 选课应用模块 (学生端 - 读多写少)
│   ├── src/main/java/com/lzh/server
│   │   ├── controller/                     // 选课请求入口
│   │   ├── service/                        // 应用层：编排流程、控制事务、调用领域层
│   │   ├── interceptor/                    // 登录拦截器 (预热Caffeine缓存)
│   │   └── async/                          // 异步任务 (落库MySQL, 补偿机制)
│   └── pom.xml
└── edu-admin                               // 管理端模块 (管理端 - 重型CRUD)
    ├── src/main/java/com/lzh/admin
    │   ├── controller/                     // 课程、标签、学生管理接口
    │   ├── service/                        // 管理端业务逻辑 (Excel处理、预热触发)
    │   └── task/                           // 预热任务 (将MySQL数据推向Redis/Caffeine)
    └── pom.xml
```

## 责任链实现思路

数据库中的tag令存优先级，顺序为层序
路径扁平化后通过set去重，再通过优先级队列排序，最后放入普通数组中作为享元使用（项目启动时完成）
责任链不再是传统链式调用，而是转为迭代遍历
