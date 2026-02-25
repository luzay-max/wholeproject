import request from './request';

// 获取OSS上传签名
export const getOssSignature = async () => {
  try {
    const res = await request({
      url: '/api/oss/getSignature',
      method: 'get'
    });
    return res.data;
  } catch (error) {
    console.error('获取OSS签名失败:', error);
    throw error;
  }
};

// 上传文件到OSS
export const uploadFileToOss = async (file, type = 'image') => {
  const enableMockOss = import.meta.env.DEV && import.meta.env.VITE_ENABLE_MOCK_OSS === 'true';

  try {
    // 仅在开发环境显式开启 VITE_ENABLE_MOCK_OSS=true 时使用模拟地址
    if (enableMockOss) {
      console.log('开发环境：使用 mock OSS 地址');
      // 构建模拟的文件URL
      const timestamp = new Date().getTime();
      const fileExt = file.name.split('.').pop();
      const folder = type === 'avatar' ? 'avatar' : 'images';
      const fileName = `${folder}/${timestamp}_${Math.random().toString(36).substring(2, 10)}.${fileExt}`;
      
      // 返回模拟的访问URL
      const mockUrl = `https://mock-oss.example.com/${fileName}`;
      console.log('模拟文件上传成功，返回URL:', mockUrl);
      
      // 模拟网络延迟
      return new Promise(resolve => setTimeout(() => resolve(mockUrl), 300));
    }
    
    // 默认走真实 OSS 上传流程（生产环境严格禁用 mock 回退）
    const signature = await getOssSignature();
    
    // 构建上传路径
    const timestamp = new Date().getTime();
    const fileExt = file.name.split('.').pop();
    const folder = type === 'avatar' ? 'avatar' : 'images';
    const fileName = `${folder}/${timestamp}_${Math.random().toString(36).substring(2, 10)}.${fileExt}`;
    
    // 构建上传表单数据
    const formData = new FormData();
    formData.append('key', fileName);
    formData.append('OSSAccessKeyId', signature.accessKeyId);
    formData.append('policy', signature.policy);
    formData.append('Signature', signature.signature);
    formData.append('success_action_status', '200');
    formData.append('file', file);
    
    // 上传文件
    const uploadRes = await fetch(signature.uploadUrl, {
      method: 'POST',
      body: formData
    });
    
    if (uploadRes.ok) {
      // 返回文件访问URL
      return `${signature.domain}/${fileName}`;
    } else {
      throw new Error('文件上传失败');
    }
  } catch (error) {
    console.error('OSS文件上传失败:', error);
    throw error;
  }
};

// 批量上传文件
export const batchUploadFiles = async (files, type = 'image') => {
  const uploadPromises = files.map(file => uploadFileToOss(file, type));
  return Promise.all(uploadPromises);
};

// 生成图片缩略图URL（如果OSS配置了图片处理）
export const getThumbnailUrl = (originalUrl, width = 200, height = 200) => {
  if (!originalUrl) return '';
  // 根据OSS图片处理规则拼接缩略图URL
  return `${originalUrl}?x-oss-process=image/resize,m_fill,w_${width},h_${height}`;
};

// 验证文件类型是否为图片
export const isImageFile = (file) => {
  const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
  return validTypes.includes(file.type);
};

// 验证文件大小
export const validateFileSize = (file, maxSizeMB = 5) => {
  const maxSize = maxSizeMB * 1024 * 1024; // 转换为字节
  return file.size <= maxSize;
};

// 获取文件大小的可读格式
export const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};
