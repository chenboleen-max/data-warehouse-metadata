<template>
  <div v-loading="tableStore.loading" class="table-detail-container">
    <el-page-header @back="goBack" class="page-header">
      <template #content>
        <span class="page-title">{{ currentTable?.tableName || '表详情' }}</span>
      </template>
      <template #extra>
        <el-button-group>
          <el-button :icon="Share" @click="viewLineage">血缘关系</el-button>
          <el-button :icon="TrendCharts" @click="viewQuality">数据质量</el-button>
          <el-button :icon="Clock" @click="viewHistory">变更历史</el-button>
        </el-button-group>
      </template>
    </el-page-header>

    <el-row :gutter="16" class="content-row">
      <el-col :span="24">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>基本信息</span>
              <el-button
                v-if="authStore.isDeveloper"
                type="primary"
                size="small"
                :icon="Edit"
                @click="handleEdit"
              >
                编辑
              </el-button>
            </div>
          </template>
          
          <el-descriptions :column="2" border>
            <el-descriptions-item label="表名">
              {{ currentTable?.tableName }}
            </el-descriptions-item>
            <el-descriptions-item label="数据库">
              {{ currentTable?.databaseName }}
            </el-descriptions-item>
            <el-descriptions-item label="表类型">
              <el-tag :type="getTableTypeTag(currentTable?.tableType)">
                {{ getTableTypeLabel(currentTable?.tableType) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="存储格式">
              {{ currentTable?.storageFormat || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="存储位置" :span="2">
              {{ currentTable?.storageLocation || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="数据大小">
              {{ formatSize(currentTable?.dataSizeBytes) }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatDate(currentTable?.createdAt) }}
            </el-descriptions-item>
            <el-descriptions-item label="更新时间">
              {{ formatDate(currentTable?.updatedAt) }}
            </el-descriptions-item>
            <el-descriptions-item label="最后访问">
              {{ formatDate(currentTable?.lastAccessedAt) }}
            </el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">
              {{ currentTable?.description || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="content-row">
      <el-col :span="24">
        <el-card class="columns-card">
          <template #header>
            <div class="card-header">
              <span>字段列表 ({{ tableStore.currentColumns.length }})</span>
            </div>
          </template>
          
          <el-table
            :data="tableStore.currentColumns"
            stripe
            style="width: 100%"
          >
            <el-table-column type="index" label="#" width="60" />
            <el-table-column prop="columnName" label="字段名" min-width="150" />
            <el-table-column prop="dataType" label="数据类型" width="120" />
            <el-table-column prop="columnOrder" label="顺序" width="80" />
            <el-table-column label="可为空" width="80">
              <template #default="{ row }">
                <el-tag :type="row.isNullable ? 'info' : 'success'" size="small">
                  {{ row.isNullable ? '是' : '否' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="分区键" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.isPartitionKey" type="warning" size="small">
                  是
                </el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column
              prop="description"
              label="描述"
              min-width="200"
              show-overflow-tooltip
            />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Edit, Share, TrendCharts, Clock } from '@element-plus/icons-vue'
import { useTableStore } from '@/stores/table'
import { useAuthStore } from '@/stores/auth'
import type { TableType } from '@/types'

const route = useRoute()
const router = useRouter()
const tableStore = useTableStore()
const authStore = useAuthStore()

const tableId = computed(() => route.params.id as string)
const currentTable = computed(() => tableStore.currentTable)

const getTableTypeLabel = (type?: TableType): string => {
  if (!type) return '-'
  const labels: Record<TableType, string> = {
    TABLE: '表',
    VIEW: '视图',
    EXTERNAL: '外部表'
  }
  return labels[type] || type
}

const getTableTypeTag = (type?: TableType): 'success' | 'info' | 'warning' => {
  if (!type) return 'info'
  const tags: Record<TableType, 'success' | 'info' | 'warning'> = {
    TABLE: 'success',
    VIEW: 'info',
    EXTERNAL: 'warning'
  }
  return tags[type] || 'info'
}

const formatDate = (dateStr?: string): string => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const formatSize = (bytes?: number): string => {
  if (!bytes) return '-'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let size = bytes
  let unitIndex = 0
  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex++
  }
  return `${size.toFixed(2)} ${units[unitIndex]}`
}

const goBack = () => {
  router.back()
}

const handleEdit = () => {
  ElMessage.info('编辑功能开发中...')
}

const viewLineage = () => {
  router.push(`/lineage?tableId=${tableId.value}`)
}

const viewQuality = () => {
  router.push(`/quality?tableId=${tableId.value}`)
}

const viewHistory = () => {
  router.push(`/history?entityType=TABLE&entityId=${tableId.value}`)
}

const fetchTableData = async () => {
  try {
    await tableStore.fetchTableById(tableId.value)
    await tableStore.fetchTableColumns(tableId.value)
  } catch (err) {
    ElMessage.error('获取表详情失败')
  }
}

onMounted(() => {
  fetchTableData()
})
</script>

<style scoped>
.table-detail-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  background: #fff;
  padding: 16px;
  border-radius: 4px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
}

.content-row {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-card,
.columns-card {
  height: 100%;
}
</style>
