import { createRouter, createWebHistory } from 'vue-router';
import { useUserStore } from '../store/userStore';
import { ElMessage } from 'element-plus';

// 导入页面组件
const Home = () => import('../views/Home.vue');
const Login = () => import('../views/Login.vue');
const Register = () => import('../views/Register.vue');
const UserCenterPage = () => import('../views/UserCenterPage.vue');
const LostPublish = () => import('../views/LostPublish.vue');
const FindPublish = () => import('../views/FindPublish.vue');
const LostList = () => import('../views/LostList.vue');
const FindList = () => import('../views/FindList.vue');
const InfoDetailPage = () => import('../views/InfoDetailPage.vue');
const HonorBoard = () => import('../views/HonorBoard.vue');
const ClaimCenter = () => import('../views/ClaimCenter.vue');
const NoticeCenter = () => import('../views/NoticeCenter.vue');

// 管理员页面组件
const AdminDashboard = () => import('../views/Admin/Dashboard.vue');
const InfoCenter = () => import('../views/Admin/InfoCenter.vue');
const UserManagement = () => import('../views/Admin/UserManagement.vue');
const CommentManagement = () => import('../views/Admin/CommentManagement.vue');
const ActivityManagement = () => import('../views/Admin/ActivityManagement.vue');
const LogManagement = () => import('../views/Admin/LogManagement.vue');
const HonorManagement = () => import('../views/Admin/HonorManagement.vue');
const WhitelistPage = () => import('../views/Admin/WhitelistPage.vue');
const DictIndex = () => import('../views/system/dict/index.vue');
const DictData = () => import('../views/system/dict/data.vue');

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: {
      title: '首页 - 校园失物招领平台',
      requiresAuth: false
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: {
      title: '登录 - 校园失物招领平台',
      requiresAuth: false,
      guest: true
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: {
      title: '注册 - 校园失物招领平台',
      requiresAuth: false,
      guest: true
    }
  },
  {
    path: '/user-center',
    name: 'UserCenterPage',
    component: UserCenterPage,
    meta: {
      title: '个人中心 - 校园失物招领平台',
      requiresAuth: true
    }
  },
  {
    path: '/lost/publish',
    name: 'LostPublish',
    component: LostPublish,
    meta: {
      title: '发布失物 - 校园失物招领平台',
      requiresAuth: true
    }
  },
  {
    path: '/find/publish',
    name: 'FindPublish',
    component: FindPublish,
    meta: {
      title: '发布招领 - 校园失物招领平台',
      requiresAuth: true
    }
  },
  {
    path: '/lost/list',
    name: 'LostList',
    component: LostList,
    meta: {
      title: '失物列表 - 校园失物招领平台',
      requiresAuth: false
    }
  },
  {
    path: '/find/list',
    name: 'FindList',
    component: FindList,
    meta: {
      title: '招领列表 - 校园失物招领平台',
      requiresAuth: false
    }
  },
  {
    path: '/claim-center',
    name: 'ClaimCenter',
    component: ClaimCenter,
    meta: {
      title: '认领中心 - 校园失物招领平台',
      requiresAuth: true
    }
  },
  {
    path: '/notice-center',
    name: 'NoticeCenter',
    component: NoticeCenter,
    meta: {
      title: '通知中心 - 校园失物招领平台',
      requiresAuth: true
    }
  },
  {
    path: '/honor-board',
    name: 'HonorBoard',
    component: HonorBoard,
    meta: {
      title: '光荣榜 - 校园失物招领平台',
      requiresAuth: false
    }
  },
  {
    path: '/info/detail/:type/:id',
    name: 'InfoDetailPage',
    component: InfoDetailPage,
    meta: {
      title: '信息详情 - 校园失物招领平台',
      requiresAuth: false
    },
    props: true
  },
  // 管理员路由
  {
    path: '/admin',
    component: () => import('../views/Admin/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        redirect: '/admin/info'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: AdminDashboard,
        meta: { title: '管理看板 - 校园失物招领平台' }
      },
      { 
        path: 'info', 
        name: 'InfoCenter', 
        component: InfoCenter, 
        meta: { title: '信息管理中心 - 校园失物招领平台' } 
      },
      { 
        path: 'users', 
        name: 'UserManagement', 
        component: UserManagement, 
        meta: { title: '用户管理 - 校园失物招领平台' } 
      },
      { 
        path: 'comments', 
        name: 'CommentManagement', 
        component: CommentManagement, 
        meta: { title: '评论管理 - 校园失物招领平台' } 
      },
      { 
        path: 'activities', 
        name: 'ActivityManagement', 
        component: ActivityManagement, 
        meta: { title: '活动日志管理 - 校园失物招领平台' } 
      },
      { 
        path: 'logs', 
        name: 'LogManagement', 
        component: LogManagement, 
        meta: { title: '操作日志管理 - 校园失物招领平台' } 
      },
      { 
        path: 'honor', 
        name: 'HonorManagement', 
        component: HonorManagement, 
        meta: { title: '光荣榜管理 - 校园失物招领平台' } 
      },
      { 
        path: 'whitelist', 
        name: 'WhitelistPage', 
        component: WhitelistPage, 
        meta: { title: '白名单管理 - 校园失物招领平台' } 
      },
      { 
        path: 'dict', 
        name: 'DictIndex', 
        component: DictIndex, 
        meta: { title: '字典管理 - 校园失物招领平台' } 
      },
      { 
        path: 'dict/data', 
        name: 'DictData', 
        component: DictData, 
        meta: { title: '字典数据 - 校园失物招领平台' } 
      }
    ]
  },
  // 404页面
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    redirect: '/',
    meta: {
      title: '页面不存在 - 校园失物招领平台',
      requiresAuth: false
    }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 全局前置守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title || '校园失物招领平台';
  
  const userStore = useUserStore();
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  const requiresAdmin = to.matched.some(record => record.meta.requiresAdmin);

  // 刷新进入受限页面时，先补齐用户信息，避免管理员被误判
  if (userStore.token && !userStore.userInfo?.role && (requiresAuth || requiresAdmin)) {
    try {
      await userStore.getUserInfo();
    } catch (_) {
      // getUserInfo 内部会清理失效登录态
    }
  }
  
  // 检查是否需要登录
  if (requiresAuth && !userStore.isLoggedIn) {
    return next({
      path: '/login',
      query: { redirect: to.fullPath }
    });
  }
  
  // 检查是否需要管理员权限
  if (requiresAdmin && !userStore.isAdmin) {
    ElMessage.error('没有权限访问该页面');
    return next('/');
  }
  
  // 已登录用户不能访问登录和注册页面
  if (to.meta.guest && userStore.isLoggedIn) {
    return next('/');
  }
  
  next();
});

export default router;
