# 指调上报（JS2）告警关联方案

## 背景

告警数据通过 Kafka 分三个 topic 消费：

| Topic | 内容 | 说明 |
|-------|------|------|
| `zcenter.alarm.msg` | 告警消息 | 包含 alarmId、ruleId、risk 等 |
| `zcenter.alarm.desc` | 告警描述 | 包含 alarmId、源文件名等 |
| `zcenter.alarm.file` | 告警文件 | 文件落盘路径 |

告警文件上报 JS2 时需要告警消息中的 `ruleId`、`risk`，以及通过 `ruleId + module` 查到的 `taskId`。
但消息和文件在不同 topic，到达时机不确定，需要关联机制。

**原始逻辑**（`JS2AlarmReportJob`）：告警消息上报时将 `ruleId_risk` 存入 Redis（key=`deviceId_fileId`，TTL=900s），
告警文件上报时从 Redis 取出。该方案存在以下问题：
- Redis TTL 过期后文件无法关联
- 集群环境下本地缓存不可用
- 无持久化，重启丢失

## 关联关系

告警消息与告警文件是 **1:1** 对应关系，通过 `alarmId` 关联：

```
告警消息:  alarmId, ruleId, risk, deviceId, module
                ↕ alarmId
告警文件:  alarmId, filePath, deviceId, module
```

关联后可获取上报所需的完整信息：`ruleId + module → taskId`。

## 方案设计

### 数据库关联表

```sql
create table alarm_js2_correlation
(
    id               int auto_increment comment '主键' primary key,
    device_id        varchar(32)   not null comment '设备编号',
    alarm_id         int           not null comment '告警id',
    data_type        int           null comment '数据类型，0告警消息 1告警文件',
    rule_id          int           null comment '告警是由哪个策略id产生的',
    rule_risk        int           null comment '策略等级',
    task_id          int default 0 not null comment '任务编号，默认为0，非指调策略都是0',
    meta_data        varchar(128)  null comment '源消息，告警文件类型有用，存储告警描述',
    alarm_module     varchar(64)   not null comment '告警父类型',
    alarm_sub_module varchar(64)   not null comment '告警子类型',
    cluster_no       varchar(32)  null comment '集群节点编号，标识文件所在节点'
) comment '指调告警消息和告警文件关联表';
```

### 整体架构

```
【消息端 Kafka】zcenter.alarm.data (dataType=data)
       │
       ▼
   ImDataListener → ImKafkaDataBusiness
       │
       ▼
   ImDefaultAlarmDataTopicHandleStrategy.handle()
       │
       ├─ localDeal() ──── 本级落盘
       ├─ s2Deal() ────── 级联上报 S2
       └─ js2Deal() ───── 指调上报 JS2 + 写 correlation 表
              │
              ├─ 立即上报 JS2（ruleId/risk）
              ├─ 批量查库获取 taskId
              └─ 写入 alarm_js2_correlation (data_type=1)

【文件端 Kafka】zcenter.alarm.file (dataType=file)
       │
       ▼
   ImFileListener → ImKafkaFileBusiness
       │
       ▼
   ImDefaultAlarmFileTopicHandleStrategy.handle()
       │
       ├─ localDeal() ──── 本级落盘
       ├─ s2Deal() ────── 级联上报 S2
       └─ js2Deal() ───── 写 correlation 表 (data_type=0)

【定时任务】每 30 秒
       │
       ▼
   JS2CorrelationJob
       │
       ├─ 匹配查询（JOIN alarmId + deviceId + alarmModule + alarmSubModule）
       ├─ 上报 JS2
       └─ 批量物理删除已处理记录
```

### 处理流程

#### 消息端处理（dataType=data）

```
1. 消费告警消息，立即上报 JS2（用 ruleId/risk，不依赖文件）
2. 批量查询 taskId（ruleId + alarmModule → taskId）
3. 写入 alarm_js2_correlation 表（data_type=1），包含：
   - alarmId、deviceId、ruleId、ruleRisk、taskId
   - alarmModule、alarmSubModule
4. 并行执行 localDeal（落盘）、s2Deal（级联上报）、js2Deal（指调上报）
```

#### 文件端处理（dataType=file）

```
1. 解析 fileDesc 获取 alarmId
2. 写入 alarm_js2_correlation 表（data_type=0），包含：
   - alarmId、deviceId、alarmModule、alarmSubModule
   - metaData（fileDesc 内容）、clusterNo（本节点编号）
3. 并行执行 localDeal（落盘）、s2Deal（级联上报）、js2Deal（关联写入）
```

#### 定时任务（每 30 秒）

```
第一步：匹配查询
  SELECT m.*, f.meta_data
  FROM alarm_js2_correlation m
  JOIN alarm_js2_correlation f
      ON m.alarm_id = f.alarm_id
      AND m.device_id = f.device_id
      AND m.alarm_module = f.alarm_module
      AND m.alarm_sub_module = f.alarm_sub_module
      AND m.data_type = 1 AND f.data_type = 0
  WHERE f.cluster_no = #{currentClusterNo}

第二步：上报 JS2
  使用匹配结果中的 ruleId、taskId、metaData 等字段上报告警文件到 JS2

第三步：批量物理删除
  DELETE FROM alarm_js2_correlation WHERE id IN (matched_msg_ids..., matched_file_ids...)

第四步：超时清理（每 10 分钟）
  DELETE FROM alarm_js2_correlation WHERE create_time < NOW() - INTERVAL 10 MINUTE
  记录清理日志（孤儿数据告警）
```

### 并行处理设计

消息端使用线程池并行执行 localDeal、s2Deal、js2Deal，提高 Kafka 消费速度。

```
handle() {
    CompletableFuture.allOf(
        CompletableFuture.runAsync(() -> localDeal()),
        CompletableFuture.runAsync(() -> s2Deal()),
        CompletableFuture.runAsync(() -> js2Deal())
    ).join();
}
```

### 匹配 SQL（完整版）

```sql
SELECT
    m.id AS msg_id,
    m.rule_id, m.rule_risk, m.task_id,
    m.device_id, m.alarm_module, m.alarm_sub_module,
    f.id AS file_id, f.meta_data
FROM alarm_js2_correlation m
JOIN alarm_js2_correlation f
    ON m.alarm_id = f.alarm_id
    AND m.device_id = f.device_id
    AND m.alarm_module = f.alarm_module
    AND m.alarm_sub_module = f.alarm_sub_module
    AND m.data_type = 1 AND f.data_type = 0
WHERE f.cluster_no = #{currentClusterNo}
```

## 与现有代码的关系

| 现有类 | 变更 |
|--------|------|
| `ImDefaultAlarmFileTopicHandleStrategy` | 修改 js2Deal()，补充写入 correlation 表逻辑 |
| `ImDefaultAlarmDataTopicHandleStrategy` | 修改 js2Deal()，补充上报 JS2 + 写入 correlation 表 + 批量查 taskId |
| `ImAlarmJs2CorrelationServiceImpl` | 补充 CRUD、匹配查询、批量删除、超时清理方法 |
| `ImAlarmJs2CorrelationMapper` | 新增匹配查询、批量删除、超时清理的 XML 方法 |

| 新增类 | 说明 |
|--------|------|
| `JS2CorrelationJob` | 定时任务：匹配上报 + 批量删除 + 超时清理 |

## 注意事项

1. **消息端先于文件端到达是常态**，消息 INSERT 后文件可能几秒到几分钟才到
2. **超时时间设为 10 分钟**，覆盖正常延迟场景；超过 10 分钟未匹配视为异常，清理并告警
3. **匹配完成后批量物理删除**，避免频繁单条删除导致索引碎片
4. **定时任务间隔 30 秒**，文件上报延迟在可接受范围内
5. **不使用 Redis 做关联**，避免 TTL 过期、重启丢失、集群不同步的问题
6. **taskId 在消息端写入**，文件端无需查询策略表，匹配后直接使用
7. **cluster_no 实现集群文件定位**，告警文件落盘在特定节点，写入时记录本节点编号，定时任务只处理本节点的文件
8. **JOIN 条件包含 alarmModule + alarmSubModule**，避免跨模块误匹配
9. **本级落盘 + 级联上报逻辑完全不变**，仅在 js2Deal() 中补充关联表写入
