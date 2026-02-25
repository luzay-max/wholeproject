<template>
  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    class="login-form"
    v-focus-next="handleSubmit"
  >
    <el-form-item prop="username" class="form-item">
      <el-input
        ref="usernameRef"
        v-model="form.username"
        placeholder="请输入账号或用户名"
        size="large"
        class="custom-input"
      >
        <template #prefix>
          <div class="input-prefix">
            <el-icon><User /></el-icon>
          </div>
        </template>
      </el-input>
    </el-form-item>
    
    <el-form-item prop="password" class="form-item">
      <el-input
        ref="passwordRef"
        v-model="form.password"
        type="password"
        placeholder="请输入密码"
        size="large"
        class="custom-input"
        show-password
      >
        <template #prefix>
          <div class="input-prefix">
            <el-icon><Lock /></el-icon>
          </div>
        </template>
      </el-input>
    </el-form-item>
    
    <el-form-item prop="captchaCode" class="form-item">
      <div class="captcha-row">
        <el-input
          ref="captchaRef"
          v-model="form.captchaCode"
          placeholder="请输入验证码"
          size="large"
          class="captcha-input custom-input"
        >
          <template #prefix>
            <div class="input-prefix">
              <el-icon><Key /></el-icon>
            </div>
          </template>
        </el-input>
        <div class="captcha-wrapper" @click="fetchCaptcha">
          <img
            v-if="captchaImage"
            :src="captchaImage"
            class="captcha-image"
            alt="验证码"
          />
          <div v-else class="captcha-loading">
            <el-icon><Loading /></el-icon>
          </div>
        </div>
      </div>
    </el-form-item>
    
    <div class="form-options">
      <el-checkbox v-model="form.remember" class="remember-checkbox">
        <span class="checkbox-label">记住我</span>
      </el-checkbox>
      <el-link type="primary" :underline="false" class="forgot-link" @click="handleForgotPassword">
        忘记密码？
      </el-link>
    </div>
    
    <el-form-item class="form-item">
      <el-button
        type="primary"
        size="large"
        class="submit-btn"
        :loading="loading"
        @click="handleSubmit"
      >
        <span v-if="!loading">登录</span>
        <span v-else>登录中...</span>
      </el-button>
    </el-form-item>
  </el-form>
</template>

<script>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { User, Lock, Key, Loading } from '@element-plus/icons-vue';
import { getCaptcha } from '../../api/userApi';
import { useUserStore } from '../../store/userStore';

export default {
  name: 'LoginForm',
  emits: ['login-success', 'to-register'],
  setup(props, { emit }) {
    const userStore = useUserStore();
    const formRef = ref(null);
    const loading = ref(false);
    const captchaId = ref('');
    const captchaImage = ref('');
    
    const form = reactive({
      username: '',
      password: '',
      captchaCode: '',
      remember: false
    });
    
    const rules = {
      username: [
        { required: true, message: '请输入账号或用户名', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
      ],
      captchaCode: [
        { required: true, message: '请输入验证码', trigger: 'blur' }
      ]
    };
    
    const fetchCaptcha = async () => {
      try {
        const res = await getCaptcha();
        captchaId.value = res.data.captchaId;
        captchaImage.value = `data:image/png;base64,${res.data.imageBase64}`;
        form.captchaCode = '';
      } catch (error) {
        console.error('获取验证码失败:', error);
      }
    };
    
    const handleSubmit = async () => {
      if (!formRef.value) return;
      
      try {
        await formRef.value.validate();
        loading.value = true;
        
        await userStore.login({
          username: form.username,
          password: form.password,
          remember: form.remember,
          captchaId: captchaId.value,
          captchaCode: form.captchaCode
        });
        
        if (form.remember) {
          localStorage.setItem('rememberedUser', JSON.stringify({
            username: form.username
          }));
        } else {
          localStorage.removeItem('rememberedUser');
        }
        
        emit('login-success');
      } catch (error) {
        if (error !== false) {
          fetchCaptcha();
        }
      } finally {
        loading.value = false;
      }
    };

    const handleForgotPassword = () => {
      ElMessage.warning('请联系管理员');
    };
    
    onMounted(() => {
      const remembered = localStorage.getItem('rememberedUser');
      if (remembered) {
        const userInfo = JSON.parse(remembered);
        form.username = userInfo.username || '';
        form.remember = true;
      }
      fetchCaptcha();
    });
    
    return {
      formRef,
      form,
      rules,
      loading,
      captchaImage,
      User,
      Lock,
      Key,
      Loading,
      fetchCaptcha,
      handleSubmit,
      handleForgotPassword
    };
  }
};
</script>

<style scoped>
.login-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form-item {
  margin-bottom: 4px;
}

.custom-input :deep(.el-input__wrapper) {
  border-radius: 12px;
}

.input-prefix {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  background: var(--page-accent-soft, rgba(37, 99, 235, 0.12));
  color: var(--page-accent, #2563eb);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.captcha-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.captcha-input {
  flex: 1;
}

.captcha-wrapper {
  width: 110px;
  height: 36px;
  border-radius: 10px;
  border: 1px solid var(--color-border-secondary);
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;
  flex-shrink: 0;
}

.captcha-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.captcha-loading {
  color: var(--color-text-tertiary);
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: var(--color-text-secondary);
}

.remember-checkbox :deep(.el-checkbox__label) {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.forgot-link {
  font-size: 12px;
}

.submit-btn {
  width: 100%;
  height: 46px;
  border-radius: 10px;
  font-weight: 600;
}

.login-form :deep(.el-form-item__error) {
  position: static;
  margin-top: 6px;
  line-height: 1.35;
}

@media (max-width: 480px) {
  .captcha-wrapper {
    width: 96px;
    height: 34px;
  }
}
</style>

