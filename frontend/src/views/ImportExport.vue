<template>
  <div class="import-export-container">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="import-panel">
          <template #header>
            <span>数据导入</span>
          </template>
          
          <el-tabs v-model="importType">
            <el-tab-pane label="文件导入" name="file">
              <el-form label-width="100px">
                <el-form-item label="导入格式">
                  <el-radio-group v-model="importFormat">
                    <el-radio label="CSV">CSV</el-radio>
                    <el-radio label="JSON">JSON</el-radio>
                  </el-radio-group>
                </el-form-item>
                
                <el-form-item label="选择文件">
                  <el-upload
                    ref="uploadRef"
                    :auto-upload="false"
                    :limit="1"
                    :on-change="handleFileChange"
                    :on-exceed="handleExceed"
                    accept=".csv,.json"
                  >
                    <el-button type="primary">选择文件</el-button>
                    <template #tip>
                      <div class="upload-tip">
                        支持 CSV 和 JSON 格式，文件大小不超过 10MB
                      </div>
                    </template>
                  </el-upload>
                </el-form-item>
                
                <el-form-item>
                  <el-button
                    type="primary"
                    :loading="importing"
                    :disabled="!selectedFile"
                    @click="handleImport"
                  >
                    开始导入
                  </el-button>
                </el-form-item>
              </el-form>
              
              <el-alert
                v-if="importResult"
                :type="importResult.success ? 'success' : 'error'"
                :title="importResult.message"
                :closable="false"
                show-icon
              >
                <template v-if="importResult.details">
                  <div>成功: {{ importResult.details.successCount }}</div>
                  <div>失败: {{ importResult.details.failureCount }}</div>
                </template>
              </el-alert>
            </el-tab-pane>

            <el-tab-pane label="Hive Metastore" name="hive">
              <el-form :model="hiveConfig" label-width="120px">
                <el-form-item label="Metastore URI">
                  <el-input
                    v-model="hiveConfig.metastoreUri"
                    placeholder="thrift://localhost:9083"
                  />
                </el-form-item>
                
                <el-form-item label="数据库名">
                  <el-input
                    v-model="hiveConfig.databaseName"
                    placeholder="default"
                  />
                </el-form-item>
                
                <el-form-item label="表名模式">
                  <el-input
                    v-model="hiveConfig.tablePattern"
                    placeholder="* 表示所有表"
                  />
                </el-form-item>
                
                <el-form-item>
                  <el-button
                    type="primary"
                    :loading="importing"
                    @click="handleHiveImport"
                  >
                    从 Hive 导入
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card class="export-panel">
          <template #header>
            <span>数据导出</span>
          </template>
          
          <el-form :model="exportConfig" label-width="100px">
            <el-form-item label="导出格式">
              <el-radio-group v-model="exportConfig.format">
                <el-radio label="CSV">CSV</el-radio>
                <el-radio label="JSON">JSON</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="数据库">
              <el-input
                v-model="exportConfig.databaseName"
                placeholder="留空表示所有数据库"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="表类型">
              <el-select v-model="exportConfig.tableType" placeholder="选择类型" clearable>
                <el-option label="表" value="TABLE" />
                <el-option label="视图" value="VIEW" />
                <el-option label="外部表" value="EXTERNAL" />
              </el-select>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                :loading="exporting"
                @click="handleExport"
              >
                创建导出任务
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
        
        <el-card class="tasks-panel">
          <template #header>
            <div class="panel-header">
              <span>导出任务</span>
              <el-button text @click="loadExportTasks">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </div>
          </template>
          
          <el-table :data="exportTasks" stripe>
            <el-table-column prop="createdAt" label="创建时间" width="160">
              <template #default="{ row }">
                {{ formatDateTime(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column prop="taskType" label="格式" width="80" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusTag(row.status)">
                  {{ getStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="recordCount" label="记录数" width="100" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 'COMPLETED'"
                  text
                  type="primary"
                  @click="handleDownload(row.id)"
                >
                  下载
                </el-button>
                <span v-else-if="row.status === 'RUNNING'">
                  <el-icon class="is-loading"><Loading /></el-icon>
                </span>
                <el-tooltip v-else-if="row.status === 'FAILED'" :content="row.errorMessage">
                  <el-icon color="#F56C6C"><Warning /></el-icon>
                </el-tooltip>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, genFileId } from 'element-plus'
import type { UploadInstance, UploadProps, UploadRawFile } from 'element-plus'
import { Refresh, Loading, Warning } from '@element-plus/icons-vue'
import api from '@/api'

const importType = ref('file')
const importFormat = ref('CSV')
const importing = ref(false)
const selectedFile = ref<File | null>(null)
const uploadRef = ref<UploadInstance>()
const importResult = ref<any>(null)

const hiveConfig = ref({
  metastoreUri: '',
  databaseName: '',
  tablePattern: '*'
})

const exportConfig = ref({
  format: 'CSV',
  databaseName: '',
  tableType: ''
})
const exporting = ref(false)
const exportTasks = ref<any[]>([])

const handleFileChange: UploadProps['onChange'] = (uploadFile) => {
  selectedFile.value = uploadFile.raw as File
  importResult.value = null
}

const handleExceed: UploadProps['onExceed'] = (files) => {
  uploadRef.value!.clearFiles()
  const file = files[0] as UploadRawFile
  file.uid = genFileId()
  uploadRef.value!.handleStart(file)
}

const handleImport = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请选择文件')
    return
  }
  
  importing.value = true
  importResult.value = null
  
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    formData.append('format', importFormat.value)
    
    const response = await api.post('/api/v1/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    
    importResult.value = {
      success: true,
      message: '导入成功',
      details: response.data
    }
    ElMessage.success('导入成功')
  } catch (err: any) {
    importResult.value = {
      success: false,
      message: err.message || '导入失败'
    }
    ElMessage.error(err.message || '导入失败')
  } finally {
    importing.value = false
  }
}

const handleHiveImport = async () => {
  if (!hiveConfig.value.metastoreUri || !hiveConfig.value.databaseName) {
    ElMessage.warning('请填写 Metastore URI 和数据库名')
    return
  }
  
  importing.value = true
  importResult.value = null
  
  try {
    const response = await api.post('/api/v1/import/hive', hiveConfig.value)
    importResult.value = {
      success: true,
      message: '从 Hive 导入成功',
      details: response.data
    }
    ElMessage.success('导入成功')
  } catch (err: any) {
    importResult.value = {
      success: false,
      message: err.message || '导入失败'
    }
    ElMessage.error(err.message || '导入失败')
  } finally {
    importing.value = false
  }
}

const handleExport = async () => {
  exporting.value = true
  try {
    const response = await api.post('/api/v1/export', exportConfig.value)
    ElMessage.success('导出任务已创建')
    await loadExportTasks()
  } catch (err: any) {
    ElMessage.error(err.message || '创建导出任务失败')
  } finally {
    exporting.value = false
  }
}

const loadExportTasks = async () => {
  try {
    const response = await api.get('/api/v1/export/tasks')
    exportTasks.value = response.data
  } catch (err: any) {
    ElMessage.error(err.message || '加载导出任务失败')
  }
}

const handleDownload = async (taskId: string) => {
  try {
    const response = await api.get(`/api/v1/export/${taskId}/download`, {
      responseType: 'blob'
    })
    
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `export_${taskId}.${exportConfig.value.format.toLowerCase()}`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('下载成功')
  } catch (err: any) {
    ElMessage.error(err.message || '下载失败')
  }
}

const formatDateTime = (date: string) => {
  return new Date(date).toLocaleString('zh-CN')
}

const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    PENDING: '等待中',
    RUNNING: '运行中',
    COMPLETED: '已完成',
    FAILED: '失败'
  }
  return labels[status] || status
}

const getStatusTag = (status: string) => {
  const tags: Record<string, any> = {
    PENDING: 'info',
    RUNNING: 'warning',
    COMPLETED: 'success',
    FAILED: 'danger'
  }
  return tags[status] || 'info'
}

onMounted(() => {
  loadExportTasks()
  
  // Auto refresh export tasks every 5 seconds
  const interval = setInterval(() => {
    loadExportTasks()
  }, 5000)
  
  // Cleanup on unmount
  return () => clearInterval(interval)
})
</script>

<style scoped>
.import-export-container {
  padding: 20px;
}

.import-panel,
.export-panel,
.tasks-panel {
  margin-bottom: 20px;
}

.upload-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.is-loading {
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
