<template>
  <div class="table-list-container">
    <el-card class="filter-card">
      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="数据库名">
          <el-input
            v-model="filters.databaseName"
            placeholder="请输入数据库名"
            clearable
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="表名">
          <el-input
            v-model="filters.tableName"
            placeholder="请输入表名"
            clearable
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="表类型">
          <el-select
            v-model="filters.tableType"
            placeholder="请选择表类型"
            clearable
            @clear="handleSearch"
          >
            <el-option label="表" value="TABLE" />
            <el-option label="视图" value="VIEW" />
            <el-option label="外部表" value="EXTERNAL" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">
            搜索
          </el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button
            v-if="authStore.isDeveloper"
            type="success"
            :icon="Plus"
            @click="handleCreate"
          >
            新建表
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table
        v-loading="tableStore.loading"
        :data="tableStore.tables"
        stripe
        style="width: 100%"
        @row-click="handleRowClick"
      >
        <el-table-column prop="tableName" label="表名" min-width="150">
          <template #default="{ row }">
            <el-link type="primary" @click.stop="goToDetail(row.id)">
              {{ row.tableName }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="databaseName" label="数据库" width="120" />
        <el-table-column prop="tableType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTableTypeTag(row.tableType)">
              {{ getTableTypeLabel(row.tableType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="description"
          label="描述"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column prop="updatedAt" label="更新时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              :icon="View"
              @click.stop="goToDetail(row.id)"
            >
              查看
            </el-button>
            <el-button
              v-if="authStore.isDeveloper"
              size="small"
              type="warning"
              :icon="Edit"
              @click.stop="handleEdit(row)"
            >
              编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        v-model:page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="tableStore.pagination.total"
        @change="handlePageChange"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus, View, Edit } from '@element-plus/icons-vue'
import { useTableStore } from '@/stores/table'
import { useAuthStore } from '@/stores/auth'
import Pagination from '@/components/Pagination.vue'
import type { TableType } from '@/types'

const router = useRouter()
const tableStore = useTableStore()
const authStore = useAuthStore()

const filters = reactive({
  databaseName: '',
  tableName: '',
  tableType: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 20
})

const getTableTypeLabel = (type: TableType): string => {
  const labels: Record<TableType, string> = {
    TABLE: '表',
    VIEW: '视图',
    EXTERNAL: '外部表'
  }
  return labels[type] || type
}

const getTableTypeTag = (type: TableType): 'success' | 'info' | 'warning' => {
  const tags: Record<TableType, 'success' | 'info' | 'warning'> = {
    TABLE: 'success',
    VIEW: 'info',
    EXTERNAL: 'warning'
  }
  return tags[type] || 'info'
}

const formatDate = (dateStr: string): string => {
  return new Date(dateStr).toLocaleString('zh-CN')
}

const fetchTables = async () => {
  try {
    await tableStore.fetchTables({
      page: pagination.page,
      pageSize: pagination.pageSize,
      databaseName: filters.databaseName || undefined,
      tableName: filters.tableName || undefined,
      tableType: filters.tableType || undefined
    })
  } catch (err) {
    ElMessage.error('获取表列表失败')
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchTables()
}

const handleReset = () => {
  filters.databaseName = ''
  filters.tableName = ''
  filters.tableType = ''
  pagination.page = 1
  fetchTables()
}

const handlePageChange = (page: number, pageSize: number) => {
  pagination.page = page
  pagination.pageSize = pageSize
  fetchTables()
}

const handleRowClick = (row: any) => {
  goToDetail(row.id)
}

const goToDetail = (id: string) => {
  router.push(`/tables/${id}`)
}

const handleCreate = () => {
  ElMessage.info('创建表功能开发中...')
}

const handleEdit = (row: any) => {
  ElMessage.info('编辑表功能开发中...')
}

onMounted(() => {
  fetchTables()
})
</script>

<style scoped>
.table-list-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-card {
  margin-bottom: 0;
}

.filter-form {
  margin-bottom: 0;
}

.table-card {
  flex: 1;
}

.el-table {
  cursor: pointer;
}

.el-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}
</style>
