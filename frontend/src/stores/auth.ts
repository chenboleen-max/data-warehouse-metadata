/**
 * Authentication Store
 * Manages user authentication state and tokens
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api'
import type { User, LoginRequest, TokenResponse } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref<User | null>(null)
  const accessToken = ref<string | null>(localStorage.getItem('access_token'))
  const refreshToken = ref<string | null>(localStorage.getItem('refresh_token'))
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const isAuthenticated = computed(() => !!accessToken.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const isDeveloper = computed(() => user.value?.role === 'DEVELOPER' || user.value?.role === 'ADMIN')
  const isGuest = computed(() => user.value?.role === 'GUEST')

  // Actions
  const login = async (credentials: LoginRequest): Promise<void> => {
    try {
      loading.value = true
      error.value = null
      
      const response: TokenResponse = await authApi.login(credentials)
      
      // Save tokens
      accessToken.value = response.accessToken
      refreshToken.value = response.refreshToken
      localStorage.setItem('access_token', response.accessToken)
      localStorage.setItem('refresh_token', response.refreshToken)
      
      // Fetch user info
      await fetchCurrentUser()
    } catch (err: any) {
      error.value = err.response?.data?.message || '登录失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  const logout = async (): Promise<void> => {
    try {
      await authApi.logout()
    } catch (err) {
      console.error('Logout error:', err)
    } finally {
      // Clear state
      user.value = null
      accessToken.value = null
      refreshToken.value = null
      localStorage.removeItem('access_token')
      localStorage.removeItem('refresh_token')
    }
  }

  const fetchCurrentUser = async (): Promise<void> => {
    try {
      user.value = await authApi.getCurrentUser()
    } catch (err: any) {
      error.value = err.response?.data?.message || '获取用户信息失败'
      throw err
    }
  }

  const refreshAccessToken = async (): Promise<void> => {
    if (!refreshToken.value) {
      throw new Error('No refresh token available')
    }

    try {
      const response: TokenResponse = await authApi.refreshToken(refreshToken.value)
      
      accessToken.value = response.accessToken
      refreshToken.value = response.refreshToken
      localStorage.setItem('access_token', response.accessToken)
      localStorage.setItem('refresh_token', response.refreshToken)
    } catch (err: any) {
      // Refresh token expired, logout
      await logout()
      throw err
    }
  }

  const checkAuth = async (): Promise<boolean> => {
    if (!accessToken.value) {
      return false
    }

    try {
      await fetchCurrentUser()
      return true
    } catch (err) {
      return false
    }
  }

  return {
    // State
    user,
    accessToken,
    refreshToken,
    loading,
    error,
    
    // Getters
    isAuthenticated,
    isAdmin,
    isDeveloper,
    isGuest,
    
    // Actions
    login,
    logout,
    fetchCurrentUser,
    refreshAccessToken,
    checkAuth,
  }
}, {
  persist: {
    enabled: true,
    strategies: [
      {
        key: 'auth',
        storage: localStorage,
        paths: ['user', 'accessToken', 'refreshToken']
      }
    ]
  }
})
