/**
 * Lineage Store
 * Manages lineage relationship state
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { lineageApi } from '@/api'
import type { LineageGraph, ImpactReport, TableMetadata, LineageDirection } from '@/types'

export const useLineageStore = defineStore('lineage', () => {
  // State
  const lineageGraph = ref<LineageGraph | null>(null)
  const upstreamTables = ref<TableMetadata[]>([])
  const downstreamTables = ref<TableMetadata[]>([])
  const impactReport = ref<ImpactReport | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Actions
  const fetchLineageGraph = async (
    tableId: string,
    direction: LineageDirection = 'both',
    depth: number = 3
  ): Promise<void> => {
    try {
      loading.value = true
      error.value = null
      
      lineageGraph.value = await lineageApi.getLineageGraph(tableId, direction, depth)
    } catch (err: any) {
      error.value = err.response?.data?.message || '获取血缘图谱失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  const fetchUpstreamTables = async (tableId: string): Promise<void> => {
    try {
      loading.value = true
      error.value = null
      
      upstreamTables.value = await lineageApi.getUpstreamTables(tableId)
    } catch (err: any) {
      error.value = err.response?.data?.message || '获取上游表失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  const fetchDownstreamTables = async (tableId: string): Promise<void> => {
    try {
      loading.value = true
      error.value = null
      
      downstreamTables.value = await lineageApi.getDownstreamTables(tableId)
    } catch (err: any) {
      error.value = err.response?.data?.message || '获取下游表失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  const fetchImpactReport = async (tableId: string): Promise<void> => {
    try {
      loading.value = true
      error.value = null
      
      impactReport.value = await lineageApi.analyzeImpact(tableId)
    } catch (err: any) {
      error.value = err.response?.data?.message || '影响分析失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  const createLineage = async (data: {
    sourceTableId: string
    targetTableId: string
    lineageType: 'DIRECT' | 'INDIRECT'
    transformationLogic?: string
  }): Promise<void> => {
    try {
      loading.value = true
      error.value = null
      
      await lineageApi.createLineage(data)
    } catch (err: any) {
      error.value = err.response?.data?.message || '创建血缘关系失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  const deleteLineage = async (id: string): Promise<void> => {
    try {
      loading.value = true
      error.value = null
      
      await lineageApi.deleteLineage(id)
    } catch (err: any) {
      error.value = err.response?.data?.message || '删除血缘关系失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  const clearLineageData = () => {
    lineageGraph.value = null
    upstreamTables.value = []
    downstreamTables.value = []
    impactReport.value = null
  }

  return {
    // State
    lineageGraph,
    upstreamTables,
    downstreamTables,
    impactReport,
    loading,
    error,
    
    // Actions
    fetchLineageGraph,
    fetchUpstreamTables,
    fetchDownstreamTables,
    fetchImpactReport,
    createLineage,
    deleteLineage,
    clearLineageData,
  }
})
