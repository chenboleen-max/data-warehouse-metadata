import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false, title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/components/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'tables',
        name: 'Tables',
        component: () => import('@/views/TableList.vue'),
        meta: { title: '表元数据' }
      },
      {
        path: 'tables/:id',
        name: 'TableDetail',
        component: () => import('@/views/TableDetail.vue'),
        meta: { title: '表详情' }
      },
      {
        path: 'lineage',
        name: 'Lineage',
        component: () => import('@/views/LineageGraph.vue'),
        meta: { title: '血缘关系' }
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('@/views/Search.vue'),
        meta: { title: '搜索' }
      },
      {
        path: 'catalog',
        name: 'Catalog',
        component: () => import('@/views/Catalog.vue'),
        meta: { title: '数据目录' }
      },
      {
        path: 'quality',
        name: 'Quality',
        component: () => import('@/views/Quality.vue'),
        meta: { title: '数据质量' }
      },
      {
        path: 'history',
        name: 'History',
        component: () => import('@/views/History.vue'),
        meta: { title: '变更历史' }
      },
      {
        path: 'import-export',
        name: 'ImportExport',
        component: () => import('@/views/ImportExport.vue'),
        meta: { title: '导入导出', requiresDeveloper: true }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '404' }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

// Navigation guard
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  
  // Set page title
  document.title = to.meta.title 
    ? `${to.meta.title} - 数据仓库元数据管理系统` 
    : '数据仓库元数据管理系统'

  // Check if route requires authentication
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth !== false)
  
  if (requiresAuth) {
    // Check if user is authenticated
    if (!authStore.isAuthenticated) {
      ElMessage.warning('请先登录')
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      return
    }

    // Check if user info is loaded
    if (!authStore.user) {
      try {
        await authStore.fetchCurrentUser()
      } catch (err) {
        ElMessage.error('获取用户信息失败')
        next('/login')
        return
      }
    }

    // Check developer permission
    if (to.meta.requiresDeveloper && !authStore.isDeveloper) {
      ElMessage.error('您没有权限访问此页面')
      next('/')
      return
    }

    // Check admin permission
    if (to.meta.requiresAdmin && !authStore.isAdmin) {
      ElMessage.error('您没有权限访问此页面')
      next('/')
      return
    }
  }

  // Redirect to home if already logged in and trying to access login page
  if (to.path === '/login' && authStore.isAuthenticated) {
    next('/')
    return
  }

  next()
})

export default router
