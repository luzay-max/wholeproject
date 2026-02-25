<template>
  <div class="info-detail" v-loading="loading">
    <div class="container">
      <!-- 面包屑导航 -->
      <div class="breadcrumb">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ name: 'Home' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ name: infoType === 'lost' ? 'LostList' : 'FindList' }">
            {{ infoType === 'lost' ? '失物信息' : '招领信息' }}
          </el-breadcrumb-item>
          <el-breadcrumb-item>详情</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <!-- 主要内容 -->
      <div class="detail-container">
        <el-card class="detail-card" shadow="hover">
          <!-- 头部信息 -->
          <div class="detail-header">
            <div class="header-left">
              <h1 class="item-title">{{ infoDetail.name }}</h1>
              <div class="header-meta">
                <DictTag :options="info_status" :value="infoDetail.status" class="status-tag" />
                <span class="publish-time">
                  <el-icon><Clock /></el-icon>
                  发布时间：{{ formatDate(infoDetail.publishTime) }}
                </span>
              </div>
            </div>
            <div class="header-right">
              <div class="item-type">
                <span class="type-icon" :class="infoDetail.type">
                  {{ getCategoryIcon(infoDetail.type) }}
                </span>
                <span class="type-label">{{ getCategoryLabel(infoDetail.type) }}</span>
              </div>
            </div>
          </div>

          <!-- 图片展示 -->
          <div class="image-section" v-if="filteredImages.length">
            <h3 class="section-title">物品图片</h3>
            <div class="image-gallery" :class="imageGridClass">
              <div
                v-for="(img, index) in filteredImages"
                :key="index"
                class="gallery-item"
                @click="previewImage(img)"
              >
                <img :src="img" alt="物品图片" class="gallery-img" />
                <div class="image-overlay">
                  <el-icon><ZoomIn /></el-icon>
                </div>
              </div>
            </div>
          </div>

          <!-- 详细信息 -->
          <div class="detail-section">
            <h3 class="section-title">详细信息</h3>
            <div class="detail-grid">
              <div class="detail-item">
                <div class="detail-icon">
                  <el-icon><Location /></el-icon>
                </div>
                <div class="detail-content">
                  <label class="detail-label">地点</label>
                  <p class="detail-value">{{ infoDetail.location }}</p>
                </div>
              </div>
              
              <div class="detail-item">
                <div class="detail-icon">
                  <el-icon><Calendar /></el-icon>
                </div>
                <div class="detail-content">
                  <label class="detail-label">
                    {{ infoType === 'lost' ? '丢失时间' : '拾获时间' }}
                  </label>
                  <p class="detail-value">{{ formatDate(infoDetail.lostTime || infoDetail.findTime) }}</p>
                </div>
              </div>
              
              <div class="detail-item full-width">
                <div class="detail-icon">
                  <el-icon><Document /></el-icon>
                </div>
                <div class="detail-content">
                  <label class="detail-label">详细描述</label>
                  <p class="detail-value description">{{ infoDetail.description }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- 联系信息 -->
          <div class="contact-section">
            <h3 class="section-title">联系信息</h3>
            <div class="contact-grid">
              <div class="contact-item">
                <div class="contact-icon">
                  <el-icon><User /></el-icon>
                </div>
                <div class="contact-content">
                  <label class="contact-label">联系人</label>
                  <p class="contact-value">{{ infoDetail.contactInfo || infoDetail.contactName || '未提供' }}</p>
                </div>
              </div>
              
              <div class="contact-item">
                <div class="contact-icon">
                  <el-icon><Phone /></el-icon>
                </div>
                <div class="contact-content">
                  <label class="contact-label">联系电话</label>
                  <p class="contact-value phone">{{ infoDetail.contactPhone }}</p>
                </div>
              </div>
              
              <div class="contact-item">
                <div class="contact-icon">
                  <el-icon><Message /></el-icon>
                </div>
                <div class="contact-content">
                  <label class="contact-label">邮箱地址</label>
                  <p class="contact-value">{{ infoDetail.contactEmail }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="action-section" v-if="canShowActions">
            <h3 class="section-title">操作</h3>
            <div class="action-buttons">
              <el-button 
                type="primary" 
                size="large" 
                @click="handleContact"
                class="contact-btn"
              >
                <el-icon><Phone /></el-icon>
                联系发布者
              </el-button>
              
              <el-button 
                type="success" 
                size="large" 
                @click="handleUpdateStatus" 
                v-if="canUpdateStatus"
                class="status-btn"
              >
                <el-icon><Check /></el-icon>
                {{ infoType === 'lost' ? '标记已找回' : '标记已认领' }}
              </el-button>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 图片预览 -->
    <el-dialog v-model="dialogVisible" width="70%" align-center :append-to-body="true">
      <template #header>
        <div class="dialog-header">
          <h3>图片预览</h3>
        </div>
      </template>
      <div class="preview-container">
        <img :src="dialogImageUrl" class="preview-img" />
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Clock, 
  ZoomIn, 
  Location, 
  Calendar, 
  Document, 
  User, 
  Phone, 
  Message, 
  Check 
} from '@element-plus/icons-vue'
import { getLostDetail, updateLostStatus } from '../../api/lostApi'
import { getFindDetail, updateFindStatus } from '../../api/findApi'
import { useUserStore } from '../../store/userStore';
import { useInfoStore } from '../../store/infoStore';
import { getDicts } from '../../api/system/dict/data';
import DictTag from '../Dict/DictTag.vue';

export default {
  name: 'InfoDetail',
  components: {
    DictTag,
    Clock, ZoomIn, Location, Calendar, Document, User, Phone, Message, Check
  },
  props: {
    infoId: { type: [String, Number], required: true },
    infoType: {
      type: String,
      default: 'lost',
      validator: (v) => ['lost', 'find'].includes(v)
    }
  },
  emits: ['status-change'],
  setup(props, { emit }) {
    const userStore = useUserStore();
    const infoStore = useInfoStore();
    const loading = ref(false)
    const dialogVisible = ref(false)
    const dialogImageUrl = ref('')

    const infoDetail = ref({
      id: null, name: '', type: '', location: '', lostTime: null, findTime: null,
      description: '', contactName: '', contactPhone: '', contactEmail: '',
      images: [], publishTime: null, status: '', userId: null
    })

    const info_type = ref([])
    const info_status = ref([])

    onMounted(() => {
      getDicts('info_type').then(res => info_type.value = res.data)
      getDicts('info_status').then(res => info_status.value = res.data)
    })

    /** 图片字段自动解析 */
    const parseImages = (images) => {
      if (!images) return []
      if (Array.isArray(images)) return images
      try {
        const parsed = JSON.parse(images)
        return parsed.map(img => img.trim())
      } catch {
        return []
      }
    }

    const filteredImages = computed(() => parseImages(infoDetail.value.images))

    const imageGridClass = computed(() => {
      const count = filteredImages.value.length;
      if (count === 1) return 'grid-1';
      if (count === 2 || count === 4) return 'grid-2';
      return 'grid-3';
    })

    const canShowActions = computed(() => userStore.isLoggedIn && infoDetail.value.status === 'APPROVED')
    const canUpdateStatus = computed(() => canShowActions.value && userStore.userInfo.id === infoDetail.value.userId)

    function getCategoryIcon(value) {
      const typeKey = String(value ?? '').trim().toUpperCase()
      const map = {
        ID_CARD: '🆔',
        ELECTRONICS: '📱',
        BOOKS: '📚',
        CLOTHES: '👕',
        DAILY_NECESSITIES: '🧴',
        OTHER: '📦',
        // 兼容历史数字值
        '1': '📱',
        '2': '🧴',
        '3': '🆔',
        '4': '📦'
      }
      return map[typeKey] || '📦'
    }

    function getCategoryLabel(value) {
      const item = info_type.value.find(p => (p.value || p.dictValue) == value)
      return item ? (item.label || item.dictLabel) : value
    }

    function formatDate(ts) {
      if (!ts) return ''
      const d = new Date(ts)
      return isNaN(d.getTime()) ? ts : `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
    }

    async function loadDetail() {
      loading.value = true
      try {
        const res = props.infoType === 'lost'
          ? await getLostDetail(props.infoId)
          : await getFindDetail(props.infoId)
        const data = res.data || {}
        infoDetail.value = {
          ...data,
          contactName: data.contactName ?? data.contact_name ?? data.contactName,
          contactPhone: data.contactPhone ?? data.contact_phone ?? data.contactPhone,
          contactEmail: data.contactEmail ?? data.contact_email ?? data.contactEmail
        }
      } finally {
        loading.value = false
      }
    }

    function previewImage(url) {
      dialogImageUrl.value = url
      dialogVisible.value = true
    }

    function handleContact() {
      const phone = infoDetail.value.contactPhone;
      if (!phone) {
        ElMessage.warning('未提供联系电话');
        return;
      }
      navigator.clipboard.writeText(phone).then(() => {
        ElMessage.success('电话号码已复制: ' + phone);
      }).catch(err => {
        console.error('Copy failed:', err);
        ElMessage.error('复制失败，请手动复制');
      });
    }

    async function handleUpdateStatus() {
      try {
        const updateFn = props.infoType === 'lost' ? updateLostStatus : updateFindStatus
        await updateFn({ id: props.infoId, status: 'SOLVED' })
        infoDetail.value.status = 'SOLVED'
        ElMessage.success('状态已更新');
        await infoStore.fetchActivities();
        emit('status-change', 'SOLVED')
      } catch (error) {
        ElMessage.error('状态更新失败');
      }
    }

    watch(() => props.infoId, loadDetail, { immediate: true })

    return {
      infoDetail, filteredImages, imageGridClass, loading, dialogVisible, dialogImageUrl,
      canShowActions, canUpdateStatus, getCategoryLabel, getCategoryIcon,
      formatDate, previewImage, 
      handleContact, handleUpdateStatus,
      info_status, info_type
    }
  }
}
</script>

<style scoped>
.info-detail {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 0;
}

.breadcrumb {
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.detail-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-card {
  border-radius: 16px;
  border: 1px solid var(--color-border-secondary);
  box-shadow: var(--shadow-1);
  overflow: hidden;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  padding: 12px;
  background: #f8fafc;
  border-bottom: 1px solid var(--color-border-secondary);
}

.header-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-top: 8px;
}

.item-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.status-tag {
  border-radius: 999px;
  font-weight: 600;
}

.publish-time {
  font-size: 12px;
  color: var(--color-text-tertiary);
  display: flex;
  align-items: center;
  gap: 4px;
}

.item-type {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #ffffff;
  border: 1px solid var(--color-border-secondary);
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 12px;
  color: var(--color-text-secondary);
}

.type-icon {
  width: 26px;
  height: 26px;
  border-radius: 8px;
  background: var(--page-accent-soft, rgba(37, 99, 235, 0.12));
  color: var(--page-accent, #2563eb);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 12px;
}

.image-section,
.detail-section,
.contact-section,
.action-section {
  padding: 12px;
  border-bottom: 1px solid var(--color-border-secondary);
}

.action-section {
  border-bottom: none;
}

.image-gallery {
  display: grid;
  gap: 6px;
}

.image-gallery.grid-1 {
  grid-template-columns: 1fr;
  max-width: 60%;
}

.image-gallery.grid-2 {
  grid-template-columns: repeat(2, 1fr);
  max-width: 80%;
}

.image-gallery.grid-3 {
  grid-template-columns: repeat(3, 1fr);
}

.gallery-item {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid var(--color-border-secondary);
  aspect-ratio: 1;
}

.grid-1 .gallery-item {
  aspect-ratio: auto;
  max-height: 400px;
}

.gallery-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.image-overlay {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.gallery-item:hover .image-overlay {
  opacity: 1;
}

.detail-grid,
.contact-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.full-width {
  grid-column: 1 / -1;
}

.detail-item,
.contact-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 8px;
  border-radius: 8px;
  background: #f8fafc;
  border: 1px solid var(--color-border-secondary);
}

.detail-content,
.contact-content {
  flex: 1;
  min-width: 0;
}

.detail-icon,
.contact-icon {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--page-accent-soft, rgba(37, 99, 235, 0.12));
  color: var(--page-accent, #2563eb);
}

.detail-label,
.contact-label {
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.detail-value,
.contact-value {
  margin: 4px 0 0;
  font-size: 14px;
  color: var(--color-text-primary);
  font-weight: 500;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.5;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  flex-wrap: wrap;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border-radius: 10px;
  font-weight: 600;
  transition: all 0.2s ease;
}

.contact-btn {
  background: var(--page-accent, #2563eb);
  border: 1px solid var(--page-accent, #2563eb);
}

.status-btn {
  background: #16a34a;
  border: 1px solid #16a34a;
}

.contact-btn:hover,
.status-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 16px rgba(15, 23, 42, 0.12);
}

.dialog-header {
  text-align: center;
}

.dialog-header h3 {
  margin: 0;
  color: var(--color-text-primary);
}

.preview-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 12px;
}

.preview-img {
  max-width: 100%;
  max-height: 70vh;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.15);
}

@media (max-width: 768px) {
  .detail-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .detail-grid,
  .contact-grid {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    flex-direction: column;
    align-items: stretch;
  }

  .contact-btn,
  .status-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>


