<template>
  <div class="search-bar">
    <el-autocomplete
      v-model="searchKeyword"
      :fetch-suggestions="fetchSuggestions"
      :trigger-on-focus="false"
      placeholder="搜索表名、字段名或描述..."
      clearable
      @select="handleSelect"
      @keyup.enter="handleSearch"
      class="search-input"
    >
      <template #prefix>
        <el-icon><Search /></el-icon>
      </template>
      <template #default="{ item }">
        <div class="suggestion-item">
          <el-icon><Grid /></el-icon>
          <span>{{ item.value }}</span>
        </div>
      </template>
    </el-autocomplete>
    
    <el-button 
      type="primary" 
      :icon="Search" 
      @click="handleSearch"
      class="search-button"
    >
      搜索
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Grid } from '@element-plus/icons-vue'
import { useSearchStore } from '@/stores/search'

const router = useRouter()
const searchStore = useSearchStore()

const searchKeyword = ref('')

interface Suggestion {
  value: string
}

const fetchSuggestions = async (
  queryString: string,
  cb: (suggestions: Suggestion[]) => void
) => {
  if (!queryString || queryString.trim() === '') {
    // Show search history
    const historySuggestions = searchStore.searchHistory.map(h => ({ value: h }))
    cb(historySuggestions)
    return
  }

  try {
    await searchStore.fetchSuggestions(queryString)
    const suggestions = searchStore.suggestions.map(s => ({ value: s }))
    cb(suggestions)
  } catch (err) {
    cb([])
  }
}

const handleSelect = (item: Suggestion) => {
  searchKeyword.value = item.value
  handleSearch()
}

const handleSearch = () => {
  if (!searchKeyword.value || searchKeyword.value.trim() === '') {
    return
  }

  router.push({
    path: '/search',
    query: { keyword: searchKeyword.value }
  })
}
</script>

<style scoped>
.search-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.search-input {
  flex: 1;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 20px;
}

.search-button {
  border-radius: 20px;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
