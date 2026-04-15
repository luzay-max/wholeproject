import { computed, ref } from 'vue'
import {
  ChatDotRound,
  Collection,
  DataAnalysis,
  DocumentChecked,
  House,
  List,
  Monitor,
  Operation,
  Platform,
  Trophy,
  User
} from '@element-plus/icons-vue'
import { getDicts } from '../api/system/dict/data'

const ICON_MAP = {
  Platform,
  House,
  List,
  DataAnalysis,
  User,
  DocumentChecked,
  Collection,
  ChatDotRound,
  Trophy,
  Operation,
  Monitor
}

const DEFAULT_MENU_ITEMS = [
  {
    path: '/admin/info',
    label: '信息管理中心',
    dictSort: 10,
    status: '0',
    remark: '{"icon":"List","contexts":["sidebar","breadcrumb"]}'
  },
  {
    path: '/admin/dashboard',
    label: '管理看板',
    dictSort: 20,
    status: '0',
    remark: '{"icon":"DataAnalysis","contexts":["sidebar","dropdown","mobile","breadcrumb"]}'
  },
  {
    path: '/admin/users',
    label: '用户管理',
    dictSort: 30,
    status: '0',
    remark: '{"icon":"User","contexts":["sidebar","dropdown","mobile","breadcrumb"]}'
  },
  {
    path: '/admin/whitelist',
    label: '白名单管理',
    dictSort: 40,
    status: '0',
    remark: '{"icon":"DocumentChecked","contexts":["sidebar","dropdown","mobile","breadcrumb"]}'
  },
  {
    path: '/admin/dict',
    label: '数据字典',
    dictSort: 50,
    status: '0',
    remark: '{"icon":"Collection","contexts":["sidebar","dropdown","mobile","breadcrumb"]}'
  },
  {
    path: '/admin/comments',
    label: '评论管理',
    dictSort: 60,
    status: '0',
    remark: '{"icon":"ChatDotRound","contexts":["sidebar","dropdown","mobile","breadcrumb"]}'
  },
  {
    path: '/admin/honor',
    label: '光荣榜管理',
    dictSort: 70,
    status: '0',
    remark: '{"icon":"Trophy","contexts":["sidebar","dropdown","mobile","breadcrumb"]}'
  },
  {
    path: '/admin/activities',
    label: '活动日志',
    dictSort: 80,
    status: '0',
    remark: '{"icon":"Operation","contexts":["sidebar","dropdown","mobile","breadcrumb"]}'
  },
  {
    path: '/admin/logs',
    label: '操作日志',
    dictSort: 90,
    status: '0',
    remark: '{"icon":"Monitor","contexts":["sidebar","dropdown","mobile","breadcrumb"]}'
  },
  {
    path: '/admin/info?tab=audit',
    label: '信息审核',
    dictSort: 100,
    status: '0',
    remark: '{"icon":"List","contexts":["dropdown","mobile"]}'
  },
  {
    path: '/admin/info?tab=manage&type=lost',
    label: '失物管理',
    dictSort: 110,
    status: '0',
    remark: '{"icon":"List","contexts":["dropdown","mobile"]}'
  },
  {
    path: '/admin/info?tab=manage&type=find',
    label: '招领管理',
    dictSort: 120,
    status: '0',
    remark: '{"icon":"List","contexts":["dropdown","mobile"]}'
  }
]

const menuItemsRef = ref([])
let menuLoaded = false
let menuPromise = null

const normalizePath = (value) => String(value || '').trim()
const stripQuery = (value) => normalizePath(value).split('?')[0]

const parseRemark = (value) => {
  const text = String(value || '').trim()
  if (!text) return {}
  try {
    const parsed = JSON.parse(text)
    return parsed && typeof parsed === 'object' ? parsed : {}
  } catch (_) {
    return {}
  }
}

const normalizeMenuItem = (raw) => {
  const path = normalizePath(raw.path ?? raw.dictValue ?? raw.value)
  const label = String(raw.label ?? raw.dictLabel ?? raw.title ?? path).trim()
  const meta = parseRemark(raw.remark)
  const contexts = Array.isArray(meta.contexts) && meta.contexts.length
    ? meta.contexts
    : ['sidebar', 'dropdown', 'mobile', 'breadcrumb']

  return {
    key: meta.key || path,
    path,
    routePath: normalizePath(meta.routePath || stripQuery(path)),
    label,
    icon: meta.icon || 'List',
    dictSort: Number(raw.dictSort ?? meta.sort ?? 0),
    sidebar: contexts.includes('sidebar'),
    dropdown: contexts.includes('dropdown'),
    mobile: contexts.includes('mobile'),
    breadcrumb: contexts.includes('breadcrumb'),
    hidden: String(raw.status ?? '0') !== '0'
  }
}

const normalizeMenuItems = (items) => {
  return (Array.isArray(items) ? items : [])
    .map(normalizeMenuItem)
    .filter((item) => item.path)
    .filter((item) => !item.hidden)
    .sort((a, b) => a.dictSort - b.dictSort)
}

const fallbackItems = () => normalizeMenuItems(DEFAULT_MENU_ITEMS)

export const loadAdminConsoleMenu = async (force = false) => {
  if (menuLoaded && !force) {
    return menuItemsRef.value
  }
  if (menuPromise && !force) {
    return menuPromise
  }

  menuPromise = getDicts('admin_console_menu')
    .then((response) => {
      const items = normalizeMenuItems(response?.data || [])
      menuItemsRef.value = items.length ? items : fallbackItems()
      menuLoaded = true
      return menuItemsRef.value
    })
    .catch(() => {
      menuItemsRef.value = fallbackItems()
      menuLoaded = true
      return menuItemsRef.value
    })
    .finally(() => {
      menuPromise = null
    })

  return menuPromise
}

export const useAdminConsoleMenu = () => {
  const adminMenuItems = computed(() => (menuItemsRef.value.length ? menuItemsRef.value : fallbackItems()))
  const sidebarMenuItems = computed(() => adminMenuItems.value.filter((item) => item.sidebar))
  const dropdownMenuItems = computed(() => adminMenuItems.value.filter((item) => item.dropdown))
  const mobileMenuItems = computed(() => adminMenuItems.value.filter((item) => item.mobile))

  const findMenuItemByRoutePath = (path) => {
    const routePath = stripQuery(path)
    return adminMenuItems.value.find((item) => item.routePath === routePath)
  }

  const getMenuTitle = (path, fallback = '') => {
    return findMenuItemByRoutePath(path)?.label || fallback
  }

  const isBreadcrumbVisible = (path) => {
    const item = findMenuItemByRoutePath(path)
    return item ? item.breadcrumb : true
  }

  const getMenuIcon = (name) => ICON_MAP[name] || List

  return {
    adminMenuItems,
    sidebarMenuItems,
    dropdownMenuItems,
    mobileMenuItems,
    getMenuTitle,
    isBreadcrumbVisible,
    getMenuIcon
  }
}

export const adminConsoleFixedIcons = {
  Platform,
  House
}
