// 统一 API 导出文件

// 导入各个模块的 API 函数
import * as lostApi from './lostApi';
import * as findApi from './findApi';
import * as authApi from './authApi';
import * as auditApi from './auditApi';
import * as userApi from './userApi';
import * as activityApi from './activityApi';
import * as honorApi from './honorApi';
import * as aiApi from './aiApi';

// 导出所有 API 函数
export default {
  ...lostApi,
  ...findApi,
  ...authApi,
  ...auditApi,
  ...userApi,
  ...activityApi,
  ...honorApi,
  ...aiApi
};

// 单独导出各个函数以便按需导入
export * from './lostApi';
export * from './findApi';
export * from './authApi';
export * from './auditApi';
export * from './userApi';
export * from './activityApi';
export * from './honorApi';
export * from './aiApi';
