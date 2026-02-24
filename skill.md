# Skill Guide (功能实现参考)

本文档记录项目中关键功能的实现方案与参考代码片段，供开发参考。

## 1. Redis 登录状态存储 (Remember Me)

### 依赖
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

### 实现逻辑
1.  **登录成功**:
    *   生成 `accessToken` (短效, 如 2小时)。
    *   若勾选“记住我”，生成 `refreshToken` (长效, 如 7天)，存入 Redis: `Key: auth:refresh:{userId}, Value: token, Expire: 7 days`.
2.  **请求拦截**:
    *   校验 `accessToken`。若过期，前端使用 `refreshToken` 请求刷新接口。
3.  **刷新接口**:
    *   校验 `refreshToken` 是否存在且匹配 Redis 中记录。
    *   若匹配，签发新 `accessToken`。

## 2. MyBatis-Plus 逻辑删除

### 配置 (application.yml)
```yaml
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: isDeleted  # 实体类字段名
      logic-delete-value: 1          # 删除状态值
      logic-not-delete-value: 0      # 未删除状态值
```

### 实体类注解
```java
@TableLogic
private Integer isDeleted;
```

## 3. Excel 导入 (EasyExcel)

### 依赖
```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>easyexcel</artifactId>
    <version>3.x.x</version>
</dependency>
```

### 监听器模式
使用 `ReadListener` 逐行读取 Excel 数据并批量写入数据库，避免大文件内存溢出。

## 4. 全局异常处理与参数校验

### Validation 注解
*   `@NotBlank`: 字符串非空。
*   `@Pattern(regexp = "...")`: 正则校验。
*   `@Past`: 时间必须在过去。

### 统一异常拦截
使用 `@RestControllerAdvice` + `@ExceptionHandler` 捕获 `MethodArgumentNotValidException`，返回统一格式错误信息。

## 5. AOP 操作日志记录

### 定义注解 @Log
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {
    String module();
    String action();
}
```

### 切面实现
*   `@Around("@annotation(Log)")`
*   获取当前登录用户 (ThreadLocal/SecurityContext)。
*   记录方法入参、执行时间、结果/异常。
*   异步写入 `operation_log` 表。

## 6. Vue 3 + Element Plus 弹窗复用模式 (CRUD Pattern)

### 场景
后台管理系统中，新增和编辑通常共享同一个表单弹窗。

### 实现步骤
1.  **定义表单数据对象**: 使用 `reactive` 定义表单数据，字段与后端实体对齐。
2.  **定义弹窗状态**: `dialogVisible` (Boolean) 和 `isEditMode` (Boolean)。
3.  **打开弹窗方法**:
    *   **新增**: 重置表单 -> 设置 `isEditMode=false` -> 打开弹窗。
    *   **编辑**: 接收行数据 -> `Object.assign` 填充表单 -> 设置 `isEditMode=true` -> 打开弹窗。
    *   **注意**: 对于不可编辑字段 (如用户名)，在模板中根据 `isEditMode` 设置 `disabled`。
4.  **提交处理**:
    *   校验表单 (`formRef.value.validate()`)。
    *   根据 `isEditMode` 调用 `createAPI` 或 `updateAPI`。
    *   成功后关闭弹窗并刷新列表。

### 代码示例
```javascript
const handleAdd = () => {
  resetForm();
  isEditMode.value = false;
  dialogVisible.value = true;
};

const handleEdit = (row) => {
  Object.assign(form, row);
  isEditMode.value = true;
  dialogVisible.value = true;
};
```

