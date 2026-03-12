<template>
  <el-menu
    :default-active="activeMenu"
    class="sidebar-menu"
    router
    @select="handleSelect"
  >
    <el-menu-item index="/">
      <el-icon><HomeFilled /></el-icon>
      <span>首页</span>
    </el-menu-item>
    
    <el-menu-item index="/tables">
      <el-icon><Grid /></el-icon>
      <span>表元数据</span>
    </el-menu-item>
    
    <el-menu-item index="/lineage">
      <el-icon><Share /></el-icon>
      <span>血缘关系</span>
    </el-menu-item>
    
    <el-menu-item index="/search">
      <el-icon><Search /></el-icon>
      <span>搜索</span>
    </el-menu-item>
    
    <el-menu-item index="/catalog">
      <el-icon><FolderOpened /></el-icon>
      <span>数据目录</span>
    </el-menu-item>
    
    <el-menu-item index="/quality">
      <el-icon><TrendCharts /></el-icon>
      <span>数据质量</span>
    </el-menu-item>
    
    <el-menu-item index="/history">
      <el-icon><Clock /></el-icon>
      <span>变更历史</span>
    </el-menu-item>
    
    <el-menu-item index="/import-export" v-if="authStore.isDeveloper">
      <el-icon><Upload /></el-icon>
      <span>导入导出</span>
    </el-menu-item>
    
    <el-divider v-if="authStore.isAdmin" />
    
    <el-sub-menu index="admin" v-if="authStore.isAdmin">
      <template #title>
        <el-icon><Setting /></el-icon>
        <span>系统管理</span>
      </template>
      <el-menu-item index="/admin/users">
        <el-icon><User /></el-icon>
        <span>用户管理</span>
      </el-menu-item>
      <el-menu-item index="/admin/permissions">
        <el-icon><Lock /></el-icon>
        <span>权限管理</span>
      </el-menu-item>
      <el-menu-item index="/admin/logs">
        <el-icon><Document /></el-icon>
        <span>系统日志</span>
      </el-menu-item>
    </el-sub-menu>
  </el-menu>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  HomeFilled,
  Grid,
  Share,
  Search,
  FolderOpened,
  TrendCharts,
  Clock,
  Upload,
  Setting,
  User,
  Lock,
  Document
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const authStore = useAuthStore()

const activeMenu = computed(() => route.path)

const handleSelect = (index: string) => {
  console.log('Selected menu:', index)
}
</script>

<style scoped>
.sidebar-menu {
  border-right: none;
  height: 100%;
}

.sidebar-menu .el-menu-item,
.sidebar-menu .el-sub-menu {
  height: 48px;
  line-height: 48px;
}

.sidebar-menu .el-menu-item.is-active {
  background-color: #ecf5ff;
  color: #409eff;
}
</style>
