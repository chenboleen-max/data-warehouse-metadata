<template>
  <div class="catalog-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="tree-panel">
          <template #header>
            <div class="panel-header">
              <span>数据目录</span>
              <el-button
                v-if="authStore.isAdmin"
                type="primary"
                size="small"
                @click="showCreateDialog"
              >
                新建目录
              </el-button>
            </div>
          </template>
          
          <el-tree
            :data="catalogTree"
            :props="{ label: 'name', children: 'children' }"
            node-key="id"
            default-expand-all
            highlight-current
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <div class="tree-node">
                <span>{{ node.label }}</span>
                <div class="node-actions" v-if="authStore.isAdmin">
                  <el-button
                    text
                    size="small"
                    @click.stop="editCatalog(data)"
                  >
                    编辑
                  </el-button>
                  <el-button
                    text
                    size="small"
                    type="danger"
                    @click.stop="deleteCatalog(data)"
                  >
                    删除
                  </el-button>
                </div>
              </div>
            </template>
          </el-tree>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card class="content-panel" v-loading="loading">
          <template #header>
            <div class="panel-header">
              <span>{{ selectedCatalog ? selectedCatalog.name : '目录详情' }}</span>
            </div>
          </template>
          
          <div v-if="selectedCatalog">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="目录名称">
                {{ selectedCatalog.name }}
              </el-descriptions-item>
              <el-descriptions-item label="层级">
                {{ selectedCatalog.level }}
              </el-descriptions-item>
              <el-descriptions-item label="描述" :span="2">
                {{ selectedCatalog.description || '无' }}
              </el-descriptions-item>
            </el-descriptions>
            
            <el-divider />
            
            <div class="tables-section">
              <div class="section-header">
                <h3>包含的表</h3>
                <el-button
                  v-if="authStore.isDeveloper"
                  type="primary"
                  size="small"
                  @click="showAddTableDialog"
                >
                  添加表
                </el-button>
              </div>
              
              <el-table :data="catalogTables" stripe>
                <el-table-column prop="tableName" label="表名" width="200">
                  <template #default="{ row }">
                    <router-link :to="`/tables/${row.id}`" class="table-link">
                      {{ row.tableName }}
                    </router-link>
                  </template>
                </el-table-column>
                <el-table-column prop="databaseName" label="数据库" width="150" />
                <el-table-column prop="description" label="描述" min-width="200" />
                <el-table-column label="操作" width="100" v-if="authStore.isDeveloper">
                  <template #default="{ row }">
                    <el-button
                      text
                      type="danger"
                      size="small"
                      @click="removeTableFromCatalog(row.id)"
                    >
                      移除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
          
          <el-empty v-else description="请选择一个目录" />
        </el-card>
      </el-col>
    </el-row>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新建目录' : '编辑目录'"
      width="500px"
    >
      <el-form :model="catalogForm" label-width="80px">
        <el-form-item label="目录名称">
          <el-input v-model="catalogForm.name" placeholder="请输入目录名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="catalogForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>
        <el-form-item label="父目录">
          <el-tree-select
            v-model="catalogForm.parentId"
            :data="catalogTree"
            :props="{ label: 'name', children: 'children' }"
            placeholder="选择父目录（可选）"
            clearable
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveCatalog">确定</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="addTableDialogVisible" title="添加表到目录" width="500px">
      <el-select
        v-model="selectedTableIds"
        multiple
        filterable
        placeholder="选择要添加的表"
        style="width: 100%"
      >
        <el-option
          v-for="table in availableTables"
          :key="table.id"
          :label="`${table.databaseName}.${table.tableName}`"
          :value="table.id"
        />
      </el-select>
      <template #footer>
        <el-button @click="addTableDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddTables">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const authStore = useAuthStore()

const catalogTree = ref<any[]>([])
const selectedCatalog = ref<any>(null)
const catalogTables = ref<any[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const catalogForm = ref({
  id: '',
  name: '',
  description: '',
  parentId: null as string | null
})
const addTableDialogVisible = ref(false)
const selectedTableIds = ref<string[]>([])
const availableTables = ref<any[]>([])

const loadCatalogTree = async () => {
  try {
    const response = await api.get('/api/v1/catalog/tree')
    catalogTree.value = response.data
  } catch (err: any) {
    ElMessage.error(err.message || '加载目录树失败')
  }
}

const handleNodeClick = async (data: any) => {
  selectedCatalog.value = data
  await loadCatalogTables(data.id)
}

const loadCatalogTables = async (catalogId: string) => {
  loading.value = true
  try {
    const response = await api.get(`/api/v1/catalog/${catalogId}/tables`)
    catalogTables.value = response.data
  } catch (err: any) {
    ElMessage.error(err.message || '加载目录表列表失败')
  } finally {
    loading.value = false
  }
}

const showCreateDialog = () => {
  dialogMode.value = 'create'
  catalogForm.value = {
    id: '',
    name: '',
    description: '',
    parentId: null
  }
  dialogVisible.value = true
}

const editCatalog = (data: any) => {
  dialogMode.value = 'edit'
  catalogForm.value = {
    id: data.id,
    name: data.name,
    description: data.description,
    parentId: data.parentId
  }
  dialogVisible.value = true
}

const handleSaveCatalog = async () => {
  try {
    if (dialogMode.value === 'create') {
      await api.post('/api/v1/catalog', catalogForm.value)
      ElMessage.success('创建成功')
    } else {
      await api.put(`/api/v1/catalog/${catalogForm.value.id}`, catalogForm.value)
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    await loadCatalogTree()
  } catch (err: any) {
    ElMessage.error(err.message || '保存失败')
  }
}

const deleteCatalog = async (data: any) => {
  try {
    await ElMessageBox.confirm('确定要删除此目录吗？', '提示', {
      type: 'warning'
    })
    await api.delete(`/api/v1/catalog/${data.id}`)
    ElMessage.success('删除成功')
    await loadCatalogTree()
    if (selectedCatalog.value?.id === data.id) {
      selectedCatalog.value = null
      catalogTables.value = []
    }
  } catch (err: any) {
    if (err !== 'cancel') {
      ElMessage.error(err.message || '删除失败')
    }
  }
}

const showAddTableDialog = async () => {
  try {
    const response = await api.get('/api/v1/tables', {
      params: { page: 1, pageSize: 1000 }
    })
    availableTables.value = response.data.items
    selectedTableIds.value = []
    addTableDialogVisible.value = true
  } catch (err: any) {
    ElMessage.error(err.message || '加载表列表失败')
  }
}

const handleAddTables = async () => {
  if (!selectedCatalog.value || selectedTableIds.value.length === 0) {
    ElMessage.warning('请选择要添加的表')
    return
  }
  
  try {
    await api.post(`/api/v1/catalog/${selectedCatalog.value.id}/tables`, {
      tableIds: selectedTableIds.value
    })
    ElMessage.success('添加成功')
    addTableDialogVisible.value = false
    await loadCatalogTables(selectedCatalog.value.id)
  } catch (err: any) {
    ElMessage.error(err.message || '添加失败')
  }
}

const removeTableFromCatalog = async (tableId: string) => {
  if (!selectedCatalog.value) return
  
  try {
    await ElMessageBox.confirm('确定要从目录中移除此表吗？', '提示', {
      type: 'warning'
    })
    await api.delete(`/api/v1/catalog/${selectedCatalog.value.id}/tables/${tableId}`)
    ElMessage.success('移除成功')
    await loadCatalogTables(selectedCatalog.value.id)
  } catch (err: any) {
    if (err !== 'cancel') {
      ElMessage.error(err.message || '移除失败')
    }
  }
}

onMounted(() => {
  loadCatalogTree()
})
</script>

<style scoped>
.catalog-container {
  padding: 20px;
}

.tree-panel,
.content-panel {
  min-height: 600px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tree-node {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-right: 10px;
}

.node-actions {
  display: none;
}

.tree-node:hover .node-actions {
  display: block;
}

.tables-section {
  margin-top: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.table-link {
  color: #409EFF;
  text-decoration: none;
}

.table-link:hover {
  text-decoration: underline;
}
</style>
