<template>
  <div class="home-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="表总数" :value="stats.tableCount">
            <template #prefix>
              <el-icon><Grid /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="数据库数" :value="stats.databaseCount">
            <template #prefix>
              <el-icon><Coin /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="血缘关系" :value="stats.lineageCount">
            <template #prefix>
              <el-icon><Share /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="今日访问" :value="stats.todayVisits">
            <template #prefix>
              <el-icon><View /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>快速入口</span>
            </div>
          </template>
          <div class="quick-links">
            <el-button type="primary" :icon="Grid" @click="goTo('/tables')">
              浏览表元数据
            </el-button>
            <el-button type="success" :icon="Search" @click="goTo('/search')">
              搜索元数据
            </el-button>
            <el-button type="warning" :icon="Share" @click="goTo('/lineage')">
              查看血缘关系
            </el-button>
            <el-button type="info" :icon="FolderOpened" @click="goTo('/catalog')">
              数据目录
            </el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>系统信息</span>
            </div>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="系统版本">v1.0.0</el-descriptions-item>
            <el-descriptions-item label="当前用户">
              {{ authStore.user?.username }}
            </el-descriptions-item>
            <el-descriptions-item label="用户角色">
              <el-tag :type="getRoleType(authStore.user?.role)">
                {{ getRoleLabel(authStore.user?.role) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="登录时间">
              {{ formatDate(authStore.user?.lastLoginAt) }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Grid, Coin, Share, View, Search, FolderOpened } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import type { UserRole } from '@/types'

const router = useRouter()
const authStore = useAuthStore()

const stats = ref({
  tableCount: 0,
  databaseCount: 0,
  lineageCount: 0,
  todayVisits: 0
})

const getRoleLabel = (role?: UserRole): string => {
  const labels: Record<UserRole, string> = {
    ADMIN: '管理员',
    DEVELOPER: '开发人员',
    GUEST: '访客'
  }
  return role ? labels[role] : '未知'
}

const getRoleType = (role?: UserRole): 'success' | 'warning' | 'info' => {
  const types: Record<UserRole, 'success' | 'warning' | 'info'> = {
    ADMIN: 'success',
    DEVELOPER: 'warning',
    GUEST: 'info'
  }
  return role ? types[role] : 'info'
}

const formatDate = (dateStr?: string): string => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const goTo = (path: string) => {
  router.push(path)
}
</script>

<style scoped>
.home-container {
  padding: 0;
}

.stat-card {
  text-align: center;
}

.card-header {
  font-weight: 600;
}

.quick-links {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.quick-links .el-button {
  width: 100%;
  justify-content: flex-start;
}
</style>
