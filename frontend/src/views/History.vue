<template>
  <div class="history-container">
    <el-card class="filter-panel">
      <el-form :inline="true">
        <el-form-item label="实体类型">
          <el-select v-model="filters.entityType" placeholder="选择类型" clearable>
            <el-option label="表" value="TABLE" />
            <el-option label="字段" value="COLUMN" />
            <el-option label="血缘关系" value="LINEAGE" />
            <el-option label="目录" value="CATALOG" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="操作类型">
          <el-select v-model="filters.operation" placeholder="选择操作" clearable>
            <el-option label="创建" value="CREATE" />
            <el-option label="更新" value="UPDATE" />
            <el-option label="删除" value="DELETE" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="操作人">
          <el-input v-model="filters.changedBy" placeholder="用户名" clearable />
        </el-form-item>
        
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filters.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="loadHistory" :loading="loading">
            查询
          </el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="timeline-panel" v-loading="loading">
      <el-tabs v-model="viewMode">
        <el-tab-pane label="列表视图" name="list">
          <el-table :data="historyList" stripe>
            <el-table-column prop="changedAt" label="时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.changedAt) }}
              </template>
            </el-table-column>
            <el-table-column prop="entityType" label="实体类型" width="100">
              <template #default="{ row }">
                <el-tag :type="getEntityTypeTag(row.entityType)">
                  {{ getEntityTypeLabel(row.entityType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="operation" label="操作" width="100">
              <template #default="{ row }">
                <el-tag :type="getOperationTag(row.operation)">
                  {{ getOperationLabel(row.operation) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="fieldName" label="字段" width="150" />
            <el-table-column label="变更内容" min-width="300">
              <template #default="{ row }">
                <div class="change-content">
                  <div v-if="row.oldValue" class="old-value">
                    <span class="label">旧值:</span>
                    <code>{{ formatValue(row.oldValue) }}</code>
                  </div>
                  <div v-if="row.newValue" class="new-value">
                    <span class="label">新值:</span>
                    <code>{{ formatValue(row.newValue) }}</code>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="changedBy" label="操作人" width="120" />
          </el-table>
          
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total="total"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadHistory"
              @current-change="loadHistory"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="时间线视图" name="timeline">
          <el-timeline>
            <el-timeline-item
              v-for="item in historyList"
              :key="item.id"
              :timestamp="formatDateTime(item.changedAt)"
              placement="top"
            >
              <el-card>
                <div class="timeline-item-header">
                  <el-tag :type="getOperationTag(item.operation)">
                    {{ getOperationLabel(item.operation) }}
                  </el-tag>
                  <el-tag :type="getEntityTypeTag(item.entityType)">
                    {{ getEntityTypeLabel(item.entityType) }}
                  </el-tag>
                  <span class="user">{{ item.changedBy }}</span>
                </div>
                <div class="timeline-item-content">
                  <div v-if="item.fieldName">
                    <strong>字段:</strong> {{ item.fieldName }}
                  </div>
                  <div v-if="item.oldValue" class="change-line">
                    <span class="label">旧值:</span>
                    <code>{{ formatValue(item.oldValue) }}</code>
                  </div>
                  <div v-if="item.newValue" class="change-line">
                    <span class="label">新值:</span>
                    <code>{{ formatValue(item.newValue) }}</code>
                  </div>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
          
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total="total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @size-change="loadHistory"
              @current-change="loadHistory"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const loading = ref(false)
const viewMode = ref('list')
const filters = ref({
  entityType: '',
  operation: '',
  changedBy: '',
  dateRange: null as any
})
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const historyList = ref<any[]>([])

const loadHistory = async () => {
  loading.value = true
  try {
    const params: any = {
      page: currentPage.value,
      pageSize: pageSize.value
    }
    
    if (filters.value.entityType) params.entityType = filters.value.entityType
    if (filters.value.operation) params.operation = filters.value.operation
    if (filters.value.changedBy) params.changedBy = filters.value.changedBy
    if (filters.value.dateRange) {
      params.startDate = filters.value.dateRange[0].toISOString()
      params.endDate = filters.value.dateRange[1].toISOString()
    }
    
    const response = await api.get('/api/v1/history', { params })
    historyList.value = response.data.items
    total.value = response.data.total
  } catch (err: any) {
    ElMessage.error(err.message || '加载变更历史失败')
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.value = {
    entityType: '',
    operation: '',
    changedBy: '',
    dateRange: null
  }
  currentPage.value = 1
  loadHistory()
}

const formatDateTime = (date: string) => {
  return new Date(date).toLocaleString('zh-CN')
}

const formatValue = (value: string) => {
  try {
    const parsed = JSON.parse(value)
    return JSON.stringify(parsed, null, 2)
  } catch {
    return value
  }
}

const getEntityTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    TABLE: '表',
    COLUMN: '字段',
    LINEAGE: '血缘关系',
    CATALOG: '目录'
  }
  return labels[type] || type
}

const getEntityTypeTag = (type: string) => {
  const tags: Record<string, any> = {
    TABLE: 'primary',
    COLUMN: 'success',
    LINEAGE: 'warning',
    CATALOG: 'info'
  }
  return tags[type] || 'info'
}

const getOperationLabel = (operation: string) => {
  const labels: Record<string, string> = {
    CREATE: '创建',
    UPDATE: '更新',
    DELETE: '删除'
  }
  return labels[operation] || operation
}

const getOperationTag = (operation: string) => {
  const tags: Record<string, any> = {
    CREATE: 'success',
    UPDATE: 'warning',
    DELETE: 'danger'
  }
  return tags[operation] || 'info'
}

onMounted(() => {
  loadHistory()
})
</script>

<style scoped>
.history-container {
  padding: 20px;
}

.filter-panel {
  margin-bottom: 20px;
}

.timeline-panel {
  min-height: 600px;
}

.change-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.old-value,
.new-value {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}

.label {
  font-weight: 500;
  color: #606266;
  flex-shrink: 0;
}

code {
  background-color: #F5F7FA;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 12px;
  color: #303133;
  word-break: break-all;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.timeline-item-header {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 10px;
}

.timeline-item-header .user {
  margin-left: auto;
  color: #909399;
  font-size: 14px;
}

.timeline-item-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.change-line {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}
</style>
