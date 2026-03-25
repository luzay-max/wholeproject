<template>
  <div class="register-page">
    <!-- 背景装饰 -->
    <div class="background-decoration">
      <div class="decoration-circle circle-1"></div>
      <div class="decoration-circle circle-2"></div>
      <div class="decoration-circle circle-3"></div>
      <div class="decoration-circle circle-4"></div>
    </div>
    
    <!-- 主内容区 -->
    <div class="register-container">
      <!-- 左侧品牌区 -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="brand-icon">
            <el-icon><Plus /></el-icon>
          </div>
          <h1 class="brand-title">加入我们</h1>
          <p class="brand-slogan">创建账号，开启失物招领之旅</p>
          <div class="brand-benefits">
            <h3>注册即享</h3>
            <div class="benefit-item">
              <el-icon><Bell /></el-icon>
              <span>实时推送最新失物/招领信息</span>
            </div>
            <div class="benefit-item">
              <el-icon><Star /></el-icon>
              <span>参与光荣榜评选，获得荣誉积分</span>
            </div>
            <div class="benefit-item">
              <el-icon><ChatDotRound /></el-icon>
              <span>发布评论，与校园社区互动</span>
            </div>
          </div>
          <div class="login-prompt">
            <span>已有账号？</span>
            <el-button type="primary" @click="handleToLogin">
              立即登录
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 右侧注册区 -->
      <div class="register-section">
        <div class="register-card">
          <div class="card-header">
            <h2 class="card-title">用户注册</h2>
            <p class="card-subtitle">请填写以下信息完成注册</p>
          </div>
          
          <RegisterForm
            @register-success="handleRegisterSuccess"
            @to-login="handleToLogin"
          />

          <div class="mobile-login-entry">
            <span>已有账号？</span>
            <el-button type="primary" text @click="handleToLogin">
              返回登录
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 底部版权 -->
    <div class="page-footer">
      <span>© 2024 校园失物招领平台 · 让丢失的物品找到回家的路</span>
    </div>
  </div>
</template>

<script>
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Plus, Bell, Star, ChatDotRound, ArrowRight } from '@element-plus/icons-vue';
import RegisterForm from '../components/user/RegisterForm.vue';

export default {
  name: 'Register',
  components: {
    Plus,
    Bell,
    Star,
    ChatDotRound,
    ArrowRight,
    RegisterForm
  },
  setup() {
    const router = useRouter();
    
    const handleRegisterSuccess = () => {
      ElMessage.success('注册成功，请登录');
      router.push('/login');
    };
    
    const handleToLogin = () => {
      router.push('/login');
    };
    
    return {
      handleRegisterSuccess,
      handleToLogin
    };
  }
};
</script>

<style scoped>
.register-page {
  --page-accent: #2563eb;
  --page-accent-strong: #1d4ed8;
  height: 100dvh;
  min-height: 100vh;
  width: 100%;
  position: relative;
  overflow-x: hidden;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  background: radial-gradient(900px 360px at 10% -10%, rgba(37, 99, 235, 0.12), transparent 60%),
    linear-gradient(180deg, #f8fafc 0%, #ffffff 70%);
}

.background-decoration {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.2;
}

.circle-1 {
  width: 360px;
  height: 360px;
  background: rgba(37, 99, 235, 0.25);
  top: -120px;
  left: -120px;
}

.circle-2 {
  width: 260px;
  height: 260px;
  background: rgba(15, 118, 110, 0.2);
  top: 55%;
  right: -60px;
}

.circle-3 {
  width: 200px;
  height: 200px;
  background: rgba(245, 158, 11, 0.18);
  bottom: 10%;
  left: 25%;
}

.circle-4 {
  width: 140px;
  height: 140px;
  background: rgba(109, 40, 217, 0.16);
  bottom: -40px;
  right: 20%;
}

.register-container {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(360px, 1.1fr) minmax(340px, 0.9fr);
  min-height: calc(100dvh - 56px);
  padding: 32px 40px;
  gap: 40px;
  align-items: center;
  justify-content: center;
}

.brand-section {
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-content {
  text-align: left;
  padding: 24px;
  max-width: 520px;
}

.brand-icon {
  width: 72px;
  height: 72px;
  border-radius: 18px;
  background: var(--page-accent);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34px;
  color: #fff;
  margin-bottom: 20px;
  box-shadow: 0 12px 30px rgba(37, 99, 235, 0.28);
}

.brand-title {
  font-size: 30px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0 0 12px 0;
  letter-spacing: 1px;
}

.brand-slogan {
  font-size: 15px;
  color: var(--color-text-secondary);
  margin: 0 0 22px 0;
  line-height: 1.6;
}

.brand-benefits {
  background: #ffffff;
  border-radius: 16px;
  padding: 16px 18px;
  border: 1px solid var(--color-border-secondary);
  box-shadow: var(--shadow-1);
}

.brand-benefits h3 {
  margin: 0 0 12px 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.benefit-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: var(--color-text-secondary);
  padding: 6px 0;
}

.benefit-item .el-icon {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: rgba(37, 99, 235, 0.12);
  color: var(--page-accent);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.login-prompt {
  margin-top: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.register-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  max-height: calc(100dvh - 120px);
  overflow: auto;
  padding-right: 2px;
}

.register-card {
  width: 100%;
  max-width: 440px;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 20px;
  padding: 32px 32px 28px;
  box-shadow: 0 18px 50px rgba(15, 23, 42, 0.12);
  border: 1px solid rgba(226, 232, 240, 0.8);
}

.card-header {
  text-align: center;
  margin-bottom: 24px;
}

.card-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 6px 0;
}

.card-subtitle {
  font-size: 13px;
  color: var(--color-text-tertiary);
  margin: 0;
}

.mobile-login-entry {
  display: none;
  margin-top: 12px;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.page-footer {
  position: absolute;
  bottom: 18px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 12px;
  color: var(--color-text-tertiary);
}

@media (max-width: 992px) {
  .register-container {
    grid-template-columns: 1fr;
    padding: 50px 24px;
  }

  .brand-content {
    text-align: center;
  }
}

@media (max-width: 576px) {
  .register-page {
    height: 100dvh;
  }

  .register-container {
    grid-template-columns: 1fr;
    min-height: auto;
    align-items: flex-start;
    gap: 12px;
    padding: max(12px, env(safe-area-inset-top)) 10px 10px;
  }

  .brand-section {
    display: none;
  }

  .register-section {
    width: 100%;
    max-height: none;
    overflow: visible;
  }

  .register-card {
    max-width: none;
    width: 100%;
    padding: 20px 14px 16px;
    border-radius: 16px;
    box-shadow: 0 12px 28px rgba(15, 23, 42, 0.12);
  }

  .mobile-login-entry {
    display: flex;
  }

  .page-footer {
    position: static;
    transform: none;
    padding: 8px 12px 16px;
    text-align: center;
  }

  .register-card {
    padding: 20px 14px 16px;
  }

  .brand-title {
    font-size: 24px;
  }

  .brand-icon {
    width: 60px;
    height: 60px;
    font-size: 28px;
  }
}
</style>

