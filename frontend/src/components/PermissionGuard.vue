<template>
  <div v-if="hasPermission">
    <slot></slot>
  </div>
  <div v-else-if="showFallback" class="permission-denied">
    <el-empty description="您没有权限访问此内容">
      <el-button type="primary" @click="goBack">返回</el-button>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import type { UserRole } from '@/types'

interface Props {
  roles?: UserRole[]
  requireAdmin?: boolean
  requireDeveloper?: boolean
  showFallback?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  roles: undefined,
  requireAdmin: false,
  requireDeveloper: false,
  showFallback: true
})

const router = useRouter()
const authStore = useAuthStore()

const hasPermission = computed(() => {
  if (!authStore.user) {
    return false
  }

  // Check specific roles
  if (props.roles && props.roles.length > 0) {
    return props.roles.includes(authStore.user.role)
  }

  // Check admin requirement
  if (props.requireAdmin) {
    return authStore.isAdmin
  }

  // Check developer requirement (includes admin)
  if (props.requireDeveloper) {
    return authStore.isDeveloper
  }

  // Default: allow if authenticated
  return authStore.isAuthenticated
})

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.permission-denied {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  padding: 40px;
}
</style>
