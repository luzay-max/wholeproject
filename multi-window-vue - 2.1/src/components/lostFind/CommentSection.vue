<template>
  <el-card class="comment-section" shadow="hover">
    <template #header>
      <div class="card-header">
        <div class="header-title">
          <h3 class="title-text">评论区</h3>
          <span class="comment-count">{{ totalComments }} 条评论</span>
        </div>
        <div class="header-divider"></div>
      </div>
    </template>

    <!-- 评论输入 -->
    <div v-if="userStore.isLoggedIn" class="comment-input-wrapper">
      <div class="user-avatar">
        <img :src="userStore.userInfo.avatar || defaultAvatar" alt="用户头像" class="avatar">
        <div class="avatar-badge"></div>
      </div>
      <div class="input-content">
        <el-input
          v-model="commentContent"
          type="textarea"
          placeholder="写下你的评论...（支持 Markdown 语法）"
          :rows="4"
          maxlength="300"
          show-word-limit
          resize="none"
          class="comment-textarea"
        ></el-input>
        <div class="input-footer">
          <div class="footer-tips">
            <el-icon><InfoFilled /></el-icon>
            <span>文明评论，共建和谐社区</span>
          </div>
          <el-button 
            type="primary" 
            @click="submitComment" 
            :loading="submittingComment"
            class="submit-btn"
            :disabled="!commentContent.trim()"
          >
            <el-icon><Promotion /></el-icon>
            发表评论
          </el-button>
        </div>
      </div>
    </div>

    <!-- 未登录提示 -->
    <div v-else class="login-prompt">
      <div class="prompt-content">
        <div class="prompt-icon">
          <el-icon><UserFilled /></el-icon>
        </div>
        <div class="prompt-text">
          <h4>登录后参与评论</h4>
          <p>与大家一起讨论，分享你的想法</p>
        </div>
        <el-button type="primary" @click="handleLogin" class="login-btn">
          <el-icon><User /></el-icon>
          立即登录
        </el-button>
      </div>
    </div>

    <!-- 评论列表 -->
    <div class="comment-list">
      <div v-if="comments.length === 0" class="empty-comments">
        <div class="empty-icon">
          <el-icon><ChatDotRound /></el-icon>
        </div>
        <h4>暂无评论</h4>
        <p>快来发表第一条评论吧！</p>
      </div>
      
      <div v-for="(comment, index) in comments" :key="comment.id || index" class="comment-item">
        <div class="comment-avatar">
          <img :src="comment.avatar || defaultAvatar" alt="用户头像" class="avatar-img">
          <div class="avatar-ring"></div>
        </div>
        
        <div class="comment-content-wrapper">
          <div class="comment-header">
            <div class="user-info-row">
              <span class="username">{{ comment.accountName || comment.username || '匿名用户' }}</span>
              <span class="user-badge-mini" v-if="comment.userId === userStore.userInfo.id">作者</span>
              <span class="user-badge-mini admin" v-if="comment.isAdmin">管理员</span>
              <span class="comment-time">
                <el-icon><Clock /></el-icon>
                {{ formatDate(comment.createTime) }}
              </span>
            </div>
            <div v-if="canDeleteComment(comment)" class="comment-actions">
              <el-dropdown trigger="click">
                <span class="el-dropdown-link">
                  <el-icon><MoreFilled /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleDeleteComment(comment.id)" class="delete-item">
                      <el-icon><Delete /></el-icon>
                      <span>删除评论</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
          
          <div class="comment-body">
            <p class="comment-text">{{ comment.content }}</p>
          </div>
          
          <div class="comment-footer">
            <div 
              class="comment-like"
              :class="{ 'liked': isLiked(comment.id) }"
              @click="handleLikeComment(comment.id)"
            >
              <span class="like-icon">
                <el-icon><ArrowUp /></el-icon>
              </span>
              <span class="like-count">{{ comment.likeCount || 0 }}</span>
            </div>
            
            <div class="comment-reply" @click="handleReply(comment)">
              <el-icon><ChatDotRound /></el-icon>
              <span>回复</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载更多 -->
    <div v-if="comments.length > 0 && hasMore" class="load-more">
      <el-button 
        type="text" 
        @click="loadMoreComments" 
        :loading="loadingMore"
        class="load-more-btn"
      >
        <el-icon><ArrowDown /></el-icon>
        加载更多评论
      </el-button>
    </div>
  </el-card>
</template>

<script>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  MoreFilled, 
  Delete, 
  ArrowUp, 
  ChatDotRound, 
  Clock, 
  ArrowDown,
  Promotion,
  User,
  UserFilled,
  InfoFilled
} from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import { getComments, addComment, likeComment, deleteComment } from '../../api/commentsApi';
import { useUserStore } from '../../store/userStore';
import debounce from '../../utils/debounce';

export default {
  name: 'CommentSection',
  components: {
    MoreFilled, Delete, ArrowUp, ChatDotRound, Clock, ArrowDown,
    Promotion, User, UserFilled, InfoFilled
  },
  props: {
    infoId: { type: [String, Number], required: true },
    infoType: { type: String, default: 'lost', validator: (v) => ['lost','find'].includes(v) }
  },
  setup(props) {
    const userStore = useUserStore();
    const router = useRouter();
    const comments = ref([]);
    const totalComments = ref(0);
    const commentContent = ref('');
    const submittingComment = ref(false);
    const loadingMore = ref(false);
    const page = ref(1);
    const pageSize = ref(10);
    const hasMore = ref(true);
    const likedComments = ref(new Set());
    const defaultAvatar = '/src/assets/images/1.png';

    const formatDate = (ts) => {
      if (!ts) return '';
      const d = new Date(ts);
      return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`;
    };

    const canDeleteComment = (comment) => {
      if (!userStore.isLoggedIn) return false;
      if (!comment.userId) return false;
      return (userStore.userInfo.id === comment.userId) || userStore.isAdmin;
    };
    const isLiked = (commentId) => likedComments.value.has(commentId);

    const loadComments = async (isLoadMore=false) => {
      try {
        if(!isLoadMore) page.value=1;
        const params = { infoId: props.infoId, infoType: props.infoType, page: page.value, pageSize: pageSize.value };
        const res = await getComments(params);
        const data = res.data?.data?.list || res.data?.list || [];
        totalComments.value = res.data?.data?.total ?? res.data?.total ?? 0;
        data.forEach(c=>{ 
          c.username=c.username||'匿名用户'; 
          c.avatar=c.avatar||defaultAvatar; 
          c.userId = c.userId || null; 
          c.isAdmin = String(c.role || '').toUpperCase() === 'ADMIN';
        });
        if(isLoadMore) comments.value.push(...data);
        else comments.value = data;
        hasMore.value = data.length === pageSize.value;
      } catch (err) {
        if (err && err.response && err.response.status === 401) return;
        console.error(err);
        ElMessage.error('加载评论失败');
      }
    };

    const loadMoreComments = async () => {
      if(loadingMore.value||!hasMore.value) return;
      loadingMore.value=true; page.value++; await loadComments(true); loadingMore.value=false;
    };

    const submitComment = async () => {
      if(!commentContent.value.trim()) { ElMessage.warning('请输入评论内容'); return; }
      try { 
        submittingComment.value=true; 
        await addComment({ 
          infoId: props.infoId, 
          infoType: props.infoType, 
          content: commentContent.value.trim() 
        }); 
        ElMessage.success('评论发布成功'); 
        commentContent.value=''; 
        loadComments(); 
      }
      catch(e){ console.error(e); ElMessage.error('评论发布失败'); }
      finally{ submittingComment.value=false; }
    };

    const likeCommentDebounced = debounce(async (commentId) => {
      try {
        await likeComment(commentId);
        const c = comments.value.find(c=>c.id===commentId);
        if(c){
          if(isLiked(commentId)){ 
            likedComments.value.delete(commentId); 
            c.likeCount=Math.max(0,c.likeCount-1); 
          }
          else{ 
            likedComments.value.add(commentId); 
            c.likeCount=(c.likeCount||0)+1; 
          }
        }
      } catch { ElMessage.error('点赞失败'); }
    }, 300);

    const handleLikeComment = (commentId) => {
      if(!userStore.isLoggedIn) return handleLogin();
      likeCommentDebounced(commentId);
    };

    const handleDeleteComment = async (commentId) => {
      try {
        await ElMessageBox.confirm('确定要删除这条评论吗？', '确认删除', { 
          confirmButtonText: '确定', 
          cancelButtonText: '取消', 
          type: 'warning' 
        });
        await deleteComment(commentId);
        const idx = comments.value.findIndex(c=>c.id===commentId); 
        if(idx>-1) comments.value.splice(idx,1); 
        totalComments.value--;
        ElMessage.success('删除成功');
      } catch(e){ if(e!=='cancel') ElMessage.error('删除失败'); }
    };

    const handleReply = (comment) => {
      if(!userStore.isLoggedIn) return handleLogin();
      commentContent.value = `@${comment.username} `;
      const inputElement = document.querySelector('.comment-textarea textarea');
      if (inputElement) {
        inputElement.focus();
        inputElement.scrollIntoView({ behavior: 'smooth' });
      }
    };

    const handleLogin = () => router.push('/login');
    
    onMounted(async ()=>{
      if (!userStore.userInfo.role) {
        try {
          await userStore.getUserInfo();
        } catch (error) {
          console.error('Failed to load user info:', error);
        }
      }
      loadComments();
    });
    
    return { 
      comments, 
      totalComments, 
      commentContent, 
      submittingComment, 
      loadingMore, 
      hasMore, 
      userStore, 
      defaultAvatar, 
      formatDate, 
      canDeleteComment, 
      isLiked, 
      submitComment, 
      handleLikeComment, 
      handleDeleteComment, 
      handleReply,
      handleLogin, 
      loadMoreComments 
    };
  }
};
</script>

<style scoped>
.comment-section {
  border-radius: 16px;
  border: 1px solid var(--color-border-secondary);
  box-shadow: var(--shadow-1);
  overflow: hidden;
}

.comment-section :deep(.el-card__header) {
  background: #f8fafc;
  border-bottom: 1px solid var(--color-border-secondary);
  padding: 14px 16px;
}

.card-header {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.header-title {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.title-text {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.comment-count {
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.header-divider {
  height: 1px;
  background: var(--color-border-secondary);
}

.comment-input-wrapper {
  display: flex;
  gap: 12px;
  padding: 14px 16px;
  background: #f8fafc;
  border-bottom: 1px solid var(--color-border-secondary);
}

.user-avatar {
  position: relative;
  flex-shrink: 0;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #ffffff;
  box-shadow: 0 6px 12px rgba(15, 23, 42, 0.08);
}

.avatar-badge {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #22c55e;
  border: 2px solid #ffffff;
}

.input-content {
  flex: 1;
  min-width: 0;
}

.comment-textarea :deep(.el-textarea__inner) {
  border-radius: 12px;
}

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-top: 10px;
}

.footer-tips {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.submit-btn {
  border-radius: 10px;
  font-weight: 600;
}

.login-prompt {
  padding: 16px;
}

.prompt-content {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid var(--color-border-secondary);
  padding: 14px;
}

.prompt-icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: rgba(37, 99, 235, 0.12);
  color: #2563eb;
  display: flex;
  align-items: center;
  justify-content: center;
}

.prompt-text h4 {
  margin: 0 0 4px;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.prompt-text p {
  margin: 0;
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.login-btn {
  border-radius: 10px;
}

.comment-list {
  padding: 14px 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid var(--color-border-secondary);
  background: #ffffff;
}

.comment-avatar {
  flex-shrink: 0;
}

.comment-avatar img {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}

.comment-main {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.user-info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.username {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
  line-height: 1.4;
}

.user-badge-mini {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  background: #f1f5ff;
  color: #2563eb;
  font-weight: 500;
  line-height: 1;
  white-space: nowrap;
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
}

.user-badge-mini.admin {
  background: #fff1f2;
  color: #e11d48;
}

.comment-time {
  font-size: 12px;
  color: var(--color-text-tertiary);
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.comment-content {
  margin: 8px 0 6px;
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.6;
}

.comment-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  flex-wrap: wrap;
}

.comment-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  font-size: 12px;
  color: var(--color-text-secondary);
  border-radius: 999px;
  padding: 4px 10px;
  transition: all 0.2s ease;
}

.action-btn:hover {
  background: #f1f5ff;
  color: #2563eb;
}

.reply-section {
  margin-top: 10px;
  background: #f8fafc;
  border-radius: 12px;
  padding: 10px;
  border: 1px solid var(--color-border-secondary);
}

.reply-input {
  margin-bottom: 8px;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.reply-btn {
  border-radius: 10px;
  font-weight: 600;
}

.reply-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.reply-item {
  display: flex;
  gap: 10px;
  padding: 10px;
  border-radius: 10px;
  background: #ffffff;
  border: 1px solid var(--color-border-secondary);
}

.reply-avatar img {
  width: 28px;
  height: 28px;
  border-radius: 50%;
}

.reply-content {
  flex: 1;
  min-width: 0;
}

.comment-like {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--color-text-tertiary);
  border-radius: 999px;
  padding: 4px 8px;
  transition: all 0.2s ease;
}

.comment-like:hover {
  background: #f1f5ff;
  color: #2563eb;
}

.comment-like.liked {
  color: #2563eb;
}

.load-more {
  text-align: center;
  padding: 16px 0 10px;
}

.load-more-btn {
  color: #2563eb;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 999px;
  transition: all 0.2s ease;
}

.load-more-btn:hover {
  background: #f1f5ff;
}

.delete-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #dc2626;
  font-weight: 600;
}

@media (max-width: 768px) {
  .comment-input-wrapper {
    flex-direction: column;
    align-items: stretch;
  }

  .input-footer {
    flex-direction: column;
    align-items: stretch;
  }

  .comment-item {
    padding: 10px;
  }

  .comment-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .comment-time {
    margin-left: 0;
  }
}

@media (max-width: 480px) {
  .comment-section {
    margin-top: 12px;
  }

  .comment-avatar {
    display: none;
  }
}
</style>

