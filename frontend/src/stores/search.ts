/**
 * Search Store
 * Manages search state and history
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { searchApi } from '@/api'
import type { SearchResult, TableMetadata } from '@/types'

export const useSearchStore = defineStore('search', () => {
  // State
  const searchResults = ref<TableMetadata[]>([])
  const suggestions = ref<string[]>([])
  const searchHistory = ref<string[]>([])
  const currentKeyword = ref<string>('')
  const pagination = ref({
    page: 1,
    pageSize: 20,
    total: 0,
    totalPages: 0
  })
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Actions
  const search = async (keyword: string, params?: {
    page?: number
    pageSize?: number
    sortBy?: string
    sortOrder?: 'asc' | 'desc'
  }): Promise<void> => {
    try {
      loading.value = true
      error.value = null
      currentKeyword.value = keyword
      
      const response: SearchResult = await searchApi.searchTables({
        keyword,
        ...params
      })
      
      searchResults.value = response.items
      pagination.value = {
        page: response.page,
        pageSize: response.pageSize,
        total: response.total,
        totalPages: response.totalPages
      }
      
      // Add to search history
      addToHistory(keyword)
    } catch (err: any) {
      error.value = err.response?.data?.message || '搜索失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  const fetchSuggestions = async (prefix: string): Promise<void> => {
    try {
      suggestions.value = await searchApi.getSuggestions(prefix, 10)
    } catch (err: any) {
      console.error('Failed to fetch suggestions:', err)
      suggestions.value = []
    }
  }

  const filterTables = async (filters: Record<string, any>): Promise<void> => {
    try {
      loading.value = true
      error.value = null
      
      const response: SearchResult = await searchApi.filterTables(filters)
      
      searchResults.value = response.items
      pagination.value = {
        page: response.page,
        pageSize: response.pageSize,
        total: response.total,
        totalPages: response.totalPages
      }
    } catch (err: any) {
      error.value = err.response?.data?.message || '过滤失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  const addToHistory = (keyword: string) => {
    if (!keyword || keyword.trim() === '') return
    
    // Remove if already exists
    const index = searchHistory.value.indexOf(keyword)
    if (index !== -1) {
      searchHistory.value.splice(index, 1)
    }
    
    // Add to beginning
    searchHistory.value.unshift(keyword)
    
    // Keep only last 10
    if (searchHistory.value.length > 10) {
      searchHistory.value = searchHistory.value.slice(0, 10)
    }
    
    // Save to localStorage
    localStorage.setItem('search_history', JSON.stringify(searchHistory.value))
  }

  const clearHistory = () => {
    searchHistory.value = []
    localStorage.removeItem('search_history')
  }

  const loadHistory = () => {
    const saved = localStorage.getItem('search_history')
    if (saved) {
      try {
        searchHistory.value = JSON.parse(saved)
      } catch (err) {
        console.error('Failed to load search history:', err)
      }
    }
  }

  const clearResults = () => {
    searchResults.value = []
    currentKeyword.value = ''
    pagination.value = {
      page: 1,
      pageSize: 20,
      total: 0,
      totalPages: 0
    }
  }

  // Load history on initialization
  loadHistory()

  return {
    // State
    searchResults,
    suggestions,
    searchHistory,
    currentKeyword,
    pagination,
    loading,
    error,
    
    // Actions
    search,
    fetchSuggestions,
    filterTables,
    addToHistory,
    clearHistory,
    clearResults,
  }
})
