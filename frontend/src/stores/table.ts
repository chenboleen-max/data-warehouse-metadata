/**
 * Table Metadata Store
 * Manages table metadata state
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { tablesApi } from '@/api'
import type { TableMetadata, Column, PagedResponse } from '@/types'

export const useTableStore = defineStore('table', () => {
  // State
  const tables = ref<TableMetadata[]>([])
  const currentTable = ref<TableMetadata | null>(null)
  const currentColumns = ref<Column[]>([])
  const pagination = ref({
    page: 1,
    pageSize: 20,
    total: 0,
    totalPages: 0
  })
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Actions
  const fetchTables = async (params?: {
    page?: number
    pageSize?: number
    databaseName?: string
    tableName?: string
    tableType?: string
    sortBy?: string
    sortOrder?: 'asc' | 'desc'
  }): Promise<void> => {
    try {
      loading.value = true
      error.value = null
      
      const response: PagedResponse<TableMetadata> = await tablesApi.listTables(params)
      
      tables.value = response.items
      pagination.value = {
        page: response.page,
        pageSize: response.pageSize,
        total: response.total,
        totalPages: response.totalPages
      }
    } catch (err: any) {
      error.value = err.response?.data?.message || '获取表列表失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  const fetchTableById = async (id: string): Promise<void> => {
    try {
      loading.value = true
      error.value = null
      
      currentTable.value = await tablesApi.getTableById(id)
    } catch (err: any) {
      error.value = err.response?.data?.message || '获取表详情失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  const fetchTableColumns = async (tableId: string): Promise<void> => {
    try {
      loading.value = true
      error.value = null
      
      currentColumns.value = await tablesApi.getTableColumns(tableId)
    } catch (err: any) {
      error.value = err.response?.data?.message || '获取字段列表失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  const createTable = async (data: any): Promise<TableMetadata> => {
    try {
      loading.value = true
      error.value = null
      
      const table = await tablesApi.createTable(data)
      tables.value.unshift(table)
      
      return table
    } catch (err: any) {
      error.value = err.response?.data?.message || '创建表失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  const updateTable = async (id: string, data: any): Promise<TableMetadata> => {
    try {
      loading.value = true
      error.value = null
      
      const table = await tablesApi.updateTable(id, data)
      
      // Update in list
      const index = tables.value.findIndex(t => t.id === id)
      if (index !== -1) {
        tables.value[index] = table
      }
      
      // Update current table
      if (currentTable.value?.id === id) {
        currentTable.value = table
      }
      
      return table
    } catch (err: any) {
      error.value = err.response?.data?.message || '更新表失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  const deleteTable = async (id: string): Promise<void> => {
    try {
      loading.value = true
      error.value = null
      
      await tablesApi.deleteTable(id)
      
      // Remove from list
      tables.value = tables.value.filter(t => t.id !== id)
      
      // Clear current table if deleted
      if (currentTable.value?.id === id) {
        currentTable.value = null
      }
    } catch (err: any) {
      error.value = err.response?.data?.message || '删除表失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  const clearCurrentTable = () => {
    currentTable.value = null
    currentColumns.value = []
  }

  return {
    // State
    tables,
    currentTable,
    currentColumns,
    pagination,
    loading,
    error,
    
    // Actions
    fetchTables,
    fetchTableById,
    fetchTableColumns,
    createTable,
    updateTable,
    deleteTable,
    clearCurrentTable,
  }
})
