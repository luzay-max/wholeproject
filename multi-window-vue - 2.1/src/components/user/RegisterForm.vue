<template>
  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    class="register-form"
    label-position="top"
    v-focus-next="handleSubmit"
  >
    <div class="form-row">
      <el-form-item label="用户名" prop="username" class="form-col">
        <el-input
          ref="usernameRef"
          v-model="form.username"
          placeholder="3-20位字母、数字或下划线"
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

      <el-form-item label="密码" prop="password" class="form-col">
        <el-input
          ref="passwordRef"
          v-model="form.password"
          type="password"
          placeholder="8-20位字母+数字"
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
    </div>

    <el-form-item label="确认密码" prop="confirmPassword" class="form-item">
      <el-input
        ref="confirmPasswordRef"
        v-model="form.confirmPassword"
        type="password"
        placeholder="请再次输入密码"
        size="large"
        class="custom-input"
        show-password
      >
        <template #prefix>
          <div class="input-prefix">
            <el-icon><CircleCheck /></el-icon>
          </div>
        </template>
      </el-input>
    </el-form-item>

    <div class="form-row">
      <el-form-item label="手机号" prop="phone" class="form-col">
        <el-input
          ref="phoneRef"
          v-model="form.phone"
          placeholder="请输入手机号"
          size="large"
          class="custom-input"
        >
          <template #prefix>
            <div class="input-prefix">
              <el-icon><Phone /></el-icon>
            </div>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item label="邮箱" prop="email" class="form-col">
        <el-input
          ref="emailRef"
          v-model="form.email"
          placeholder="请输入邮箱"
          size="large"
          class="custom-input"
        >
          <template #prefix>
            <div class="input-prefix">
              <el-icon><Message /></el-icon>
            </div>
          </template>
        </el-input>
      </el-form-item>
    </div>

    <div class="form-row">
      <el-form-item label="真实姓名" prop="name" class="form-col">
        <el-input
          ref="nameRef"
          v-model="form.name"
          placeholder="请输入真实姓名"
          size="large"
          class="custom-input"
          v-focus-next
        >
          <template #prefix>
            <div class="input-prefix">
              <el-icon><Postcard /></el-icon>
            </div>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item label="学号/工号" prop="studentId" class="form-col">
        <el-input
          ref="studentIdRef"
          v-model="form.studentId"
          placeholder="请输入学号或工号"
          size="large"
          class="custom-input"
          v-focus-next
        >
          <template #prefix>
            <div class="input-prefix">
              <el-icon><Postcard /></el-icon>
            </div>
          </template>
        </el-input>
      </el-form-item>
    </div>

    <div class="form-row">
      <el-form-item label="身份" prop="role" class="form-col">
        <DictSelect
          v-model="form.role"
          dict-type="sys_user_role"
          placeholder="请选择"
          class="custom-select"
          @keyup.enter="handleSubmit"
        >
          <template #option="{ item }">
            <span>{{ item.label || item.dictLabel }}</span>
            <span class="option-hint" v-if="item.remark">{{ item.remark }}</span>
          </template>
        </DictSelect>
      </el-form-item>
    </div>

    <div class="agreement-row">
      <el-checkbox v-model="form.agreeTerms" class="agreement-checkbox">
        <span class="agreement-text">
          我已阅读并同意
          <el-link type="primary">用户协议</el-link>
          和
          <el-link type="primary">隐私政策</el-link>
        </span>
      </el-checkbox>
    </div>

    <div class="form-actions">
      <el-button
        type="primary"
        size="large"
        class="submit-btn"
        :loading="loading"
        :disabled="!form.agreeTerms"
        @click="handleSubmit"
      >
        <span v-if="!loading">立即注册</span>
        <span v-else>注册中...</span>
      </el-button>
    </div>
  </el-form>
</template>

<script>
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { User, Lock, CircleCheck, Phone, Message, Postcard } from '@element-plus/icons-vue';
import { register, checkUsername } from '../../api/userApi';
import { validatePhone, validateEmail, validateStudentId, validatePassword, nameRegex } from '../../utils/validators';
import DictSelect from '../Dict/DictSelect.vue';

export default {
  name: 'RegisterForm',
  components: { DictSelect },
  emits: ['register-success', 'to-login'],
  setup(props, { emit }) {
    const formRef = ref(null);
    const loading = ref(false);
    const checkingUsername = ref(false);
    const usernameCache = reactive({ value: '', exists: null });

    const form = reactive({
      username: '',
      password: '',
      confirmPassword: '',
      phone: '',
      email: '',
      name: '',
      studentId: '',
      role: 'STUDENT',
      agreeTerms: false
    });

    const validateUsernameAvailability = async (rule, value, callback) => {
      const username = (value || '').trim();
      if (!username) return callback();
      if (username.length < 3 || username.length > 20 || !/^[a-zA-Z0-9_]+$/.test(username)) {
        return callback();
      }

      if (usernameCache.value === username && usernameCache.exists !== null) {
        return usernameCache.exists ? callback(new Error('用户名已存在')) : callback();
      }

      checkingUsername.value = true;
      try {
        const res = await checkUsername(username);
        const exists = Boolean(res?.data?.exists);
        usernameCache.value = username;
        usernameCache.exists = exists;
        if (exists) {
          return callback(new Error('用户名已存在'));
        }
        return callback();
      } catch (error) {
        // 校验失败时不阻断注册，避免频繁弹窗
        return callback();
      } finally {
        checkingUsername.value = false;
      }
    };

    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '长度3-20个字符', trigger: 'blur' },
        { pattern: /^[a-zA-Z0-9_]+$/, message: '只能包含字母、数字和下划线', trigger: 'blur' },
        { validator: validateUsernameAvailability, trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (!validatePassword(value)) {
              callback(new Error('密码需8-20位，包含字母和数字'));
            } else {
              callback();
            }
          },
          trigger: 'blur'
        }
      ],
      confirmPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (value !== form.password) {
              callback(new Error('两次输入密码不一致'));
            } else {
              callback();
            }
          },
          trigger: 'blur'
        }
      ],
      phone: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (!validatePhone(value)) {
              callback(new Error('请输入正确的手机号'));
            } else {
              callback();
            }
          },
          trigger: 'blur'
        }
      ],
      email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (!validateEmail(value)) {
              callback(new Error('请输入正确的邮箱地址'));
            } else {
              callback();
            }
          },
          trigger: 'blur'
        }
      ],
      name: [
        { required: true, message: '请输入真实姓名', trigger: 'blur' },
        { pattern: nameRegex, message: '请输入正确的姓名（2-20位，支持中英文、点、空格）', trigger: 'blur' }
      ],
      studentId: [
        { required: true, message: '请输入学号/工号', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (!validateStudentId(value)) {
              callback(new Error('学号/工号格式不正确'));
            } else {
              callback();
            }
          },
          trigger: 'blur'
        }
      ],
      role: [
        { required: true, message: '请选择身份', trigger: 'change' }
      ]
    };

    const handleSubmit = async () => {
      if (!formRef.value) return;

      if (!form.agreeTerms) {
        ElMessage.warning('请先阅读并同意用户协议和隐私政策');
        return;
      }

      try {
        await formRef.value.validate();
        loading.value = true;
        await register({ ...form });
        emit('register-success');
      } catch (error) {
        if (error !== false) {
          ElMessage.error(error.message || '注册失败');
        }
      } finally {
        loading.value = false;
      }
    };

    return {
      formRef,
      form,
      rules,
      loading,
      checkingUsername,
      User,
      Lock,
      CircleCheck,
      Phone,
      Message,
      Postcard,
      handleSubmit
    };
  }
};
</script>

<style scoped>
.register-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.form-col,
.form-item {
  margin-bottom: 6px;
}

.custom-input :deep(.el-input__wrapper),
.custom-select :deep(.el-input__wrapper) {
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

.option-hint {
  display: block;
  font-size: 11px;
  color: var(--color-text-tertiary);
}

.agreement-row {
  margin-top: 4px;
}

.agreement-text {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.form-actions {
  margin-top: 8px;
}

.submit-btn {
  width: 100%;
  height: 46px;
  border-radius: 10px;
  font-weight: 600;
}

.register-form :deep(.el-form-item__label) {
  margin-bottom: 6px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.register-form :deep(.el-form-item__error) {
  position: static;
  margin-top: 6px;
  line-height: 1.35;
}

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }

  .form-col,
  .form-item {
    margin-bottom: 8px;
  }
}
</style>
