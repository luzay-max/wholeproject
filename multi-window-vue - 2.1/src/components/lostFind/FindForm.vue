<template>
  <div class="find-publish">
    <div class="publish-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <div class="header-content">
          <h1 class="page-title">
            <span class="title-icon">📢</span>
            发布招领信息
          </h1>
          <p class="page-subtitle">帮助失物找到主人，共建和谐校园</p>
        </div>
      </div>

      <!-- 表单卡片 -->
      <el-card class="form-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <h3 class="card-title">
              <el-icon><DocumentAdd /></el-icon>
              招领信息填写
            </h3>
            <div class="card-tips">
              <el-icon><InfoFilled /></el-icon>
              <span>请如实填写信息，确保能够联系到您</span>
            </div>
          </div>
        </template>

        <el-form 
          ref="findFormRef" 
          :model="findForm" 
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
                  v-model="findForm.name" 
                  placeholder="请输入物品名称" 
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
                  v-model="findForm.type" 
                  dict-type="info_type"
                  placeholder="请选择物品类型" 
                  class="custom-select"
                >
                  <template #option="{ item }">
                    <div class="option-content">
                      <span class="option-icon">{{ getCategoryIcon(item.value) }}</span>
                      <span class="option-label">{{ item.label }}</span>
                    </div>
                  </template>
                </DictSelect>
              </el-form-item>
            </div>
          </div>

          <!-- 拾取地点 -->
          <el-form-item label="拾取地点" prop="location" class="form-item">
            <template #label>
              <span class="form-label">
                <el-icon><Location /></el-icon>
                拾取地点
              </span>
            </template>
            <el-input 
              v-model="findForm.location" 
              placeholder="请输入详细的拾取地点（如：图书馆二楼自习室、食堂一楼入口处）" 
              maxlength="100"
              size="large"
              class="custom-input"
            >
              <template #prefix>
                <el-icon><Location /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <!-- 拾取时间 -->
          <el-form-item label="拾取时间" prop="foundTime" class="form-item">
            <template #label>
              <span class="form-label">
                <el-icon><Clock /></el-icon>
                拾取时间
              </span>
            </template>
            <el-date-picker
              v-model="findForm.foundTime"
              type="datetime"
              placeholder="请选择拾取时间"
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
              v-model="findForm.description"
              type="textarea"
              placeholder="请详细描述物品特征（如颜色、品牌、外观、特殊标记等），帮助失主准确识别"
              :rows="5"
              maxlength="500"
              show-word-limit
              resize="none"
              class="custom-textarea"
            ></el-input>
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
                  v-model="findForm.contactName" 
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
                  v-model="findForm.contactPhone" 
                  placeholder="请输入联系电话" 
                  maxlength="11"
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
                  v-model="findForm.contactEmail" 
                  placeholder="请输入联系邮箱" 
                  maxlength="50"
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
              <span>上传清晰的物品图片有助于失主更快识别（最多9张）</span>
            </div>
            <el-upload
              :headers="{ Authorization: `${token}` }"
              action="/api/user/uploadImage"
              name="file"
              list-type="picture-card"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
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
                    <span class="upload-action" @click="handlePictureCardPreview(file)">
                      <el-icon><ZoomIn /></el-icon>
                    </span>
                    <span class="upload-action" @click="handleRemove(file)">
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
              发布招领信息
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
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
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
import { publishFind } from '../../api/findApi';
import { getToken } from '../../utils/authUtil';
import DictSelect from '../Dict/DictSelect.vue';

export default {
  name: 'FindForm',
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
    const findFormRef = ref(null);
    const loading = ref(false);
    const previewVisible = ref(false);
    const previewImage = ref('');
    const fileList = ref([]);
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
    const findForm = reactive({
      name: '',
      type: '',
      location: '',
      foundTime: null,
      description: '',
      contactName: '',
      contactPhone: '',
      contactEmail: '',
      images: []
    });

    // 表单验证规则
    const rules = {
      name: [
        { required: true, message: '请输入物品名称', trigger: 'blur' },
        { min: 1, max: 50, message: '物品名称长度不能超过50个字符', trigger: 'blur' }
      ],
      type: [
        { required: true, message: '请选择物品类型', trigger: 'change' }
      ],
      location: [
        { required: true, message: '请输入拾取地点', trigger: 'blur' },
        { min: 1, max: 100, message: '拾取地点长度不能超过100个字符', trigger: 'blur' }
      ],
      foundTime: [
        { required: true, message: '请选择拾取时间', trigger: 'change' }
      ],
      description: [
        { required: true, message: '请输入物品描述', trigger: 'blur' },
        { min: 5, max: 500, message: '物品描述长度在5-500个字符之间', trigger: 'blur' }
      ],
      contactName: [
        { min: 1, max: 20, message: '联系人姓名长度不能超过20个字符', trigger: 'blur' }
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
      findForm.images.push(imageUrl);
      file.url = imageUrl;
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
      const index = findForm.images.findIndex(img => img === file.url);
      if (index > -1) {
        findForm.images.splice(index, 1);
      }
      fileList.value = fileList.value.filter(item => item.url !== file.url);
      ElMessage.success('图片已删除');
    };
    const buildSubmitPayload = () => {
      const payload = {
        ...findForm,
        images: JSON.stringify(findForm.images)
      };
      if (findForm.contactName) {
        payload.contactName = findForm.contactName;
        payload.contact_name = findForm.contactName;
      } else {
        delete payload.contactName;
      }
      return payload;
    };
    // Submit form
    const handleSubmit = async () => {
      if (!findFormRef.value) return;

      try {
        await findFormRef.value.validate();
        loading.value = true;

        const res = await publishFind(buildSubmitPayload());

        ElMessage.success({
          message: '招领信息发布成功，等待审核通过后将显示在列表中',
          duration: 5000,
          showClose: true
        });
        emit('submit-success', res.data);
        emit('publish-success', res.data);

        handleReset();
      } catch (error) {
        if (error !== false) {
          ElMessage.error('发布失败：' + (error.message || '未知错误'));
        }
      } finally {
        loading.value = false;
      }
    };

    // 重置表单
    const handleReset = () => {
      if (findFormRef.value) findFormRef.value.resetFields();
      findForm.images = [];
    };

    return {
      findFormRef,
      loading,
      findForm,
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
      handleSubmit,
      handleReset
    };
  }
};
</script>

<style scoped>
.find-publish {
  --page-accent: #0f766e;
  --page-accent-strong: #0c5f59;
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
}

.form-col {
  min-width: 0;
}

.form-item {
  margin-bottom: 10px;
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

.custom-textarea :deep(.el-textarea__inner) {
  border-radius: 10px;
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
  border-radius: 12px;
  overflow: hidden;
}

.upload-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-actions {
  background: rgba(15, 23, 42, 0.45);
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



