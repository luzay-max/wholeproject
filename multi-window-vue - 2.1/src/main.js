import { createApp } from 'vue'
import './style.css'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './assets/styles/global.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import focusNext from './utils/directives'

// API测试工具暂时移除，避免影响应用启动
// 如需使用测试工具，请在需要测试的组件中单独导入使用
// if (import.meta.env.DEV) {
//   try {
//     import('./utils/apiTestUtil.js').then(module => {
//       window.apiTestUtil = module.default || module;
//       console.log('API测试工具已加载成功');
//     });
//   } catch (err) {
//     console.error('API测试工具加载失败:', err);
//   }
// }

// 屏蔽 ResizeObserver loop limit exceeded 错误
// 原有的防抖处理会导致组件卸载后回调执行，从而引发 Failed to execute 'getComputedStyle' on 'Window' 错误
window.addEventListener('error', (e) => {
  if (e.message === 'ResizeObserver loop limit exceeded') {
    e.stopImmediatePropagation();
    e.preventDefault();
  }
});

// 创建 Vue 应用实例
const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(ElementPlus, {
  locale: zhCn,
})
const pinia = createPinia()

app.use(pinia)
app.use(router)

// 注册全局指令
app.directive('focus-next', focusNext)

// 挂载应用
app.mount('#app')
