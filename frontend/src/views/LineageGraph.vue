<template>
  <div class="lineage-container">
    <el-card class="control-panel">
      <el-form :inline="true">
        <el-form-item label="选择表">
          <el-select
            v-model="selectedTableId"
            filterable
            placeholder="请选择表"
            style="width: 300px"
            @change="loadLineageGraph"
          >
            <el-option
              v-for="table in tables"
              :key="table.id"
              :label="`${table.databaseName}.${table.tableName}`"
              :value="table.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="方向">
          <el-radio-group v-model="direction" @change="loadLineageGraph">
            <el-radio-button label="upstream">上游</el-radio-button>
            <el-radio-button label="downstream">下游</el-radio-button>
            <el-radio-button label="both">双向</el-radio-button>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="深度">
          <el-select v-model="depth" @change="loadLineageGraph" style="width: 100px">
            <el-option :value="1" label="1" />
            <el-option :value="2" label="2" />
            <el-option :value="3" label="3" />
            <el-option :value="4" label="4" />
            <el-option :value="5" label="5" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="loadLineageGraph" :loading="loading">
            刷新
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="graph-panel" v-loading="loading">
      <div v-if="!selectedTableId" class="empty-state">
        <el-empty description="请选择一个表查看血缘关系" />
      </div>
      
      <div v-else-if="graphData && graphData.nodes.length > 0" class="graph-container">
        <div ref="graphRef" class="graph-canvas"></div>
        
        <div class="graph-info">
          <el-statistic title="节点数" :value="graphData.nodes.length" />
          <el-statistic title="关系数" :value="graphData.edges.length" />
        </div>
      </div>
      
      <div v-else class="empty-state">
        <el-empty description="该表没有血缘关系" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue'
import { useLineageStore } from '@/stores/lineage'
import { useTableStore } from '@/stores/table'
import { ElMessage } from 'element-plus'
import * as d3 from 'd3'

const lineageStore = useLineageStore()
const tableStore = useTableStore()

const selectedTableId = ref<string>('')
const direction = ref<'upstream' | 'downstream' | 'both'>('both')
const depth = ref<number>(2)
const loading = ref(false)
const graphRef = ref<HTMLElement | null>(null)
const tables = ref<any[]>([])
const graphData = ref<any>(null)

const loadTables = async () => {
  try {
    await tableStore.fetchTables({ page: 1, pageSize: 1000 })
    tables.value = tableStore.tables
  } catch (err: any) {
    ElMessage.error(err.message || '加载表列表失败')
  }
}

const loadLineageGraph = async () => {
  if (!selectedTableId.value) return
  
  loading.value = true
  try {
    await lineageStore.fetchLineageGraph(selectedTableId.value, direction.value, depth.value)
    graphData.value = lineageStore.lineageGraph
    
    await nextTick()
    renderGraph()
  } catch (err: any) {
    ElMessage.error(err.message || '加载血缘图谱失败')
  } finally {
    loading.value = false
  }
}

const renderGraph = () => {
  if (!graphRef.value || !graphData.value) return
  
  const container = graphRef.value
  container.innerHTML = ''
  
  const width = container.clientWidth
  const height = 600
  
  const svg = d3.select(container)
    .append('svg')
    .attr('width', width)
    .attr('height', height)
  
  const g = svg.append('g')

  const simulation = d3.forceSimulation(graphData.value.nodes)
    .force('link', d3.forceLink(graphData.value.edges).id((d: any) => d.id).distance(150))
    .force('charge', d3.forceManyBody().strength(-300))
    .force('center', d3.forceCenter(width / 2, height / 2))
  
  const link = g.append('g')
    .selectAll('line')
    .data(graphData.value.edges)
    .enter().append('line')
    .attr('stroke', '#999')
    .attr('stroke-width', 2)
    .attr('marker-end', 'url(#arrowhead)')
  
  svg.append('defs').append('marker')
    .attr('id', 'arrowhead')
    .attr('viewBox', '-0 -5 10 10')
    .attr('refX', 25)
    .attr('refY', 0)
    .attr('orient', 'auto')
    .attr('markerWidth', 8)
    .attr('markerHeight', 8)
    .append('svg:path')
    .attr('d', 'M 0,-5 L 10 ,0 L 0,5')
    .attr('fill', '#999')
  
  const node = g.append('g')
    .selectAll('g')
    .data(graphData.value.nodes)
    .enter().append('g')
    .call(d3.drag()
      .on('start', dragstarted)
      .on('drag', dragged)
      .on('end', dragended) as any)
  
  node.append('circle')
    .attr('r', 20)
    .attr('fill', (d: any) => d.id === selectedTableId.value ? '#409EFF' : '#67C23A')

  node.append('text')
    .attr('dy', 35)
    .attr('text-anchor', 'middle')
    .attr('font-size', '12px')
    .text((d: any) => d.name)
  
  simulation.on('tick', () => {
    link
      .attr('x1', (d: any) => d.source.x)
      .attr('y1', (d: any) => d.source.y)
      .attr('x2', (d: any) => d.target.x)
      .attr('y2', (d: any) => d.target.y)
    
    node.attr('transform', (d: any) => `translate(${d.x},${d.y})`)
  })
  
  const zoom = d3.zoom()
    .scaleExtent([0.1, 4])
    .on('zoom', (event) => {
      g.attr('transform', event.transform)
    })
  
  svg.call(zoom as any)
  
  function dragstarted(event: any) {
    if (!event.active) simulation.alphaTarget(0.3).restart()
    event.subject.fx = event.subject.x
    event.subject.fy = event.subject.y
  }
  
  function dragged(event: any) {
    event.subject.fx = event.x
    event.subject.fy = event.y
  }
  
  function dragended(event: any) {
    if (!event.active) simulation.alphaTarget(0)
    event.subject.fx = null
    event.subject.fy = null
  }
}

onMounted(() => {
  loadTables()
})

watch(selectedTableId, () => {
  if (selectedTableId.value) {
    loadLineageGraph()
  }
})
</script>

<style scoped>
.lineage-container {
  padding: 20px;
}

.control-panel {
  margin-bottom: 20px;
}

.graph-panel {
  min-height: 700px;
}

.graph-container {
  position: relative;
}

.graph-canvas {
  width: 100%;
  height: 600px;
  border: 1px solid #DCDFE6;
  border-radius: 4px;
}

.graph-info {
  display: flex;
  gap: 40px;
  margin-top: 20px;
  justify-content: center;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}
</style>
