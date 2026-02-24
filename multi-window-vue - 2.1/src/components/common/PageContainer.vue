<template>
  <div class="page-container" :style="containerStyle">
    <!-- 页面头部 -->
    <div v-if="title || showHeader" class="page-header" :class="headerClasses">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">
            <span v-if="showTitleIcon" class="title-icon">
              <slot name="title-icon">
                <el-icon><Document /></el-icon>
              </slot>
            </span>
            {{ title }}
          </h1>
          <p v-if="subtitle" class="page-subtitle">{{ subtitle }}</p>
        </div>
        <div class="header-actions">
          <slot name="header-extra"></slot>
        </div>
      </div>
      <div class="header-divider"></div>
    </div>
    
    <!-- 页面内容区 -->
    <div class="page-content" :class="contentClasses">
      <div v-if="useCard" class="content-card">
        <slot></slot>
      </div>
      <slot v-else></slot>
    </div>
    
    <!-- 页面底部 -->
    <div v-if="showFooter || $slots.footer" class="page-footer">
      <slot name="footer">
        <div class="default-footer">
          <p>© 2024 校园失物招领平台 - 让遗失的物品找到回家的路</p>
        </div>
      </slot>
    </div>

    <!-- 背景装饰 -->
    <div v-if="showBackground" class="background-decoration">
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
      <div class="bg-shape shape-3"></div>
    </div>
  </div>
</template>

<script>
import { Document } from '@element-plus/icons-vue';
import { computed } from 'vue';

export default {
  name: 'PageContainer',
  components: {
    Document
  },
  props: {
    // 页面标题
    title: {
      type: String,
      default: ''
    },
    // 页面副标题
    subtitle: {
      type: String,
      default: ''
    },
    // 是否显示头部
    showHeader: {
      type: Boolean,
      default: true
    },
    // 是否显示底部
    showFooter: {
      type: Boolean,
      default: false
    },
    // 内容区域是否使用卡片样式
    useCard: {
      type: Boolean,
      default: true
    },
    // 是否居中内容
    centerContent: {
      type: Boolean,
      default: false
    },
    // 最大宽度
    maxWidth: {
      type: String,
      default: '1200px'
    },
    // 背景颜色
    backgroundColor: {
      type: String,
      default: 'transparent'
    },
    // 是否显示标题图标
    showTitleIcon: {
      type: Boolean,
      default: true
    },
    // 是否紧凑模式
    compact: {
      type: Boolean,
      default: false
    },
    // 是否显示背景装饰
    showBackground: {
      type: Boolean,
      default: false
    },
    // 头部样式类型
    headerStyle: {
      type: String,
      default: 'default', // 'default', 'minimal', 'gradient'
      validator: (value) => ['default', 'minimal', 'gradient'].includes(value)
    }
  },
  setup(props) {
    // 容器样式
    const containerStyle = computed(() => ({
      maxWidth: props.maxWidth,
      backgroundColor: props.backgroundColor
    }));

    // 头部类名
    const headerClasses = computed(() => ({
      [`header-${props.headerStyle}`]: true,
      'header-compact': props.compact
    }));

    // 内容区域类名
    const contentClasses = computed(() => ({
      'content-centered': props.centerContent,
      'content-compact': props.compact
    }));

    return {
      containerStyle,
      headerClasses,
      contentClasses
    };
  }
};
</script>

<style scoped>
.page-container {
  width: 100%;
  margin: 0 auto;
  padding: 0;
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
  transition: transform var(--app-dur-2) var(--app-ease-out), opacity var(--app-dur-2) var(--app-ease);
}

/* 页面头部样式 */
.page-header {
  margin-bottom: 0;
  padding: 0;
  background: transparent;
  transition: all 0.3s ease;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 30px 40px 20px;
  gap: 20px;
}

.title-section {
  flex: 1;
  min-width: 0;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px 0;
  line-height: 1.3;
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  display: flex;
  align-items: center;
  color: var(--primary-color);
}

.page-subtitle {
  font-size: 16px;
  color: var(--text-secondary);
  margin: 0;
  font-weight: 500;
  line-height: 1.5;
}

.header-actions {
  flex-shrink: 0;
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.header-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, rgba(15, 23, 42, 0.10) 50%, transparent 100%);
  margin: 0 40px;
}

/* 头部样式变体 */
.header-minimal .header-content {
  padding: 20px 40px 15px;
}

.header-minimal .page-title {
  font-size: 24px;
}

.header-minimal .header-divider {
  margin: 0 40px 10px;
}

.header-gradient {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.72) 0%, rgba(255, 255, 255, 0.50) 100%);
  border: 1px solid var(--border-light);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border-radius: 0 0 20px 20px;
  margin-bottom: 24px;
}

.header-gradient .header-content {
  padding: 40px 40px 25px;
}

.header-gradient .page-title {
  color: var(--text-primary);
}

.header-gradient .page-subtitle {
  color: var(--text-secondary);
}

.header-gradient .header-divider {
  background: linear-gradient(90deg, transparent 0%, rgba(15, 23, 42, 0.10) 50%, transparent 100%);
}

.header-compact .header-content {
  padding: 20px 30px 15px;
}

.header-compact .page-title {
  font-size: 24px;
}

/* 页面内容区域样式 */
.page-content {
  width: 100%;
  padding: 0;
  transition: all 0.3s ease;
}

.content-card {
  background: var(--app-surface);
  border-radius: var(--app-radius-lg);
  box-shadow: var(--app-shadow-sm);
  margin: 0 30px 30px;
  overflow: hidden;
  transition: transform var(--app-dur-2) var(--app-ease-out), box-shadow var(--app-dur-2) var(--app-ease-out);
  border: 1px solid var(--border-light);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
}

.content-centered {
  display: flex;
  justify-content: center;
}

.content-centered .content-card {
  width: 100%;
  max-width: 800px;
  margin: 0 auto 30px;
}

.content-compact .content-card {
  margin: 0 20px 20px;
}

/* 页面底部样式 */
.page-footer {
  margin-top: auto;
  padding: 30px 40px;
  text-align: center;
  color: var(--text-secondary);
  font-size: 14px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.62) 0%, rgba(255, 255, 255, 0.46) 100%);
  border-top: 1px solid var(--border-light);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
}

.default-footer {
  max-width: 600px;
  margin: 0 auto;
}

.default-footer p {
  margin: 0;
  line-height: 1.6;
  color: var(--text-secondary);
}

/* 背景装饰 */
.background-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: -1;
  overflow: hidden;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(79, 109, 245, 0.03) 0%, rgba(255, 107, 107, 0.03) 100%);
  animation: float 8s ease-in-out infinite;
}

.shape-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  right: -100px;
  animation-delay: 0s;
}

.shape-2 {
  width: 200px;
  height: 200px;
  bottom: 100px;
  left: -50px;
  animation-delay: 2s;
}

.shape-3 {
  width: 150px;
  height: 150px;
  bottom: 200px;
  right: 20%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .page-container {
    padding: 0;
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
    padding: 25px 20px 15px;
  }
  
  .header-actions {
    justify-content: flex-start;
  }
  
  .page-title {
    font-size: 26px;
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .header-divider {
    margin: 0 20px;
  }
  
  .header-gradient .header-content {
    padding: 30px 20px 20px;
  }
  
  .content-card {
    margin: 0 15px 20px;
    border-radius: 12px;
  }
  
  .content-compact .content-card {
    margin: 0 10px 15px;
  }
  
  .page-footer {
    padding: 25px 20px;
  }
  
  .shape-1 {
    width: 200px;
    height: 200px;
    top: -50px;
    right: -50px;
  }
  
  .shape-2 {
    width: 150px;
    height: 150px;
    bottom: 50px;
    left: -30px;
  }
  
  .shape-3 {
    width: 100px;
    height: 100px;
    bottom: 100px;
    right: 10%;
  }
}

@media (max-width: 480px) {
  .header-content {
    padding: 20px 15px 12px;
  }
  
  .page-title {
    font-size: 22px;
  }
  
  .page-subtitle {
    font-size: 14px;
  }
  
  .header-divider {
    margin: 0 15px;
  }
  
  .header-gradient .header-content {
    padding: 25px 15px 15px;
  }
  
  .content-card {
    margin: 0 10px 15px;
    border-radius: 10px;
  }
  
  .page-footer {
    padding: 20px 15px;
    font-size: 13px;
  }
  
  .bg-shape {
    display: none; /* 在小屏幕上隐藏背景装饰以提升性能 */
  }
}

/* 深色模式支持 */
@media (prefers-color-scheme: dark) {
  .page-title {
    color: var(--text-primary);
  }
  
  .page-subtitle {
    color: var(--text-secondary);
  }
  
  .content-card {
    background: var(--app-surface);
    border-color: var(--border-light);
    box-shadow: var(--app-shadow-sm);
  }
  
  .header-divider {
    background: linear-gradient(90deg, transparent 0%, rgba(15, 23, 42, 0.14) 50%, transparent 100%);
  }
  
  .page-footer {
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.62) 0%, rgba(255, 255, 255, 0.46) 100%);
    border-top-color: var(--border-light);
    color: var(--text-secondary);
  }
  
  .default-footer p {
    color: var(--text-secondary);
  }
}

/* 打印样式 */
@media print {
  .page-header {
    padding: 20px 0;
  }
  
  .content-card {
    box-shadow: none;
    border: 1px solid #ddd;
    margin: 0 0 20px;
  }
  
  .page-footer {
    display: none;
  }
  
  .background-decoration {
    display: none;
  }
}
</style>
