<template>
  <div class="lost-publish">
    <div class="publish-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <div class="header-content">
          <h1 class="page-title">
            <span class="title-icon">🔍</span>
            发布失物信息
          </h1>
          <p class="page-subtitle">详细描述丢失物品，提高找回几率</p>
        </div>
      </div>

      <!-- 表单卡片 -->
      <el-card class="form-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <h3 class="card-title">
              <el-icon><DocumentAdd /></el-icon>
              失物信息填写
            </h3>
            <div class="card-tips">
              <el-icon><InfoFilled /></el-icon>
              <span>请详细填写信息，有助于他人识别和归还</span>
            </div>
          </div>
        </template>

        <el-form 
          ref="lostFormRef" 
          :model="lostForm" 
          :rules="rules" 
          label-width="120px" 
          v-loading="loading"
          class="beautiful-form"
          label-position="top"
          v-focus-next="handleSubmit"
        >
          <!-- 第一行：物品名称和类型 -->
          <div class="form-row">
            <div class="form-col">
              <el-form-item label="物品名称" prop="name" class="form-item">
                <template #label>
                  <span class="form-label">
                    <el-icon><Box /></el-icon>
                    物品名称
                  </span>
                </template>
                <el-input 
                  v-model="lostForm.name" 
                  placeholder="请输入丢失的物品名称" 
                  maxlength="50"
                  size="large"
                  class="custom-input"
                >
                  <template #prefix>
                    <el-icon><Box /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </div>
            
            <div class="form-col">
              <el-form-item label="物品类型" prop="type" class="form-item">
                <template #label>
                  <span class="form-label">
                    <el-icon><Collection /></el-icon>
                    物品类型
                  </span>
                </template>
                <DictSelect 
                  v-model="lostForm.type" 
                  dict-type="info_type"
                  placeholder="请选择物品类型" 
                  class="custom-select"
                >
                  <template #option="{ item }">
                    <div class="option-content">
                      <span class="option-icon">{{ getCategoryIcon(item.value || item.dictValue) }}</span>
                      <span class="option-label">{{ item.label || item.dictLabel }}</span>
                    </div>
                  </template>
                </DictSelect>
              </el-form-item>
            </div>
          </div>

          <!-- 丢失地点 -->
          <el-form-item label="丢失地点" prop="location" class="form-item">
            <template #label>
              <span class="form-label">
                <el-icon><Location /></el-icon>
                丢失地点
              </span>
            </template>
            <el-input 
              v-model="lostForm.location" 
              placeholder="请输入详细的丢失地点（如：教学楼A栋102教室、图书馆三楼自习区）" 
              maxlength="100"
              size="large"
              class="custom-input"
            >
              <template #prefix>
                <el-icon><Location /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <!-- 丢失时间 -->
          <el-form-item label="丢失时间" prop="lostTime" class="form-item">
            <template #label>
              <span class="form-label">
                <el-icon><Clock /></el-icon>
                丢失时间
              </span>
            </template>
            <el-date-picker
              v-model="lostForm.lostTime"
              type="datetime"
              placeholder="请选择丢失时间"
              size="large"
              class="custom-date-picker"
              value-format="x"
              :disabled-date="disabledDate"
            />
          </el-form-item>

          <!-- 物品描述 -->
          <el-form-item label="物品描述" prop="description" class="form-item">
            <template #label>
              <span class="form-label">
                <el-icon><Document /></el-icon>
                物品描述
              </span>
            </template>
            <el-input
              v-model="lostForm.description"
              type="textarea"
              placeholder="请详细描述物品特征（如颜色、品牌、尺寸、特殊标记、内含物品等），越详细越容易找回"
              :rows="5"
              maxlength="500"
              show-word-limit
              resize="none"
              class="custom-textarea"
            ></el-input>
            <div class="ai-actions">
              <el-button
                type="default"
                size="small"
                :loading="aiLoading"
                @click="handleAiSuggest"
                :class="['ai-btn', { 'ai-btn--filled': Boolean((lostForm.description || '').trim()) }]"
              >
                <el-icon><Lightning /></el-icon>
                {{ (lostForm.description || '').trim() ? 'AI优化描述' : 'AI补全描述' }}
              </el-button>
              <span class="ai-hint" :class="{ 'ai-hint--active': Boolean((lostForm.description || '').trim()) }">
                {{ (lostForm.description || '').trim()
                  ? '已填写描述，可预览AI建议后选择替换或追加'
                  : '基于名称、类型、地点和图片生成客观描述' }}
              </span>
            </div>
          </el-form-item>

          <!-- 联系信息标题 -->
          <div class="section-title">
            <el-icon><User /></el-icon>
            <span>联系信息</span>
          </div>

          <!-- 联系信息 -->
          <div class="form-row">
            <div class="form-col">
              <el-form-item label="联系人姓名" prop="contactName" class="form-item">
                <el-input 
                  v-model="lostForm.contactName" 
                  placeholder="请输入联系人姓名" 
                  maxlength="20"
                  size="large"
                  class="custom-input"
                >
                  <template #prefix>
                    <el-icon><User /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </div>
            
            <div class="form-col">
              <el-form-item label="联系电话" prop="contactPhone" class="form-item">
                <el-input 
                  v-model="lostForm.contactPhone" 
                  placeholder="请输入联系电话"                  maxlength="11"
                  size="large"
                  class="custom-input"
                >
                  <template #prefix>
                    <el-icon><Phone /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </div>
            
            <div class="form-col">
              <el-form-item label="联系邮箱" prop="contactEmail" class="form-item">
                <el-input 
                  v-model="lostForm.contactEmail" 
                  placeholder="请输入联系邮箱"                  maxlength="50"
                  size="large"
                  class="custom-input"
                >
                  <template #prefix>
                    <el-icon><Message /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </div>
          </div>

          <!-- 图片上传 -->
          <div class="section-title">
            <el-icon><Picture /></el-icon>
            <span>物品图片</span>
          </div>
          
          <el-form-item class="form-item">
            <div class="upload-tips">
              <el-icon><Lightning /></el-icon>
              <span>上传物品图片有助于他人识别（最多9张，可上传类似物品图片）</span>
            </div>
            <el-upload
              :headers="{ Authorization: `${token}` }"
              action="/api/user/uploadImage"
              name="file"
              list-type="picture-card"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :on-remove="handleRemove"
              :limit="9"
              multiple
              class="custom-upload"
              :file-list="fileList"
            >
              <div class="upload-add">
                <el-icon><Plus /></el-icon>
                <div class="upload-text">添加图片</div>
              </div>
              
              <template #file="{ file }">
                <div class="upload-item">
                  <img :src="file.url" alt="物品图片" class="upload-image" />
                  <div class="upload-actions">
                    <span class="upload-action preview-action" @click.stop="handlePictureCardPreview(file)">
                      <el-icon><ZoomIn /></el-icon>
                    </span>
                    <span class="upload-action delete-action" @click.stop="handleRemove(file)">
                      <el-icon><Delete /></el-icon>
                    </span>
                  </div>
                </div>
              </template>
            </el-upload>
          </el-form-item>

          <!-- 表单操作 -->
          <div class="form-actions">
            <el-button 
              type="primary" 
              @click="handleSubmit" 
              :loading="loading" 
              size="large"
              class="submit-btn"
            >
              <el-icon><Promotion /></el-icon>
              发布失物信息
            </el-button>
            <el-button 
              @click="handleReset" 
              size="large"
              class="reset-btn"
            >
              <el-icon><Refresh /></el-icon>
              重置表单
            </el-button>
          </div>
        </el-form>
      </el-card>
    </div>

    <!-- 图片预览对话框 -->
    <el-dialog v-model="previewVisible" width="60%" align-center>
      <template #header>
        <div class="preview-header">
          <h3>图片预览</h3>
        </div>
      </template>
      <div class="preview-container">
        <img :src="previewImage" alt="预览图片" class="preview-img" />
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { h, ref, reactive } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  DocumentAdd, 
  InfoFilled, 
  Box, 
  Collection, 
  Location, 
  Clock,
  Document, 
  User, 
  Phone, 
  Message, 
  Picture, 
  Lightning, 
  Plus, 
  ZoomIn, 
  Delete, 
  Promotion, 
  Refresh 
} from '@element-plus/icons-vue';
import { publishLost } from '../../api/lostApi';
import { suggestDescription } from '../../api/aiApi';
import { getToken } from '../../utils/authUtil';
import { createClickGuard } from '../../utils/clickGuard';
import DictSelect from '../Dict/DictSelect.vue';

export default {
  name: 'LostForm',
  components: {
    DictSelect,
    DocumentAdd,  
    InfoFilled, 
    Box, 
    Collection, 
    Location, 
    Clock,
    Document, 
    User, 
    Phone, 
    Message, 
    Picture, 
    Lightning, 
    Plus, 
    ZoomIn, 
    Delete, 
    Promotion, 
    Refresh
  },
  emits: ['submit-success', 'publish-success'],
  setup(props, { emit }) {
    const lostFormRef = ref(null);
    const loading = ref(false);
    const aiLoading = ref(false);
    const previewVisible = ref(false);
    const previewImage = ref('');
    const fileList = ref([]); // 添加文件列表响应式变量
    const clickGuard = createClickGuard(800);
    const token = getToken();

    // 限制日期不能超过当前时间
    const disabledDate = (time) => {
      return time.getTime() > Date.now();
    };

    // 获取分类图标
    const getCategoryIcon = (value) => {
      const iconMap = {
        'ID_CARD': '🆔',
        'ELECTRONICS': '📱',
        'BOOKS': '📚',
        'CLOTHES': '👕',
        'DAILY_NECESSITIES': '🧴',
        'OTHER': '📦'
      };
      return iconMap[value] || '📄';
    };

    // 表单数据
    const lostForm = reactive({
      name: '',
      type: '',
      location: '',
      lostTime: null,
      description: '',
      contactName: '',
      contactPhone: '',
      contactEmail: '',
      images: []
    });

    const noiseWords = new Set([
      'test', 'ceshi', 'asdf', 'qwer', 'zxcv', 'abc', 'abcd', 'abcde',
      'qwerty', 'asdfgh', 'aaaa', 'bbbb', 'cccc', 'xxxx', 'yyyy', 'zzzz',
      '1111', '1234', '12345', '123456', '6666', '9999'
    ]);

    const keyboardRows = ['qwertyuiop', 'asdfghjkl', 'zxcvbnm', '1234567890'];

    const normalizeText = (value) => (value || '').trim().replace(/\s+/g, ' ');
    const compactText = (value) => normalizeText(value).replace(/\s+/g, '');

    const isKeyboardNoise = (value) => {
      if (!value || value.length < 4) return false;
      for (const row of keyboardRows) {
        const reversed = row.split('').reverse().join('');
        for (let i = 0; i <= value.length - 4; i++) {
          const segment = value.slice(i, i + 4);
          if (row.includes(segment) || reversed.includes(segment)) return true;
        }
      }
      return false;
    };

    const isLikelyNoise = (value) => {
      const compact = compactText(value).toLowerCase();
      if (!compact) return false;
      if (noiseWords.has(compact)) return true;
      if (/^(.)\1{2,}$/.test(compact)) return true;
      if (/^(\d{2,})\1+$/.test(compact)) return true;
      return isKeyboardNoise(compact);
    };

    const validateItemName = (rule, value, callback) => {
      const normalized = normalizeText(value);
      if (!normalized) return callback(new Error('请输入物品名称'));
      if (normalized.length > 50) return callback(new Error('物品名称不能超过50个字符'));
      if (!/[\u4e00-\u9fa5A-Za-z0-9]/.test(normalized)) {
        return callback(new Error('物品名称格式不正确'));
      }
      if (/^\d+$/.test(compactText(normalized))) {
        return callback(new Error('物品名称不能只填写数字'));
      }
      if (isLikelyNoise(normalized)) {
        return callback(new Error('物品名称疑似无效，请填写真实名称'));
      }
      callback();
    };

    const validateLostLocation = (rule, value, callback) => {
      const normalized = normalizeText(value);
      if (!normalized) return callback(new Error('请输入丢失地点'));
      if (normalized.length < 2) return callback(new Error('丢失地点至少填写2个字符'));
      if (normalized.length > 100) return callback(new Error('丢失地点不能超过100个字符'));
      if (!/[\u4e00-\u9fa5A-Za-z0-9]/.test(normalized)) {
        return callback(new Error('丢失地点格式不正确'));
      }
      if (/^\d+$/.test(compactText(normalized))) {
        return callback(new Error('丢失地点不能只填写数字'));
      }
      if (isLikelyNoise(normalized)) {
        return callback(new Error('丢失地点疑似无效，请填写具体地点'));
      }
      callback();
    };

    // 表单验证规则
    const rules = {
      name: [
        { validator: validateItemName, trigger: 'blur' }
      ],
      type: [
        { required: true, message: '请选择物品类型', trigger: 'change' }
      ],
      location: [
        { validator: validateLostLocation, trigger: 'blur' }
      ],
      lostTime: [
        { required: true, message: '请选择丢失时间', trigger: 'change' }
      ],
      description: [
        { required: true, message: '请输入物品描述', trigger: 'blur' },
        { min: 5, max: 500, message: '物品描述长度在5-500个字符之间', trigger: 'blur' }
      ],
      contactName: [
        { min: 1, max: 20, message: '联系人姓名长度不能超过20个字', trigger: 'blur' }
      ],
      contactPhone: [
        { required: true, message: '请输入联系电话', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
      ],
      contactEmail: [
        { pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: '请输入正确的邮箱地址', trigger: 'blur' }
      ]
    };

    // 图片上传成功
    const handleUploadSuccess = (res, file) => {
      const imageUrl = res.data;
      lostForm.images.push(imageUrl);
      file.url = imageUrl;
      // 使用新数组引用触发重渲染
      fileList.value = [...fileList.value, file];
      ElMessage.success('图片上传成功');
    };

    // 图片上传失败
    const handleUploadError = () => {
      ElMessage.error('图片上传失败');
    };

    // 图片预览
    const handlePictureCardPreview = (file) => {
      previewImage.value = file.url;
      previewVisible.value = true;
    };

    // 删除图片
    const handleRemove = (file) => {
      // 从数据数组删除
      const dataIndex = lostForm.images.findIndex(url => url === file.url);
      if (dataIndex !== -1) {
        lostForm.images.splice(dataIndex, 1);
      }
      // 使用过滤创建新数组触发重渲染
      fileList.value = fileList.value.filter(item => item.url !== file.url);
      ElMessage.success('图片已删除');
    };
    const buildSubmitPayload = () => {
      const payload = {
        ...lostForm,
        images: JSON.stringify(lostForm.images)
      };
      if (lostForm.contactName) {
        payload.contactName = lostForm.contactName;
        payload.contact_name = lostForm.contactName;
      } else {
        delete payload.contactName;
      }
      return payload;
    };

    const formatTimeValue = (value) => {
      if (!value) return '';
      const num = Number(value);
      if (!Number.isNaN(num) && num > 0) {
        const dt = new Date(num);
        if (!Number.isNaN(dt.getTime())) {
          return `${dt.getFullYear()}-${String(dt.getMonth() + 1).padStart(2, '0')}-${String(dt.getDate()).padStart(2, '0')} ${String(dt.getHours()).padStart(2, '0')}:${String(dt.getMinutes()).padStart(2, '0')}`;
        }
      }
      return String(value);
    };

    const normalizeImages = () => {
      if (!Array.isArray(lostForm.images)) return [];
      return lostForm.images.filter((url) => typeof url === 'string' && /^https?:\/\//i.test(url));
    };

    const applyAiDescription = async (suggested) => {
      const clean = (suggested || '').trim();
      if (!clean) {
        ElMessage.warning('AI未生成有效描述，请重试');
        return null;
      }
      const current = (lostForm.description || '').trim();

      const dialogWidth = window.innerWidth <= 768 ? '92vw' : '720px';
      const previewNode = h('div', {
        class: 'ai-preview-content',
        style: {
          width: '100%',
          display: 'flex',
          flexDirection: 'column',
          gap: '8px'
        }
      }, [
        h('p', {
          class: 'ai-preview-title',
          style: {
            margin: '0',
            fontWeight: '600',
            color: '#303133'
          }
        }, 'AI建议描述（确认后写入）'),
        h(
          'div',
          {
            class: 'ai-preview-text',
            style: {
              margin: '0',
              padding: '10px 12px',
              borderRadius: '8px',
              border: '1px solid #dcdfe6',
              background: '#f8fafc',
              color: '#303133',
              lineHeight: '1.65',
              whiteSpace: 'pre-wrap',
              wordBreak: 'break-word',
              overflowWrap: 'anywhere',
              maxHeight: '36vh',
              overflowY: 'auto',
              overflowX: 'hidden'
            }
          },
          clean
        ),
        current
          ? h('p', {
            class: 'ai-preview-tip',
            style: {
              margin: '0',
              fontSize: '12px',
              color: '#909399'
            }
          }, '当前已有描述：确定=替换，取消=追加，关闭=不应用')
          : h('p', {
            class: 'ai-preview-tip',
            style: {
              margin: '0',
              fontSize: '12px',
              color: '#909399'
            }
          }, '点击“应用到描述”后会写入描述框')
      ]);

      if (!current) {
        try {
          await ElMessageBox.confirm(previewNode, 'AI描述建议', {
            type: 'info',
            confirmButtonText: '应用到描述',
            cancelButtonText: '暂不使用',
            distinguishCancelAndClose: true,
            closeOnClickModal: false,
            customClass: 'ai-preview-dialog',
            width: dialogWidth
          });
          lostForm.description = clean;
          return 'applied';
        } catch {
          return null;
        }
      }

      try {
        await ElMessageBox.confirm(
          previewNode,
          'AI描述建议',
          {
            type: 'info',
            confirmButtonText: '替换当前描述',
            cancelButtonText: '追加到现有描述',
            distinguishCancelAndClose: true,
            closeOnClickModal: false,
            customClass: 'ai-preview-dialog',
            width: dialogWidth
          }
        );
        lostForm.description = clean;
        return 'replaced';
      } catch (action) {
        if (action === 'cancel') {
          lostForm.description = `${current}\n${clean}`;
          return 'appended';
        }
        return null;
      }
    };

    const handleAiSuggest = async () => {
      await clickGuard.run('lost-ai-suggest', async () => {
        if (aiLoading.value) return;
        if (!lostFormRef.value) return;

        let baseValid = true;
        try {
          await lostFormRef.value.validateField(['name', 'type', 'location']);
        } catch {
          baseValid = false;
        }
        if (!baseValid) {
          ElMessage.warning('请先修正物品名称、类型和地点后再使用AI');
          return;
        }

        aiLoading.value = true;
        try {
          const payload = {
            itemKind: 'lost',
            name: lostForm.name,
            type: lostForm.type,
            location: lostForm.location,
            timeValue: formatTimeValue(lostForm.lostTime),
            imageUrls: normalizeImages(),
            currentDescription: lostForm.description || '',
            style: 'objective_concise'
          };
          const res = await suggestDescription(payload);
          const applyResult = await applyAiDescription(res.data?.suggestedDescription || '');
          if (res.data?.notice) {
            ElMessage.warning(res.data.notice);
          }
          if (applyResult) {
            ElMessage.success('AI建议已写入描述');
          }
        } catch (error) {
          ElMessage.error(error?.message || 'AI补全失败，请稍后再试');
        } finally {
          aiLoading.value = false;
        }
      }, { cooldown: 1000 });
    };

    // Submit form
    const handleSubmit = async () => {
      await clickGuard.run('lost-submit', async () => {
        if (!lostFormRef.value) return;

        const valid = await lostFormRef.value.validate().catch(() => false);
        if (!valid) {
          ElMessage.warning('表单未填写完整或存在错误，请检查后再提交');
          return;
        }

        loading.value = true;
        try {
          const res = await publishLost(buildSubmitPayload());

          ElMessage.success({
            message: '失物信息发布成功，等待审核通过后将显示在列表中',
            duration: 5000,
            showClose: true
          });
          emit('submit-success', res.data);
          emit('publish-success', res.data);

          handleReset();
        } catch (error) {
          ElMessage.error('发布失败：' + (error?.message || '请稍后重试'));
        } finally {
          loading.value = false;
        }
      }, { cooldown: 1200 });
    };

    // 重置表单
    const handleReset = () => {
      if (lostFormRef.value) lostFormRef.value.resetFields();
      lostForm.images = [];
    };

    return {
      lostFormRef,
      loading,
      aiLoading,
      lostForm,
      rules,
      previewVisible,
      previewImage,
      fileList,
      token,
      disabledDate,
      getCategoryIcon,
      handleUploadSuccess,
      handleUploadError,
      handlePictureCardPreview,
      handleRemove,
      handleAiSuggest,
      handleSubmit,
      handleReset
    };
  }
};
</script>

<style scoped>
.lost-publish {
  --page-accent: #2563eb;
  --page-accent-strong: #1d4ed8;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.publish-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.page-header {
  background: #ffffff;
  border-radius: 16px;
  border: 1px solid var(--color-border-secondary);
  padding: 16px;
  box-shadow: var(--shadow-1);
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 6px;
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.title-icon {
  font-size: 20px;
}

.page-subtitle {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.form-card {
  border-radius: 16px;
  border: 1px solid var(--color-border-secondary);
  box-shadow: var(--shadow-1);
  overflow: hidden;
}

.form-card :deep(.el-card__header) {
  padding: 14px 16px;
  border-bottom: 1px solid var(--color-border-secondary);
  background: #f8fafc;
}

.form-card :deep(.el-card__body) {
  padding: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.card-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-tips {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.beautiful-form {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  align-items: start;
}

.form-col {
  min-width: 0;
  display: flex;
}

.form-item {
  margin-bottom: 10px;
  width: 100%;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  color: var(--color-text-secondary);
}

.custom-input :deep(.el-input__wrapper),
.custom-select :deep(.el-input__wrapper),
.custom-date-picker :deep(.el-input__wrapper) {
  border-radius: 10px;
}

.custom-select,
.custom-date-picker {
  width: 100%;
}

.form-item :deep(.el-form-item__label) {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.form-item :deep(.el-form-item__content) {
  display: flex;
  flex-direction: column;
  align-items: stretch;
}

.form-item :deep(.el-form-item__error) {
  position: static;
  margin-top: 4px;
  min-height: 18px;
  line-height: 18px;
}

.custom-textarea :deep(.el-textarea__inner) {
  border-radius: 10px;
}

.ai-actions {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.ai-btn {
  border-radius: 8px;
}

.ai-btn--filled {
  color: #ffffff;
  border-color: #f59e0b;
  background: linear-gradient(135deg, #f59e0b 0%, #f97316 100%);
  box-shadow: 0 8px 18px rgba(249, 115, 22, 0.28);
}

.ai-btn--filled:hover {
  color: #ffffff;
  border-color: #ea580c;
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
}

.ai-hint {
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.ai-hint--active {
  color: #d97706;
  font-weight: 600;
}

:deep(.ai-preview-dialog .el-message-box__message) {
  width: 100%;
}

:deep(.ai-preview-content) {
  width: 100%;
}

:deep(.ai-preview-title) {
  margin: 0 0 8px;
  font-weight: 600;
  color: var(--color-text-primary);
}

:deep(.ai-preview-text) {
  margin: 0;
  padding: 10px;
  border-radius: 8px;
  border: 1px solid var(--color-border-secondary);
  background: #f8fafc;
  color: var(--color-text-primary);
  line-height: 1.6;
  max-height: 220px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
}

:deep(.ai-preview-tip) {
  margin: 10px 0 0;
  font-size: 12px;
  color: var(--color-text-secondary);
}

.section-title {
  margin: 10px 0 4px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.section-title .el-icon {
  color: var(--page-accent);
}

.upload-tips {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--color-text-tertiary);
  margin-bottom: 10px;
}

.custom-upload :deep(.el-upload--picture-card) {
  border-radius: 12px;
  border: 1px dashed #cbd5f5;
  background: #f8fafc;
}

.upload-add {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  color: var(--color-text-tertiary);
}

.upload-text {
  font-size: 12px;
}

.upload-item {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  width: 100%;
  height: 100%;
}

.upload-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  gap: 8px;
  z-index: 2;
  opacity: 1 !important;
}

.upload-action {
  width: 30px;
  height: 30px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  border: 2px solid #ffffff;
  background: rgba(15, 23, 42, 0.85);
  box-shadow: 0 4px 10px rgba(15, 23, 42, 0.35);
  cursor: pointer;
  transition: transform 0.15s ease, background-color 0.15s ease;
}

.upload-action .el-icon {
  font-size: 16px;
}

.upload-action:hover {
  transform: translateY(-1px);
}

.preview-action:hover {
  background: rgba(37, 99, 235, 0.95);
}

.delete-action:hover {
  background: rgba(220, 38, 38, 0.98);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 6px;
  flex-wrap: wrap;
}

.submit-btn {
  background: var(--page-accent);
  border-color: var(--page-accent);
  border-radius: 10px;
}

.submit-btn:hover:not(:disabled) {
  background: var(--page-accent-strong);
  border-color: var(--page-accent-strong);
}

.reset-btn {
  border-radius: 10px;
}

.preview-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.preview-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.preview-img {
  max-width: 100%;
  max-height: 70vh;
  border-radius: 12px;
}

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }

  .form-actions {
    flex-direction: column;
  }

  .submit-btn,
  .reset-btn {
    width: 100%;
    justify-content: center;
  }

  .custom-upload :deep(.el-upload--picture-card) {
    width: 100px;
    height: 100px;
  }

  .upload-item {
    width: 100px;
    height: 100px;
  }

  .card-tips {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .page-header,
  .form-card :deep(.el-card__header),
  .form-card :deep(.el-card__body) {
    padding-left: 12px;
    padding-right: 12px;
  }
}
</style>












