<template>
  <div class="quality-container">
    <el-card class="selector-panel">
      <el-form :inline="true">
        <el-form-item label="选择表">
          <el-select
            v-model="selectedTableId"
            filterable
            placeholder="请选择表"
            style="width: 400px"
            @change="loadQualityMetrics"
          >
            <el-option
              v-for="table in tables"
              :key="table.id"
              :label="`${table.databaseName}.${table.tableName}`"
              :value="table.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="时间范围">
          <el-select v-model="days" @change="loadQualityMetrics" style="width: 150px">
            <el-option :value="7" label="最近7天" />
            <el-option :value="30" label="最近30天" />
            <el-option :value="90" label="最近90天" />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>
    
    <div v-if="selectedTableId" v-loading="loading">
      <el-row :gutter="20" class="metrics-row">
        <el-col :span="6">
          <el-card>
            <el-statistic title="记录数" :value="currentMetrics?.recordCount || 0" />
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card>
            <el-statistic
              title="空值率"
              :value="((currentMetrics?.nullRate || 0) * 100).toFixed(2)"
              suffix="%"
            />
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card>
            <el-statistic
              title="更新频率"
              :value="currentMetrics?.updateFrequency || 0"
              suffix="次/天"
            />
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card>
            <el-statistic
              title="数据新鲜度"
              :value="currentMetrics?.dataFreshnessHours || 0"
              suffix="小时"
            />
          </el-card>
        </el-col>
      </el-row>

      <el-card class="chart-panel">
        <template #header>
          <span>质量趋势</span>
        </template>
        <div ref="chartRef" class="chart-container"></div>
      </el-card>
      
      <el-card class="score-panel">
        <template #header>
          <span>质量评分</span>
        </template>
        <div class="score-content">
          <div class="score-circle">
            <el-progress
              type="circle"
              :percentage="qualityScore"
              :width="200"
              :color="getScoreColor(qualityScore)"
            >
              <template #default="{ percentage }">
                <span class="score-text">{{ percentage }}</span>
                <span class="score-label">分</span>
              </template>
            </el-progress>
          </div>
          <div class="score-details">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="数据完整性">
                <el-progress
                  :percentage="(1 - (currentMetrics?.nullRate || 0)) * 100"
                  :color="getScoreColor((1 - (currentMetrics?.nullRate || 0)) * 100)"
                />
              </el-descriptions-item>
              <el-descriptions-item label="数据新鲜度">
                <el-progress
                  :percentage="getFreshnessScore(currentMetrics?.dataFreshnessHours || 0)"
                  :color="getScoreColor(getFreshnessScore(currentMetrics?.dataFreshnessHours || 0))"
                />
              </el-descriptions-item>
              <el-descriptions-item label="更新频率">
                <el-progress
                  :percentage="getUpdateFrequencyScore(currentMetrics?.updateFrequency || 0)"
                  :color="getScoreColor(getUpdateFrequencyScore(currentMetrics?.updateFrequency || 0))"
                />
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </el-card>
    </div>
    
    <el-empty v-else description="请选择一个表查看质量指标" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import api from '@/api'

const selectedTableId = ref<string>('')
const days = ref<number>(30)
const loading = ref(false)
const tables = ref<any[]>([])
const currentMetrics = ref<any>(null)
const trendData = ref<any[]>([])
const chartRef = ref<HTMLElement | null>(null)
let chartInstance: echarts.ECharts | null = null

const qualityScore = computed(() => {
  if (!currentMetrics.value) return 0
  
  const completeness = (1 - currentMetrics.value.nullRate) * 100
  const freshness = getFreshnessScore(currentMetrics.value.dataFreshnessHours)
  const updateFreq = getUpdateFrequencyScore(currentMetrics.value.updateFrequency)
  
  return Math.round((completeness + freshness + updateFreq) / 3)
})

const loadTables = async () => {
  try {
    const response = await api.get('/api/v1/tables', {
      params: { page: 1, pageSize: 1000 }
    })
    tables.value = response.data.items
  } catch (err: any) {
    ElMessage.error(err.message || '加载表列表失败')
  }
}

const loadQualityMetrics = async () => {
  if (!selectedTableId.value) return
  
  loading.value = true
  try {
    const [metricsRes, trendRes] = await Promise.all([
      api.get(`/api/v1/quality/${selectedTableId.value}/latest`),
      api.get(`/api/v1/quality/${selectedTableId.value}/trend`, {
        params: { days: days.value }
      })
    ])
    
    currentMetrics.value = metricsRes.data
    trendData.value = trendRes.data
    
    await nextTick()
    renderChart()
  } catch (err: any) {
    ElMessage.error(err.message || '加载质量指标失败')
  } finally {
    loading.value = false
  }
}

const renderChart = () => {
  if (!chartRef.value || trendData.value.length === 0) return
  
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  
  const dates = trendData.value.map((item: any) => 
    new Date(item.measuredAt).toLocaleDateString('zh-CN')
  )
  const recordCounts = trendData.value.map((item: any) => item.recordCount)
  const nullRates = trendData.value.map((item: any) => (item.nullRate * 100).toFixed(2))
  const freshness = trendData.value.map((item: any) => item.dataFreshnessHours)
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['记录数', '空值率(%)', '数据新鲜度(小时)']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: [
      {
        type: 'value',
        name: '记录数',
        position: 'left'
      },
      {
        type: 'value',
        name: '空值率(%)',
        position: 'right',
        max: 100
      }
    ],
    series: [
      {
        name: '记录数',
        type: 'line',
        data: recordCounts,
        smooth: true,
        yAxisIndex: 0
      },
      {
        name: '空值率(%)',
        type: 'line',
        data: nullRates,
        smooth: true,
        yAxisIndex: 1
      },
      {
        name: '数据新鲜度(小时)',
        type: 'line',
        data: freshness,
        smooth: true,
        yAxisIndex: 0
      }
    ]
  }
  
  chartInstance.setOption(option)
}

const getFreshnessScore = (hours: number): number => {
  if (hours <= 1) return 100
  if (hours <= 6) return 90
  if (hours <= 24) return 70
  if (hours <= 72) return 50
  return 30
}

const getUpdateFrequencyScore = (frequency: number): number => {
  if (frequency >= 10) return 100
  if (frequency >= 5) return 80
  if (frequency >= 1) return 60
  return 40
}

const getScoreColor = (score: number): string => {
  if (score >= 80) return '#67C23A'
  if (score >= 60) return '#E6A23C'
  return '#F56C6C'
}

onMounted(() => {
  loadTables()
  
  window.addEventListener('resize', () => {
    chartInstance?.resize()
  })
})

watch(selectedTableId, () => {
  if (selectedTableId.value) {
    loadQualityMetrics()
  }
})
</script>

<style scoped>
.quality-container {
  padding: 20px;
}

.selector-panel {
  margin-bottom: 20px;
}

.metrics-row {
  margin-bottom: 20px;
}

.chart-panel {
  margin-bottom: 20px;
}

.chart-container {
  width: 100%;
  height: 400px;
}

.score-panel {
  margin-bottom: 20px;
}

.score-content {
  display: flex;
  gap: 40px;
  align-items: center;
}

.score-circle {
  flex-shrink: 0;
}

.score-text {
  font-size: 48px;
  font-weight: bold;
}

.score-label {
  font-size: 16px;
  color: #909399;
}

.score-details {
  flex: 1;
}
</style>
