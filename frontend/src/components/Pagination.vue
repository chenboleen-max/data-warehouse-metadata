<template>
  <div class="pagination-container">
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="currentPageSize"
      :page-sizes="pageSizes"
      :total="total"
      :layout="layout"
      :background="background"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

interface Props {
  page?: number
  pageSize?: number
  total?: number
  pageSizes?: number[]
  layout?: string
  background?: boolean
}

interface Emits {
  (e: 'update:page', value: number): void
  (e: 'update:pageSize', value: number): void
  (e: 'change', page: number, pageSize: number): void
}

const props = withDefaults(defineProps<Props>(), {
  page: 1,
  pageSize: 20,
  total: 0,
  pageSizes: () => [10, 20, 50, 100],
  layout: 'total, sizes, prev, pager, next, jumper',
  background: true
})

const emit = defineEmits<Emits>()

const currentPage = ref(props.page)
const currentPageSize = ref(props.pageSize)

watch(() => props.page, (newVal) => {
  currentPage.value = newVal
})

watch(() => props.pageSize, (newVal) => {
  currentPageSize.value = newVal
})

const handleSizeChange = (size: number) => {
  currentPageSize.value = size
  currentPage.value = 1 // Reset to first page when page size changes
  emit('update:pageSize', size)
  emit('update:page', 1)
  emit('change', 1, size)
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  emit('update:page', page)
  emit('change', page, currentPageSize.value)
}
</script>

<style scoped>
.pagination-container {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}
</style>
