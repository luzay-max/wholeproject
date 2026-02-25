<template>
  <div class="user-page">
    <!-- 左侧用户信息卡片 -->
    <div class="user-sidebar">
      <div class="user-card">
        <div class="card-header-bg"></div>
        <div class="avatar-section">
          <div class="avatar-wrapper" @click="triggerAvatarUpload">
            <el-avatar :size="100" :src="userInfo.avatar || defaultAvatar" class="user-avatar" />
            <div class="avatar-ring"></div>
            <div class="avatar-overlay" v-if="isEditing">
              <el-icon><Camera /></el-icon>
            </div>
          </div>
          <div class="avatar-badge" v-if="isVip">
            <el-icon><Medal /></el-icon>
          </div>
        </div>
        <div class="user-info">
          <h2 class="username">{{ userInfo.username || userInfo.accountName || '未设置' }}</h2>
          <DictTag :options="user_role" :value="userInfo.role" class="role-tag" />
          <p class="register-time">{{ formatDate(userInfo.createTime) }}</p>
        </div>
        <div class="user-stats">
          <div class="stat-card" @click="activeTab = 'publishes'">
            <div class="stat-icon lost"><el-icon><Search /></el-icon></div>
            <div class="stat-content">
              <span class="stat-number">{{ lostTotal }}</span>
              <span class="stat-label">失物发布</span>
            </div>
          </div>
          <div class="stat-card" @click="activeTab = 'publishes'">
            <div class="stat-icon find"><el-icon><Bell /></el-icon></div>
            <div class="stat-content">
              <span class="stat-number">{{ findTotal }}</span>
              <span class="stat-label">招领发布</span>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon solved"><el-icon><CircleCheck /></el-icon></div>
            <div class="stat-content">
              <span class="stat-number">{{ solvedCount }}</span>
              <span class="stat-label">成功解决</span>
            </div>
          </div>
        </div>
        <div class="progress-section">
          <div class="progress-header">
            <span>活跃度</span>
            <span class="progress-value">{{ activityLevel }}%</span>
          </div>
          <el-progress :percentage="activityLevel" :stroke-width="8" :color="activityColor" :show-text="false" />
        </div>
        <input ref="avatarInput" type="file" accept="image/*" style="display:none" @change="handleAvatarChange">
      </div>
    </div>
    
    <!-- 右侧内容区 -->
    <div class="user-content">
      <div class="content-header">
        <div class="header-tabs">
          <div class="header-tab" :class="{ active: activeTab === 'profile' }" @click="activeTab = 'profile'">
            <el-icon><User /></el-icon><span>个人信息</span>
          </div>
          <div class="header-tab" :class="{ active: activeTab === 'publishes' }" @click="activeTab = 'publishes'">
            <el-icon><Document /></el-icon><span>我的发布</span>
            <el-badge :value="publishTotal" class="tab-badge" type="primary" />
          </div>
          <div class="header-tab" :class="{ active: activeTab === 'settings' }" @click="activeTab = 'settings'">
            <el-icon><Setting /></el-icon><span>用户设置</span>
          </div>
        </div>
      </div>
      
      <div class="content-body">
        <!-- 个人信息 -->
        <div v-if="activeTab === 'profile'" class="tab-panel">
          <div class="panel-header">
            <h3><el-icon><UserFilled /></el-icon>基本信息</h3>
            <el-button type="primary" size="large" @click="isEditing = !isEditing">
              <el-icon><component :is="isEditing ? 'Close' : 'Edit'" /></el-icon>
              {{ isEditing ? '取消' : '编辑资料' }}
            </el-button>
          </div>
          
          <el-form v-if="isEditing" :model="editForm" ref="formRef" :rules="rules" label-position="top" class="edit-form" v-focus-next="handleSave">
            <div class="form-grid">
              <el-form-item label="登录账号" prop="accountName">
                <el-input v-model="editForm.accountName" placeholder="登录账号不可修改" disabled />
              </el-form-item>
              <el-form-item label="昵称" prop="username">
                <el-input v-model="editForm.username" placeholder="请输入昵称" />
              </el-form-item>
              <el-form-item label="真实姓名" prop="name">
                <el-input v-model="editForm.name" placeholder="真实姓名不可修改" disabled />
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="editForm.phone" placeholder="请输入手机号" />
              </el-form-item>
              <el-form-item label="邮箱地址" prop="email">
                <el-input v-model="editForm.email" placeholder="请输入邮箱地址" />
              </el-form-item>
              <el-form-item label="所在院系" prop="college">
                <el-input v-model="editForm.college" placeholder="所在院系不可修改" disabled />
              </el-form-item>
            </div>
            <el-button type="primary" size="large" @click="handleSave" :loading="loading" class="save-btn">
              <el-icon><Check /></el-icon>保存修改
            </el-button>
          </el-form>
          
          <div v-else class="info-display">
            <div class="info-card">
              <div class="info-header"><el-icon><Postcard /></el-icon>联系信息</div>
              <div class="info-grid">
                <div class="info-item">
                  <div class="info-icon name"><el-icon><User /></el-icon></div>
                  <div class="info-content"><label>登录账号</label><span>{{ userInfo.accountName || '未设置' }}</span></div>
                </div>
                <div class="info-item">
                  <div class="info-icon name"><el-icon><User /></el-icon></div>
                  <div class="info-content"><label>昵称</label><span>{{ userInfo.username || '未设置' }}</span></div>
                </div>
                <div class="info-item">
                  <div class="info-icon name"><el-icon><User /></el-icon></div>
                  <div class="info-content"><label>真实姓名</label><span>{{ userInfo.name || '未设置' }}</span></div>
                </div>
                <div class="info-item">
                  <div class="info-icon phone"><el-icon><Phone /></el-icon></div>
                  <div class="info-content"><label>手机号</label><span>{{ userInfo.phone || '未设置' }}</span></div>
                </div>
                <div class="info-item">
                  <div class="info-icon email"><el-icon><Message /></el-icon></div>
                  <div class="info-content"><label>邮箱地址</label><span>{{ userInfo.email || '未设置' }}</span></div>
                </div>
                <div class="info-item">
                  <div class="info-icon dept"><el-icon><OfficeBuilding /></el-icon></div>
                  <div class="info-content"><label>所在院系</label><span>{{ userInfo.college || '未设置' }}</span></div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 我的发布 -->
        <div v-if="activeTab === 'publishes'" class="tab-panel">
          <div class="panel-header">
            <h3><el-icon><DocumentCopy /></el-icon>我的发布</h3>
          </div>
          <div class="sub-tabs">
            <div class="sub-tab" :class="{ active: publishTab === 'lost' }" @click="publishTab = 'lost'">
              <el-icon><Search /></el-icon><span>失物</span><el-tag size="small" type="warning">{{ lostTotal }}</el-tag>
            </div>
            <div class="sub-tab" :class="{ active: publishTab === 'find' }" @click="publishTab = 'find'">
              <el-icon><Bell /></el-icon><span>招领</span><el-tag size="small" type="success">{{ findTotal }}</el-tag>
            </div>
          </div>
          
          <div v-if="publishTab === 'lost'" class="publish-list">
            <div v-if="lostTotal === 0" class="empty-state">
              <el-icon size="60"><Search /></el-icon>
              <p>暂无失物发布</p>
              <el-button type="primary" @click="navigateToPublish('lost')"><el-icon><Plus /></el-icon>发布失物</el-button>
            </div>
            <div v-else>
              <div class="publish-grid">
                <div v-for="item in myLostList" :key="item.id" class="publish-card" @click="viewDetail(item.id, 'lost')">
                  <div class="card-image">
                    <el-image v-if="getItemImage(item)" :src="getItemImage(item)" fit="cover">
                      <template #error>
                        <div class="no-image">暂无图片</div>
                      </template>
                    </el-image>
                    <div v-else class="no-image">暂无图片</div>
                    <DictTag :options="info_status" :value="item.status" class="status-tag" />
                  </div>
                  <div class="card-content">
                    <h4>{{ item.name }}</h4>
                    <p><el-icon><Location /></el-icon>{{ item.location }}</p>
                    <p><el-icon><Clock /></el-icon>{{ formatDate(item.publishTime) }}</p>
                  </div>
                </div>
              </div>
              <div class="publish-pagination">
                <el-pagination
                  v-model:current-page="lostPagination.page"
                  v-model:page-size="lostPagination.pageSize"
                  :page-sizes="[8, 16, 24, 40]"
                  :total="lostTotal"
                  layout="total, sizes, prev, pager, next, jumper"
                  background
                  @current-change="handleLostPageChange"
                  @size-change="handleLostPageSizeChange"
                />
              </div>
            </div>
          </div>
          
          <div v-if="publishTab === 'find'" class="publish-list">
            <div v-if="findTotal === 0" class="empty-state">
              <el-icon size="60"><Bell /></el-icon>
              <p>暂无招领发布</p>
              <el-button type="primary" @click="navigateToPublish('find')"><el-icon><Plus /></el-icon>发布招领</el-button>
            </div>
            <div v-else>
              <div class="publish-grid">
                <div v-for="item in myFindList" :key="item.id" class="publish-card" @click="viewDetail(item.id, 'find')">
                  <div class="card-image">
                    <el-image v-if="getItemImage(item)" :src="getItemImage(item)" fit="cover">
                      <template #error>
                        <div class="no-image">暂无图片</div>
                      </template>
                    </el-image>
                    <div v-else class="no-image">暂无图片</div>
                    <DictTag :options="info_status" :value="item.status" class="status-tag" />
                  </div>
                  <div class="card-content">
                    <h4>{{ item.name }}</h4>
                    <p><el-icon><Location /></el-icon>{{ item.location }}</p>
                    <p><el-icon><Clock /></el-icon>{{ formatDate(item.publishTime) }}</p>
                  </div>
                </div>
              </div>
              <div class="publish-pagination">
                <el-pagination
                  v-model:current-page="findPagination.page"
                  v-model:page-size="findPagination.pageSize"
                  :page-sizes="[8, 16, 24, 40]"
                  :total="findTotal"
                  layout="total, sizes, prev, pager, next, jumper"
                  background
                  @current-change="handleFindPageChange"
                  @size-change="handleFindPageSizeChange"
                />
              </div>
            </div>
          </div>
        </div>
        
        <!-- 用户设置 -->
        <div v-if="activeTab === 'settings'" class="tab-panel">
          <div class="panel-header">
            <h3><el-icon><Setting /></el-icon>用户设置</h3>
          </div>
          <div class="settings-list">
            <div class="settings-item">
              <div class="settings-info"><el-icon><Lock /></el-icon><div><h4>修改密码</h4><p>定期更换密码保护用户安全</p></div></div>
              <el-button type="primary" plain size="small" @click="openPasswordDialog">修改</el-button>
            </div>
            <div class="settings-item">
              <div class="settings-info"><el-icon><Delete /></el-icon><div><h4>注销账户</h4><p>删除账户后将无法恢复</p></div></div>
              <el-button type="danger" plain size="small">注销</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="400px"
      destroy-on-close
      append-to-body
    >
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="80px" v-focus-next="handleChangePassword">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="8-20位字母和数字" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="loading" @click="handleChangePassword">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { User, Phone, Message, Document, Search, Bell, Location, Clock, Edit, Close, Camera, Check, Plus, ArrowRight, CircleCheck, Medal, Star, Calendar, Setting, UserFilled, Postcard, OfficeBuilding, DocumentCopy, Picture, View, Lock, Delete } from '@element-plus/icons-vue';
import { getUserInfo, updateUserInfo, updateAvatar } from '../api/userApi';
import { resetPassword } from '../api/authApi';
import { getUserLostList } from '../api/lostApi';
import { getUserFindList } from '../api/findApi';
import { useUserStore } from '../store/userStore';
import { getDicts } from '../api/system/dict/data';
import { validatePassword } from '../utils/validators';
import defaultAvatar from '../assets/images/1.png';
import DictTag from '../components/Dict/DictTag.vue';

export default {
  name: 'UserCenterPage',
  components: { User, Phone, Message, Document, Search, Bell, Location, Clock, Edit, Close, Camera, Check, Plus, ArrowRight, CircleCheck, Calendar, Setting, UserFilled, Postcard, OfficeBuilding, DocumentCopy, Picture, View, Lock, Delete, DictTag },
  setup() {
    const router = useRouter();
    const userStore = useUserStore();
    const loading = ref(false);
    const isEditing = ref(false);
    const activeTab = ref('profile');
    const publishTab = ref('lost');
    const avatarInput = ref(null);
    const formRef = ref(null);
    
    const userInfo = ref({ id: null, username: '', accountName: '', name: '', phone: '', email: '', avatar: '', role: '', createTime: null, college: '' });
    const editForm = reactive({ accountName: '', username: '', name: '', phone: '', email: '', college: '' });
    // 修改密码相关
    const passwordDialogVisible = ref(false);
    const passwordFormRef = ref(null);
    const passwordForm = reactive({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    });
    const myLostList = ref([]);
    const myFindList = ref([]);
    const lostPagination = reactive({ page: 1, pageSize: 8, total: 0 });
    const findPagination = reactive({ page: 1, pageSize: 8, total: 0 });
    
    const user_role = ref([]);
    const info_status = ref([]);
    const lostTotal = computed(() => Number(lostPagination.total) || 0);
    const findTotal = computed(() => Number(findPagination.total) || 0);
    const publishTotal = computed(() => lostTotal.value + findTotal.value);

    const solvedCount = computed(() => {
      return myLostList.value.filter(i => i.status === 'SOLVED').length + myFindList.value.filter(i => i.status === 'SOLVED').length;
    });
    const activityLevel = computed(() => {
      const total = publishTotal.value;
      if (total === 0) return 20;
      if (total <= 3) return 40;
      if (total <= 6) return 60;
      if (total <= 10) return 80;
      return 100;
    });
    const activityColor = computed(() => {
      if (activityLevel.value <= 40) return '#e6a23c';
      if (activityLevel.value <= 70) return '#409eff';
      return '#67c23a';
    });
    const isVip = computed(() => userInfo.value.role === 'ADMIN');
    
    const rules = reactive({
      username: [
        { required: true, message: '请输入昵称', trigger: 'blur' },
        { min: 3, max: 20, message: '昵称长度应在 3 到 20 个字符之间', trigger: 'blur' },
        { pattern: /^[a-zA-Z0-9_]+$/, message: '昵称仅支持字母、数字和下划线', trigger: 'blur' }
      ],
      name: [
        { required: true, message: '请输入真实姓名', trigger: 'blur' },
        { min: 2, max: 20, message: '姓名长度应在 2 到 20 个字符之间', trigger: 'blur' }
      ],
      phone: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: '请输入正确的邮箱格式', trigger: 'blur' }
      ]
    });

    const passwordRules = reactive({
      oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
      newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
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
        { required: true, message: '请再次输入新密码', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (value !== passwordForm.newPassword) {
              callback(new Error('两次输入密码不一致'));
            } else {
              callback();
            }
          },
          trigger: 'blur'
        }
      ]
    });
    
    const formatDate = (ts) => {
      if (!ts) return '';
      const d = new Date(ts);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    };
    
    const loadUserInfo = async () => {
      try {
        loading.value = true;
        const res = await getUserInfo();
        if (res?.code === 0) {
          userInfo.value = res.data || {};
          Object.assign(editForm, {
            accountName: userInfo.value.accountName || '',
            username: userInfo.value.username || '',
            name: userInfo.value.name || '',
            phone: userInfo.value.phone || '',
            email: userInfo.value.email || '',
            college: userInfo.value.college || ''
          });
        }
      } catch (error) { console.error('加载用户信息失败:', error); }
      finally { loading.value = false; }
    };
    
    const fetchLostPublishes = async () => {
      try {
        const res = await getUserLostList({
          page: lostPagination.page,
          pageSize: lostPagination.pageSize
        });
        const data = res?.data || {};
        myLostList.value = Array.isArray(data.list) ? data.list : [];
        lostPagination.total = Number(data.total) || 0;
      } catch (error) {
        console.error('加载失物发布列表失败:', error);
      }
    };

    const fetchFindPublishes = async () => {
      try {
        const res = await getUserFindList({
          page: findPagination.page,
          pageSize: findPagination.pageSize
        });
        const data = res?.data || {};
        myFindList.value = Array.isArray(data.list) ? data.list : [];
        findPagination.total = Number(data.total) || 0;
      } catch (error) {
        console.error('加载招领发布列表失败:', error);
      }
    };

    const loadPublishes = async () => {
      try {
        await Promise.all([fetchLostPublishes(), fetchFindPublishes()]);
      } catch (error) {
        console.error('加载发布列表失败:', error);
      }
    };

    const handleLostPageChange = (page) => {
      lostPagination.page = page;
      fetchLostPublishes();
    };

    const handleLostPageSizeChange = (pageSize) => {
      lostPagination.page = 1;
      lostPagination.pageSize = pageSize;
      fetchLostPublishes();
    };

    const handleFindPageChange = (page) => {
      findPagination.page = page;
      fetchFindPublishes();
    };

    const handleFindPageSizeChange = (pageSize) => {
      findPagination.page = 1;
      findPagination.pageSize = pageSize;
      fetchFindPublishes();
    };
    
    const triggerAvatarUpload = () => avatarInput.value?.click();
    const handleAvatarChange = async (event) => {
      const file = event.target.files[0];
      if (!file) return;
      try {
        loading.value = true;
        const res = await updateAvatar(file);
        userInfo.value.avatar = res.data;
        ElMessage.success('头像更新成功');
      } catch { ElMessage.error('头像更新失败'); }
      finally { loading.value = false; }
    };
    
    const handleSave = async () => {
      if (!formRef.value) return;
      try {
        await formRef.value.validate();
        loading.value = true;
        const payload = {
          username: editForm.username,
          phone: editForm.phone,
          email: editForm.email
        };
        const res = await updateUserInfo(payload);
        if (res?.code === 0 && res?.data) {
          userInfo.value = { ...userInfo.value, ...res.data };
        } else {
          Object.assign(userInfo.value, payload);
        }
        Object.assign(editForm, {
          accountName: userInfo.value.accountName || '',
          username: userInfo.value.username || '',
          name: userInfo.value.name || '',
          phone: userInfo.value.phone || '',
          email: userInfo.value.email || '',
          college: userInfo.value.college || ''
        });
        ElMessage.success('信息更新成功');
        isEditing.value = false;
      } catch { ElMessage.error('信息更新失败'); }
      finally { loading.value = false; }
    };

    const openPasswordDialog = () => {
      passwordForm.oldPassword = '';
      passwordForm.newPassword = '';
      passwordForm.confirmPassword = '';
      passwordDialogVisible.value = true;
    };

    const handleChangePassword = async () => {
      if (!passwordFormRef.value) return;
      try {
        await passwordFormRef.value.validate();
        loading.value = true;
        await resetPassword({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        });
        ElMessage.success('密码修改成功，请重新登录');
        passwordDialogVisible.value = false;
      } catch (error) {
        ElMessage.error(error.message || '密码修改失败');
      } finally {
        loading.value = false;
      }
    };
    
    const normalizeImages = (images) => {
      if (!images) return [];
      if (Array.isArray(images)) return images;
      if (typeof images === 'string') {
        try {
          const parsed = JSON.parse(images);
          return Array.isArray(parsed) ? parsed : [];
        } catch {
          return [];
        }
      }
      return [];
    };

    const getItemImage = (item) => {
      const images = normalizeImages(item?.images);
      return images.length ? images[0] : '';
    };

    const viewDetail = (id, type) => router.push({ name: 'InfoDetailPage', params: { id, type } });
    const navigateToPublish = (type) => router.push({ name: type === 'lost' ? 'LostPublish' : 'FindPublish' });
    
    onMounted(() => {
      loadUserInfo();
      loadPublishes();
      getDicts('sys_user_role').then(res => user_role.value = res.data);
      getDicts('info_status').then(res => info_status.value = res.data);
    });

    watch(publishTab, (tab) => {
      if (tab === 'lost') {
        fetchLostPublishes();
      } else {
        fetchFindPublishes();
      }
    });

    return {
      loading,
      isEditing,
      activeTab,
      publishTab,
      avatarInput,
      formRef,
      userInfo,
      editForm,
      passwordDialogVisible,
      passwordFormRef,
      passwordForm,
      myLostList,
      myFindList,
      lostPagination,
      findPagination,
      lostTotal,
      findTotal,
      publishTotal,
      solvedCount,
      activityLevel,
      activityColor,
      isVip,
      rules,
      passwordRules,
      user_role,
      info_status,
      formatDate,
      loadUserInfo,
      loadPublishes,
      fetchLostPublishes,
      fetchFindPublishes,
      handleLostPageChange,
      handleLostPageSizeChange,
      handleFindPageChange,
      handleFindPageSizeChange,
      triggerAvatarUpload,
      handleAvatarChange,
      handleSave,
      openPasswordDialog,
      handleChangePassword,
      getItemImage,
      viewDetail,
      navigateToPublish,
      defaultAvatar
    };
  }
};
</script>

<style scoped>
.user-page {
  --page-accent: #2563eb;
  --page-accent-soft: rgba(37, 99, 235, 0.12);
  height: 100%;
  min-height: 0;
  display: flex;
  gap: 16px;
  padding: 16px;
  background: radial-gradient(900px 360px at 10% -10%, rgba(37, 99, 235, 0.10), transparent 60%),
    linear-gradient(180deg, #f8fafc 0%, #ffffff 70%);
  overflow: hidden;
  box-sizing: border-box;
}

.user-sidebar {
  width: 320px;
  flex-shrink: 0;
  min-height: 0;
}

.user-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 20px;
  border: 1px solid var(--color-border-secondary);
  box-shadow: var(--shadow-1);
  position: relative;
  overflow: hidden;
}

.card-header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 80px;
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.16) 0%, rgba(37, 99, 235, 0.02) 100%);
}

.avatar-section {
  position: relative;
  display: flex;
  justify-content: center;
  margin-top: 18px;
  margin-bottom: 12px;
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
}

.avatar-wrapper:hover {
  transform: translateY(-2px);
}

.user-avatar {
  border: 3px solid #ffffff;
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.18);
}

.avatar-ring {
  position: absolute;
  inset: -6px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.3), rgba(15, 118, 110, 0.18));
  z-index: 0;
  opacity: 0.5;
  filter: blur(6px);
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay .el-icon {
  font-size: 22px;
  color: #ffffff;
}

.avatar-badge {
  position: absolute;
  bottom: 0;
  right: calc(50% - 55px);
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 12px;
  border: 3px solid #ffffff;
}

.user-info {
  text-align: center;
  margin-bottom: 16px;
}

.username {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.role-tag {
  font-weight: 600;
}

.register-time {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.user-stats {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid var(--color-border-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
}

.stat-card:hover {
  background: #ffffff;
  box-shadow: 0 8px 16px rgba(15, 23, 42, 0.06);
}

.stat-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #ffffff;
}

.stat-icon.lost { background: #2563eb; }
.stat-icon.find { background: #0f766e; }
.stat-icon.solved { background: #f59e0b; }

.stat-content { flex: 1; }

.stat-number {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.stat-label {
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.progress-section {
  padding: 14px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid var(--color-border-secondary);
}

.progress-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 12px;
  color: var(--color-text-secondary);
}

.progress-value {
  font-weight: 600;
  color: var(--page-accent);
}

.user-content {
  flex: 1;
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.content-header {
  background: #ffffff;
  border-radius: 14px;
  padding: 6px;
  margin-bottom: 12px;
  box-shadow: var(--shadow-1);
  border: 1px solid var(--color-border-secondary);
}

.header-tabs {
  display: flex;
  gap: 6px;
}

.header-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border-radius: 999px;
  cursor: pointer;
  font-weight: 500;
  color: var(--color-text-secondary);
  transition: all 0.2s ease;
  position: relative;
  background: transparent;
}

.header-tab:hover {
  background: #f1f5ff;
  color: var(--page-accent);
}

.header-tab.active {
  background: var(--page-accent);
  color: #ffffff;
}

.tab-badge {
  position: absolute;
  top: 2px;
  right: 2px;
  transform: scale(0.85);
}

.content-body {
  flex: 1;
  min-height: 0;
  background: #ffffff;
  border-radius: 16px;
  padding: 18px;
  box-shadow: var(--shadow-1);
  border: 1px solid var(--color-border-secondary);
  overflow: auto;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--color-border-secondary);
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.panel-header h3 .el-icon {
  color: var(--page-accent);
}

.edit-form {
  max-width: 800px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 18px;
}

.save-btn {
  padding: 10px 36px;
  border-radius: 10px;
  font-weight: 600;
}

.info-display {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-card {
  background: #f8fafc;
  border-radius: 14px;
  padding: 16px;
  border: 1px solid var(--color-border-secondary);
}

.info-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.info-header .el-icon {
  color: var(--page-accent);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid var(--color-border-secondary);
  transition: all 0.2s ease;
}

.info-item:hover {
  box-shadow: 0 6px 12px rgba(15, 23, 42, 0.06);
}

.info-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #ffffff;
}

.info-icon.name { background: #2563eb; }
.info-icon.phone { background: #f59e0b; }
.info-icon.email { background: #0f766e; }
.info-icon.dept { background: #6d28d9; }

.info-content label {
  display: block;
  font-size: 12px;
  color: var(--color-text-tertiary);
  margin-bottom: 4px;
}

.info-content span {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
}

.sub-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.sub-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #f8fafc;
  border-radius: 999px;
  cursor: pointer;
  font-weight: 500;
  color: var(--color-text-secondary);
  transition: all 0.2s ease;
  border: 1px solid var(--color-border-secondary);
}

.sub-tab:hover {
  background: #ffffff;
}

.sub-tab.active {
  background: var(--page-accent);
  color: #ffffff;
  border-color: var(--page-accent);
}

.publish-list {
  min-height: 260px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 50px 20px;
  color: var(--color-text-tertiary);
}

.empty-state .el-icon {
  color: #cbd5f5;
  margin-bottom: 12px;
}

.publish-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.publish-pagination {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--color-border-secondary);
  display: flex;
  justify-content: flex-end;
}

.publish-card {
  background: #ffffff;
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid var(--color-border-secondary);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.publish-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(15, 23, 42, 0.08);
  border-color: rgba(37, 99, 235, 0.3);
}

.card-image {
  position: relative;
  height: 140px;
  overflow: hidden;
}

.card-image .el-image {
  width: 100%;
  height: 100%;
}

.no-image {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f3f4f6;
  color: #94a3b8;
  font-size: 14px;
}

.status-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  font-weight: 500;
  border-radius: 999px;
}

.card-content {
  padding: 12px;
}

.card-content h4 {
  margin: 0 0 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-content p {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.settings-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.settings-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f8fafc;
  border-radius: 14px;
  border: 1px solid var(--color-border-secondary);
}

.settings-info {
  display: flex;
  align-items: center;
  gap: 14px;
}

.settings-info .el-icon {
  font-size: 22px;
  color: var(--page-accent);
}

.settings-info h4 {
  margin: 0 0 4px;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.settings-info p {
  margin: 0;
  font-size: 12px;
  color: var(--color-text-tertiary);
}

@media (max-width: 1200px) {
  .user-page {
    flex-direction: column;
    height: auto;
    min-height: 100%;
    overflow: auto;
  }

  .user-sidebar {
    width: 100%;
  }

  .user-card {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 16px;
  }

  .card-header-bg {
    display: none;
  }

  .avatar-section {
    margin-top: 0;
    margin-bottom: 0;
  }

  .user-info {
    flex: 1;
    text-align: left;
    min-width: 200px;
  }

  .user-stats {
    flex-direction: row;
    flex-wrap: wrap;
    width: 100%;
  }

  .stat-card {
    flex: 1;
    min-width: 140px;
  }

  .progress-section {
    width: 100%;
  }

  .user-content {
    overflow: visible;
  }

  .content-body {
    overflow: visible;
    min-height: auto;
  }
}

@media (max-width: 768px) {
  .user-page {
    padding: 12px;
  }

  .user-card {
    flex-direction: column;
  }

  .user-info {
    text-align: center;
  }

  .user-stats {
    flex-direction: column;
  }

  .stat-card {
    width: 100%;
  }

  .header-tabs {
    overflow-x: auto;
  }

  .content-body {
    padding: 14px;
  }

  .panel-header {
    flex-wrap: wrap;
    gap: 10px;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .publish-grid {
    grid-template-columns: 1fr;
  }

  .publish-pagination {
    justify-content: center;
  }

  .publish-pagination :deep(.el-pagination) {
    justify-content: center;
    flex-wrap: wrap;
    row-gap: 8px;
  }
}
</style>

