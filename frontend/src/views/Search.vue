<template>
  <div class="search-container">
    <el-card class="search-panel">
      <el-input
        v-model="keyword"
        placeholder="搜索表名、字段名、描述..."
        size="large"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button type="primary" @click="handleSearch" :loading="loading">
            搜索
          </el-button>
        </template>
      </el-input>
      
      <el-collapse v-model="activeFilters" class="filter-panel">
        <el-collapse-item title="高级过滤" name="filters">
          <el-form :inline="true">
            <el-form-item label="数据库">
              <el-input v-model="filters.databaseName" placeholder="数据库名" clearable />
            </el-form-item>
            
            <el-form-item label="表类型">
              <el-select v-model="filters.tableType" placeholder="选择类型" clearable>
                <el-option label="表" value="TABLE" />
                <el-option label="视图" value="VIEW" />
                <el-option label="外部表" value="EXTERNAL" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="更新时间">
              <el-date-picker
                v-model="filters.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
              />
            </el-form-item>
          </el-form>
        </el-collapse-item>
      </el-collapse>
    </el-card>

    <el-card class="results-panel" v-loading="loading">
      <div v-if="searchResults.length > 0">
        <el-table :data="searchResults" stripe>
          <el-table-column prop="tableName" label="表名" width="200">
            <template #default="{ row }">
              <router-link :to="`/tables/${row.id}`" class="table-link">
                {{ row.tableName }}
              </router-link>
            </template>
          </el-table-column>
          <el-table-column prop="databaseName" label="数据库" width="150" />
          <el-table-column prop="tableType" label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getTypeTagType(row.tableType)">
                {{ getTypeLabel(row.tableType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" min-width="300">
            <template #default="{ row }">
              <span v-html="highlightKeyword(row.description)"></span>
            </template>
          </el-table-column>
          <el-table-column prop="updatedAt" label="更新时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.updatedAt) }}
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSearch"
            @current-change="handleSearch"
          />
        </div>
      </div>
      
      <el-empty v-else-if="!loading && keyword" description="没有找到匹配的结果" />
      <el-empty v-else description="输入关键词开始搜索" />
    </el-card>

    <el-card v-if="searchHistory.length > 0" class="history-panel">
      <template #header>
        <div class="history-header">
          <span>搜索历史</span>
          <el-button text @click="clearHistory">清空</el-button>
        </div>
      </template>
      <el-tag
        v-for="(item, index) in searchHistory"
        :key="index"
        class="history-tag"
        @click="keyword = item; handleSearch()"
      >
        {{ item }}
      </el-tag>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useSearchStore } from '@/stores/search'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const searchStore = useSearchStore()

const keyword = ref('')
const loading = ref(false)
const activeFilters = ref<string[]>([])
const filters = ref({
  databaseName: '',
  tableType: '',
  dateRange: null as any
})
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const searchResults = ref<any[]>([])
const searchHistory = ref<string[]>([])

const handleSearch = async () => {
  if (!keyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  loading.value = true
  try {
    const params = {
      keyword: keyword.value,
      page: currentPage.value,
      pageSize: pageSize.value,
      ...filters.value
    }

    await searchStore.search(params)
    searchResults.value = searchStore.searchResults
    total.value = searchStore.total
    
    addToHistory(keyword.value)
  } catch (err: any) {
    ElMessage.error(err.message || '搜索失败')
  } finally {
    loading.value = false
  }
}

const addToHistory = (term: string) => {
  if (!searchHistory.value.includes(term)) {
    searchHistory.value.unshift(term)
    if (searchHistory.value.length > 10) {
      searchHistory.value.pop()
    }
    localStorage.setItem('searchHistory', JSON.stringify(searchHistory.value))
  }
}

const clearHistory = () => {
  searchHistory.value = []
  localStorage.removeItem('searchHistory')
}

const highlightKeyword = (text: string) => {
  if (!text || !keyword.value) return text
  const regex = new RegExp(`(${keyword.value})`, 'gi')
  return text.replace(regex, '<mark>$1</mark>')
}

const getTypeTagType = (type: string) => {
  const typeMap: Record<string, any> = {
    TABLE: 'primary',
    VIEW: 'success',
    EXTERNAL: 'warning'
  }
  return typeMap[type] || 'info'
}

const getTypeLabel = (type: string) => {
  const labelMap: Record<string, string> = {
    TABLE: '表',
    VIEW: '视图',
    EXTERNAL: '外部表'
  }
  return labelMap[type] || type
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  const saved = localStorage.getItem('searchHistory')
  if (saved) {
    searchHistory.value = JSON.parse(saved)
  }
})
</script>

<style scoped>
.search-container {
  padding: 20px;
}

.search-panel {
  margin-bottom: 20px;
}

.filter-panel {
  margin-top: 20px;
}

.results-panel {
  margin-bottom: 20px;
}

.table-link {
  color: #409EFF;
  text-decoration: none;
}

.table-link:hover {
  text-decoration: underline;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.history-panel {
  margin-bottom: 20px;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.history-tag {
  margin-right: 10px;
  margin-bottom: 10px;
  cursor: pointer;
}

:deep(mark) {
  background-color: #FFF3CD;
  padding: 2px 4px;
  border-radius: 2px;
}
</style>
