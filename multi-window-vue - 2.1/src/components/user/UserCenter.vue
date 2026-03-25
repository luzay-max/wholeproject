<template>
  <div class="user-center">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <span class="title-icon">👤</span>
          个人中心
        </h1>
        <p class="page-subtitle">管理您的个人信息和发布记录</p>
      </div>
    </div>

    <div class="content-container">
      <!-- 个人信息展示 -->
      <el-card class="info-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <div class="header-title">
              <h3 class="card-title">
                <el-icon><User /></el-icon>
                个人信息
              </h3>
              <DictTag :options="user_role" :value="userInfo.role" />
            </div>
            <el-button 
              type="primary" 
              size="small" 
              @click="isEditing = !isEditing"
              class="edit-btn"
            >
              <el-icon><component :is="isEditing ? 'Close' : 'Edit'" /></el-icon>
              {{ isEditing ? '取消编辑' : '编辑信息' }}
            </el-button>
          </div>
        </template>
        
        <div class="user-info">
          <!-- 头像 -->
          <div class="avatar-section">
            <div class="avatar-container">
              <div class="avatar-wrapper">
                <el-avatar :size="120" :src="userInfo.avatar || defaultAvatar" class="user-avatar" />
                <div class="avatar-overlay" v-if="isEditing">
                  <el-button
                    type="primary"
                    size="small"
                    class="change-avatar-btn"
                    @click="triggerAvatarUpload"
                  >
                    <el-icon><Camera /></el-icon>
                    更换头像
                  </el-button>
                </div>
              </div>
              <div class="avatar-info">
                <h3 class="username">{{ userInfo.username }}</h3>
                <p class="register-time">注册于 {{ formatDate(userInfo.createTime) }}</p>
              </div>
            </div>
            <input
              ref="avatarInput"
              type="file"
              accept="image/*"
              style="display: none"
              name="avatar"
              @change="handleAvatarChange"
            >
          </div>
          
          <!-- 基本信息 -->
          <div class="basic-info">
            <el-form :model="editForm" ref="formRef" :rules="rules" label-width="100px" v-if="isEditing">
              <div class="form-grid">
                <div class="form-item">
                  <label class="form-label">
                    <el-icon><User /></el-icon>
                    账户名称
                  </label>
                  <el-form-item prop="accountName" class="no-margin">
                    <el-input 
                      v-model="editForm.accountName" 
                      size="large" 
                      placeholder="请输入账户名称"
                      class="custom-input"
                      disabled
                    >
                      <template #prefix>
                        <el-icon><User /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </div>
                
                <div class="form-item">
                  <label class="form-label">
                    <el-icon><User /></el-icon>
                    真实姓名
                  </label>
                  <el-form-item prop="name" class="no-margin">
                    <el-input 
                      v-model="editForm.name" 
                      size="large" 
                      placeholder="请输入真实姓名"
                      class="custom-input"
                    >
                      <template #prefix>
                        <el-icon><User /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </div>
                
                <div class="form-item">
                  <label class="form-label">
                    <el-icon><Phone /></el-icon>
                    手机号码
                  </label>
                  <el-form-item prop="phone" class="no-margin">
                    <el-input 
                      v-model="editForm.phone" 
                      size="large" 
                      placeholder="请输入手机号"
                      class="custom-input"
                    >
                      <template #prefix>
                        <el-icon><Phone /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </div>
                
                <div class="form-item">
                  <label class="form-label">
                    <el-icon><Message /></el-icon>
                    邮箱地址
                  </label>
                  <el-form-item prop="email" class="no-margin">
                    <el-input 
                      v-model="editForm.email" 
                      size="large" 
                      placeholder="请输入邮箱"
                      class="custom-input"
                    >
                      <template #prefix>
                        <el-icon><Message /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </div>
              </div>
            </el-form>
            
            <!-- 非编辑模式 -->
            <div v-else class="info-grid">
              <div class="info-item">
                <div class="info-icon">
                  <el-icon><User /></el-icon>
                </div>
                <div class="info-content">
                  <label class="info-label">账户名称</label>
                  <p class="info-value">{{ userInfo.accountName || '未设置' }}</p>
                </div>
              </div>

              <div class="info-item">
                <div class="info-icon">
                  <el-icon><User /></el-icon>
                </div>
                <div class="info-content">
                  <label class="info-label">真实姓名</label>
                  <p class="info-value">{{ userInfo.name || '未设置' }}</p>
                </div>
              </div>
              
              <div class="info-item">
                <div class="info-icon">
                  <el-icon><Phone /></el-icon>
                </div>
                <div class="info-content">
                  <label class="info-label">手机号码</label>
                  <p class="info-value">{{ userInfo.phone || '未设置' }}</p>
                </div>
              </div>
              
              <div class="info-item">
                <div class="info-icon">
                  <el-icon><Message /></el-icon>
                </div>
                <div class="info-content">
                  <label class="info-label">邮箱地址</label>
                  <p class="info-value">{{ userInfo.email || '未设置' }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 保存按钮 -->
        <div v-if="isEditing" class="action-buttons">
          <el-button 
            type="primary" 
            @click="handleSave" 
            :loading="loading"
            class="save-btn"
          >
            <el-icon><Check /></el-icon>
            保存修改
          </el-button>
        </div>
      </el-card>
      
      <!-- 我的发布 -->
      <el-card class="publish-card" shadow="hover" v-if="!isEditing">
        <template #header>
          <div class="card-header">
            <div class="header-title">
              <h3 class="card-title">
                <el-icon><Document /></el-icon>
                我的发布
              </h3>
              <div class="publish-stats">
                <span class="stat-item">
                  <span class="stat-number">{{ myLostList.length }}</span>
                  <span class="stat-label">失物发布</span>
                </span>
                <span class="stat-item">
                  <span class="stat-number">{{ myFindList.length }}</span>
                  <span class="stat-label">招领发布</span>
                </span>
              </div>
            </div>
            <div class="tab-buttons">
              <el-button 
                :class="{ active: activeTab === 'lost' }" 
                @click="activeTab = 'lost'"
                class="tab-btn"
              >
                <el-icon><Search /></el-icon>
                失物发布
              </el-button>
              <el-button 
                :class="{ active: activeTab === 'find' }" 
                @click="activeTab = 'find'"
                class="tab-btn"
              >
                <el-icon><Bell /></el-icon>
                招领发布
              </el-button>
            </div>
          </div>
        </template>
        
        <!-- 失物发布列表 -->
        <div v-if="activeTab === 'lost'" class="publish-list">
          <div v-if="myLostList.length === 0" class="empty-list">
            <div class="empty-icon">
              <el-icon><Search /></el-icon>
            </div>
            <h4>暂无失物发布</h4>
            <p>您还没有发布过失物信息</p>
            <el-button type="primary" @click="navigateToPublish('lost')" class="publish-action">
              <el-icon><Plus /></el-icon>
              发布失物
            </el-button>
          </div>
          <div v-else>
            <el-table :data="myLostList" style="width: 100%" class="publish-table">
              <el-table-column prop="name" label="物品名称" width="200">
                <template #default="scope">
                  <div class="item-name">
                    <span class="name-text">{{ scope.row.name }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="location" label="丢失地点" width="180">
                <template #default="scope">
                  <div class="item-location">
                    <el-icon><Location /></el-icon>
                    <span>{{ scope.row.location }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="publishTime" label="发布时间" width="160">
                <template #default="scope">
                  <div class="item-time">
                    <el-icon><Clock /></el-icon>
                    <span>{{ formatDate(scope.row.publishTime) }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="120" align="center">
                <template #default="scope">
                  <DictTag :options="info_status" :value="scope.row.status" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" align="center">
                <template #default="scope">
                  <el-button
                    type="primary"
                    text
                    @click="viewDetail(scope.row.id, 'lost')"
                    size="small"
                    class="view-btn"
                  >
                    <el-icon><View /></el-icon>
                    查看
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
        
        <!-- 招领发布列表 -->
        <div v-if="activeTab === 'find'" class="publish-list">
          <div v-if="myFindList.length === 0" class="empty-list">
            <div class="empty-icon">
              <el-icon><Bell /></el-icon>
            </div>
            <h4>暂无招领发布</h4>
            <p>您还没有发布过招领信息</p>
            <el-button type="primary" @click="navigateToPublish('find')" class="publish-action">
              <el-icon><Plus /></el-icon>
              发布招领
            </el-button>
          </div>
          <div v-else>
            <el-table :data="myFindList" style="width: 100%" class="publish-table">
              <el-table-column prop="name" label="物品名称" width="200">
                <template #default="scope">
                  <div class="item-name">
                    <span class="name-text">{{ scope.row.name }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="location" label="拾取地点" width="180">
                <template #default="scope">
                  <div class="item-location">
                    <el-icon><Location /></el-icon>
                    <span>{{ scope.row.location }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="publishTime" label="发布时间" width="160">
                <template #default="scope">
                  <div class="item-time">
                    <el-icon><Clock /></el-icon>
                    <span>{{ formatDate(scope.row.publishTime) }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="120" align="center">
                <template #default="scope">
                  <DictTag :options="info_status" :value="scope.row.status" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" align="center">
                <template #default="scope">
                  <el-button
                    type="primary"
                    text
                    @click="viewDetail(scope.row.id, 'find')"
                    size="small"
                    class="view-btn"
                  >
                    <el-icon><View /></el-icon>
                    查看
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElForm } from 'element-plus';
import debounce from '../../utils/debounce';
import { 
  User, 
  Phone, 
  Message, 
  Document, 
  Search, 
  Bell, 
  Location, 
  Clock, 
  View, 
  Edit, 
  Close, 
  Camera, 
  Check,
  Plus
} from '@element-plus/icons-vue';
import { getUserInfo, updateUserInfo, updateAvatar } from '../../api/userApi';
import { getUserLostList } from '../../api/lostApi';
import { getUserFindList } from '../../api/findApi';
import { useUserStore } from '../../store/userStore';
import { useRouter } from 'vue-router';
import defaultAvatar from '../../assets/images/1.png';
import { nameRegex } from '../../utils/validators';
import { getDicts } from '../../api/system/dict/data';

export default {
  name: 'UserCenter',
  components: {
    User, Phone, Message, Document, Search, Bell, Location, Clock, View, 
    Edit, Close, Camera, Check, Plus
  },
  emits: ['view-detail'],
  setup(props, { emit }) {
    const userStore = useUserStore();
    const router = useRouter();
    const loading = ref(false);
    const isEditing = ref(false);
    const activeTab = ref('lost');
    const avatarInput = ref(null);
    const formRef = ref(null);
    
    // 用户信息
    const userInfo = ref({
      id: null,
      username: '',
      accountName: '',
      name: '',
      phone: '',
      email: '',
      avatar: '',
      role: '',
      createTime: null
    });
    
    // 编辑表单
    const editForm = reactive({
      accountName: '',
      name: '',
      phone: '',
      email: ''
    });
    
    // 我的发布列表
    const myLostList = ref([]);
    const myFindList = ref([]);
    
    const user_role = ref([]);
    const info_status = ref([]);

    // 格式化日期
    const formatDate = (timestamp) => {
      if (!timestamp) return '';
      const date = new Date(timestamp);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    };
    
    // 加载用户信息
    const loadUserInfo = async () => {
      try {
        loading.value = true;
        const res = await getUserInfo();
        
        // 添加响应数据检查
        if (!res || res.code !== 0) {
          throw new Error(res?.message || '获取用户信息失败');
        }
        
        userInfo.value = res.data || {};
        
        // 同步到store - 注意：userStore没有setUserInfo方法，使用直接赋值方式更新userInfo
        Object.assign(userStore.userInfo, userInfo.value);
        // 同时更新本地存储
        import('../../utils/authUtil').then(({ setUserInfo }) => {
          setUserInfo(userInfo.value);
        });
        
        // 初始化编辑表单
        Object.assign(editForm, {
          accountName: userInfo.value.accountName || '',
          name: userInfo.value.name || '',
          phone: userInfo.value.phone || '',
          email: userInfo.value.email || ''
        });
      } catch (error) {
        console.error('加载用户信息失败:', error);
        ElMessage.error('加载用户信息失败');
      } finally {
        loading.value = false;
      }
    };
    
    // 加载我的发布
    const loadMyPublishesImmediate = async () => {
      try {
          // ?????? - ??????
          const lostRes = await getUserLostList({ page: 1, pageSize: 10 });
          myLostList.value = lostRes.data?.list || [];
          
          // ?????? - ??????
          const findRes = await getUserFindList({ page: 1, pageSize: 10 });
          myFindList.value = findRes.data?.list || [];
      } catch (error) {
        console.error('????????:', error);
        ElMessage.error('????????');
      }
    };
    const loadMyPublishes = debounce(loadMyPublishesImmediate, 400);
    
    // 触发头像上传
    const triggerAvatarUpload = () => {
      if (avatarInput.value) {
        avatarInput.value.click();
      }
    };
    
    // 处理头像变化
    const handleAvatarChange = async (event) => {
      const file = event.target.files[0];
      if (!file) return;
      
      try {
        loading.value = true;
         const formData = new FormData();
    formData.append('avatar', file);

    console.log('FormData entries:');
    for (const [key, value] of formData.entries()) {
      console.log(key, value);
    }
        
        // 调用API更新头像
        const res = await updateAvatar(file);
        const avatarUrl = res.data;
        
        // 更新本地用户信息
        userInfo.value.avatar = avatarUrl;
        userStore.updateAvatar(avatarUrl);
        
        ElMessage.success('头像更新成功');
        
        // 清空input
        event.target.value = '';
      } catch (error) {
        ElMessage.error('头像更新失败');
      } finally {
        loading.value = false;
      }
    };
    
    // 表单校验规则
    const rules = reactive({
      name: [
        { required: true, message: '请输入真实姓名', trigger: 'blur' },
        { pattern: nameRegex, message: '请输入正确的姓名（2-20位，支持中英文、点、空格）', trigger: 'blur' }
      ],
      phone: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码格式', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { 
          pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, 
          message: '请输入正确的邮箱格式', 
          trigger: 'blur' 
        }
      ]
    });
    
    // 保存修改
    const handleSave = async () => {
      if (!formRef.value) return;
      
      try {
        // 进行表单验证
        await formRef.value.validate();
        
        loading.value = true;
        
        // 调用API更新用户信息
        await updateUserInfo(editForm);
        
        // 更新本地用户信息
        Object.assign(userInfo.value, editForm);
        userStore.updateUserInfo(editForm);
        
        ElMessage.success('信息更新成功');
        isEditing.value = false;
      } catch (error) {
        if (error !== false) { // 表单验证失败时，error为false
          ElMessage.error('信息更新失败');
        }
        return;
      } finally {
        loading.value = false;
      }
    };
    
    // 查看详情
    const viewDetail = (id, type) => {
      emit('view-detail', { id, type });
    };

    // 导航到发布页面
    const navigateToPublish = (type) => {
      const routeName = type === 'lost' ? 'LostPublish' : 'FindPublish';
      router.push({ name: routeName });
    };
    
    // 组件挂载时加载数据
    onMounted(() => {
      loadUserInfo();
      loadMyPublishesImmediate();
      getDicts('sys_user_role').then(res => user_role.value = res.data);
      getDicts('info_status').then(res => info_status.value = res.data);
    });
    
    return {
      loading,
      isEditing,
      activeTab,
      avatarInput,
      formRef,
      userInfo,
      editForm,
      rules,
      myLostList,
      myFindList,
      defaultAvatar,
      user_role,
      info_status,
      formatDate,
      triggerAvatarUpload,
      handleAvatarChange,
      handleSave,
      viewDetail,
      navigateToPublish
    };
  }
};
</script>

<style scoped>
.user-center {
  min-height: calc(100vh - 120px);
  background: transparent;
  padding: 20px 0 40px;
}

.content-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 页面标题 */
.page-header {
  margin-bottom: 30px;
  text-align: center;
}

.header-content {
  padding: 30px 0;
}

.page-title {
  margin: 0 0 12px 0;
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.title-icon {
  font-size: 36px;
}

.page-subtitle {
  margin: 0;
  font-size: 16px;
  color: #909399;
  font-weight: 500;
}

/* 卡片样式 */
.info-card,
.publish-card {
  border-radius: var(--app-radius-lg);
  border: 1px solid var(--border-light);
  overflow: hidden;
  box-shadow: var(--app-shadow-sm);
  margin-bottom: 24px;
  background: var(--app-surface);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
}

.card-header {
  padding: 0 5px 15px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.edit-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  border-radius: 8px;
}

/* 用户信息布局 */
.user-info {
  display: flex;
  flex-wrap: wrap;
  gap: 40px;
  padding: 10px 5px;
}

.avatar-section {
  flex-shrink: 0;
}

.avatar-container {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-wrapper {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.user-avatar {
  width: 100%;
  height: 100%;
  transition: all 0.3s ease;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.change-avatar-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  border-radius: 20px;
}

.avatar-info {
  text-align: left;
}

.username {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.register-time {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

/* 基本信息 */
.basic-info {
  flex: 1;
  min-width: 300px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.no-margin {
  margin-bottom: 0;
}

.custom-input :deep(.el-input__inner) {
  border-radius: 10px;
  border: 1px solid #e4e7ed;
  padding: 12px 16px;
  font-size: 14px;
  transition: all 0.3s ease;
  height: 48px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
}

.custom-input :deep(.el-input__inner:focus) {
  border-color: #4f6df5;
  box-shadow: 0 4px 12px rgba(79, 109, 245, 0.15);
}

.custom-input :deep(.el-input__prefix) {
  display: flex;
  align-items: center;
  color: #909399;
}

/* 信息网格 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.52);
  border-radius: 12px;
  transition: all 0.3s ease;
}

.info-item:hover {
  background: rgba(79, 109, 245, 0.06);
  transform: translateY(-2px);
}

.info-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4f6df5, #3a56d4);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.info-icon .el-icon {
  color: white;
  font-size: 20px;
}

.info-content {
  flex: 1;
}

.info-label {
  display: block;
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.info-value {
  margin: 0;
  font-size: 16px;
  color: #303133;
  font-weight: 600;
}

/* 操作按钮 */
.action-buttons {
  margin-top: 30px;
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.save-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 32px;
  border-radius: 10px;
  font-weight: 600;
  background: linear-gradient(135deg, #4f6df5, #3a56d4);
  border: none;
}

.save-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(79, 109, 245, 0.3);
}

/* 发布卡片样式 */
.publish-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.52);
  border-radius: 8px;
}

.stat-number {
  font-size: 20px;
  font-weight: 700;
  color: #4f6df5;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.tab-buttons {
  display: flex;
  gap: 8px;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  background: rgba(255, 255, 255, 0.86);
  color: #606266;
  transition: all 0.3s ease;
}

.tab-btn.active {
  background: #4f6df5;
  color: white;
  border-color: #4f6df5;
}

/* 发布列表 */
.publish-list {
  margin-top: 20px;
}

.empty-list {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}

.empty-icon {
  font-size: 48px;
  color: #dcdfe6;
  margin-bottom: 16px;
}

.empty-list h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
}

.empty-list p {
  margin: 0 0 20px 0;
  font-size: 14px;
}

.publish-action {
  display: flex;
  align-items: center;
  gap: 6px;
  border-radius: 8px;
}

.publish-table :deep(.el-table__header) th {
  background-color: #f8f9fa;
  color: #303133;
  font-weight: 600;
}

.publish-table :deep(.el-table__row) {
  transition: background-color 0.2s;
}

.publish-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

.item-name,
.item-location,
.item-time {
  display: flex;
  align-items: center;
  gap: 6px;
}

.name-text {
  font-weight: 500;
  color: #303133;
}

.status-tag {
  font-weight: 500;
  border-radius: 4px;
}

.view-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .content-container {
    padding: 0 15px;
  }
  
  .page-title {
    font-size: 26px;
  }
  
  .user-info {
    flex-direction: column;
    gap: 20px;
    align-items: center;
  }
  
  .basic-info {
    min-width: 100%;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .header-title {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .avatar-container {
    flex-direction: column;
    text-align: center;
  }
  
  .form-grid,
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .publish-stats {
    gap: 10px;
  }
  
  .tab-buttons {
    width: 100%;
  }
  
  .tab-btn {
    flex: 1;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .user-center {
    padding: 15px 0 30px;
  }
  
  .page-title {
    font-size: 22px;
  }
  
  .page-subtitle {
    font-size: 14px;
  }
  
  .card-title {
    font-size: 18px;
  }
  
  .username {
    font-size: 20px;
  }
  
  .info-item {
    padding: 16px;
  }
  
  .form-item {
    gap: 6px;
  }
  
  .custom-input :deep(.el-input__inner) {
    height: 44px;
    padding: 10px 14px;
  }
}
</style>
