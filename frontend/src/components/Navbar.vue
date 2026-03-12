<template>
  <div class="navbar">
    <div class="navbar-left">
      <h2 class="navbar-title">
        <el-icon><DataAnalysis /></el-icon>
        数据仓库元数据管理系统
      </h2>
    </div>
    
    <div class="navbar-center">
      <SearchBar />
    </div>
    
    <div class="navbar-right">
      <el-dropdown @command="handleCommand">
        <span class="user-dropdown">
          <el-avatar :size="32" :icon="UserFilled" />
          <span class="username">{{ authStore.user?.username || '用户' }}</span>
          <el-icon><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item disabled>
              <el-tag :type="getRoleType(authStore.user?.role)">
                {{ getRoleLabel(authStore.user?.role) }}
              </el-tag>
            </el-dropdown-item>
            <el-dropdown-item divided command="profile">
              <el-icon><User /></el-icon>
              个人信息
            </el-dropdown-item>
            <el-dropdown-item command="settings">
              <el-icon><Setting /></el-icon>
              系统设置
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  DataAnalysis, 
  UserFilled, 
  ArrowDown, 
  User, 
  Setting, 
  SwitchButton 
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import SearchBar from './SearchBar.vue'
import type { UserRole } from '@/types'

const router = useRouter()
const authStore = useAuthStore()

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

const handleCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await authStore.logout()
        ElMessage.success('已退出登录')
        router.push('/login')
      } catch (err) {
        // User cancelled
      }
      break
  }
}
</script>

<style scoped>
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  height: 100%;
  padding: 0 20px;
}

.navbar-left {
  flex-shrink: 0;
}

.navbar-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.navbar-center {
  flex: 1;
  max-width: 600px;
  margin: 0 40px;
}

.navbar-right {
  flex-shrink: 0;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-dropdown:hover {
  background-color: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #606266;
}
</style>
