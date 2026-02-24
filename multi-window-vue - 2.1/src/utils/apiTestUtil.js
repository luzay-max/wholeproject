// API测试文件 - 用于开发环境测试接口连接
// 使用方法：在浏览器控制台运行相应的测试函数

import * as api from '@/api';
import { setToken } from '@/utils/authUtil';

// 基础配置
const TEST_USERNAME = 'test_user';
const TEST_PASSWORD = '123456';

// 测试结果记录
const testResults = {
  success: [],
  failed: []
};

// 输出测试结果
const printResults = () => {
  console.log('=== API测试结果 ===');
  console.log(`成功: ${testResults.success.length} 个接口`);
  testResults.success.forEach(item => {
    console.log(`✅ ${item}`);
  });
  console.log(`失败: ${testResults.failed.length} 个接口`);
  testResults.failed.forEach(item => {
    console.log(`❌ ${item}`);
  });
};

// 单个接口测试
const testApi = async (apiName, apiCall, params = {}) => {
  console.log(`测试接口: ${apiName}`);
  try {
    const result = await apiCall(params);
    console.log(`✅ ${apiName} 调用成功`, result);
    testResults.success.push(apiName);
    return result;
  } catch (error) {
    console.error(`❌ ${apiName} 调用失败`, error);
    testResults.failed.push(apiName);
    return null;
  }
};

// 测试用户认证相关接口
export const testAuthApis = async () => {
  console.log('=== 开始测试认证相关接口 ===');
  
  // 测试登录接口
  const loginResult = await testApi('login', api.login, { 
    username: TEST_USERNAME, 
    password: TEST_PASSWORD 
  });
  
  // 如果登录成功，保存token
  if (loginResult && loginResult.data && loginResult.data.token) {
    setToken(loginResult.data.token);
    console.log('Token 已保存，可以测试需要认证的接口');
    
    // 测试获取用户信息接口
    await testApi('getUserInfo', api.getUserInfo);
  }
  
  printResults();
};

// 测试失物信息相关接口
export const testLostApis = async () => {
  console.log('=== 开始测试失物信息接口 ===');
  
  // 测试获取失物列表
  await testApi('getLostList', api.getLostList, { 
    page: 1, 
    limit: 10 
  });
  
  // 测试获取热门失物
  await testApi('getHotLostList', api.getHotLostList);
  
  printResults();
};

// 测试招领信息相关接口
export const testFindApis = async () => {
  console.log('=== 开始测试招领信息接口 ===');
  
  // 测试获取招领列表
  await testApi('getFindList', api.getFindList, { 
    page: 1, 
    limit: 10 
  });
  
  // 测试获取热门招领
  await testApi('getHotFindList', api.getHotFindList);
  
  printResults();
};

// 运行所有测试
export const runAllTests = async () => {
  // 先清空测试结果
  testResults.success = [];
  testResults.failed = [];
  
  await testAuthApis();
  await testLostApis();
  await testFindApis();
  
  console.log('=== 所有测试完成 ===');
  printResults();
};

// 默认导出测试函数对象
export default {
  testAuthApis,
  testLostApis,
  testFindApis,
  runAllTests
};

// 单独导出各个函数
export {
  testAuthApis,
  testLostApis,
  testFindApis,
  runAllTests
};

// 仅在直接加载模块时输出提示
if (import.meta.url === new URL(document.currentScript?.src || '', window.location).href) {
  console.log('API测试工具已加载，请使用以下命令测试接口:');
  console.log('window.apiTestUtil.testAuthApis() - 测试认证接口');
  console.log('window.apiTestUtil.testLostApis() - 测试失物信息接口');
  console.log('window.apiTestUtil.testFindApis() - 测试招领信息接口');
  console.log('window.apiTestUtil.runAllTests() - 运行所有测试');
}