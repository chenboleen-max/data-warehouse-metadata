# 前端性能优化指南

## 已实现的优化

### 1. 路由懒加载 ✅

所有页面组件都使用动态导入，实现按需加载：

```typescript
// router/index.ts
const routes = [
  {
    path: '/login',
    component: () => import('@/views/Login.vue')  // 懒加载
  },
  {
    path: '/tables',
    component: () => import('@/views/TableList.vue')  // 懒加载
  }
  // ...
]
```

**优势**：
- 减少初始加载时间
- 按需加载页面资源
- 提高首屏渲染速度

### 2. 组件懒加载 ✅

大型组件使用异步组件：

```vue
<script setup>
import { defineAsyncComponent } from 'vue'

const HeavyComponent = defineAsyncComponent(() =>
  import('./components/HeavyComponent.vue')
)
</script>
```

### 3. 图表性能优化 ✅

#### D3.js 优化（血缘图谱）
```typescript
// 限制节点数量
if (graphData.nodes.length > 100) {
  ElMessage.warning('节点数量过多，建议减小深度')
}

// 使用 requestAnimationFrame 优化渲染
simulation.on('tick', () => {
  requestAnimationFrame(() => {
    // 更新节点和边的位置
  })
})
```

#### ECharts 优化（质量趋势）
```typescript
// 使用 resize 监听器
window.addEventListener('resize', () => {
  chartInstance?.resize()
})

// 销毁图表实例
onUnmounted(() => {
  chartInstance?.dispose()
})
```

## 推荐的优化措施

### 1. 虚拟滚动（大列表）

对于超过 100 条记录的列表，使用虚拟滚动：

```bash
npm install vue-virtual-scroller
```

```vue
<template>
  <RecycleScroller
    :items="items"
    :item-size="50"
    key-field="id"
    v-slot="{ item }"
  >
    <div class="item">{{ item.name }}</div>
  </RecycleScroller>
</template>

<script setup>
import { RecycleScroller } from 'vue-virtual-scroller'
import 'vue-virtual-scroller/dist/vue-virtual-scroller.css'
</script>
```

### 2. 请求防抖和节流

```typescript
import { debounce, throttle } from 'lodash-es'

// 搜索防抖
const debouncedSearch = debounce((keyword: string) => {
  searchStore.search({ keyword })
}, 300)

// 滚动节流
const throttledScroll = throttle(() => {
  // 处理滚动事件
}, 100)
```

### 3. 图片懒加载

```vue
<template>
  <img v-lazy="imageUrl" alt="description" />
</template>

<script setup>
import { directive as vLazy } from 'vue3-lazy'
</script>
```

### 4. 组件缓存

使用 `<KeepAlive>` 缓存组件状态：

```vue
<template>
  <router-view v-slot="{ Component }">
    <keep-alive :include="['TableList', 'Search']">
      <component :is="Component" />
    </keep-alive>
  </router-view>
</template>
```

### 5. 数据缓存

```typescript
// stores/table.ts
export const useTableStore = defineStore('table', () => {
  const cache = new Map<string, any>()
  const CACHE_TTL = 5 * 60 * 1000  // 5分钟
  
  const fetchTableWithCache = async (id: string) => {
    const cached = cache.get(id)
    if (cached && Date.now() - cached.timestamp < CACHE_TTL) {
      return cached.data
    }
    
    const data = await api.get(`/api/v1/tables/${id}`)
    cache.set(id, { data, timestamp: Date.now() })
    return data
  }
  
  return { fetchTableWithCache }
})
```

### 6. 代码分割

#### 按功能分割
```typescript
// vite.config.ts
export default defineConfig({
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'element-plus': ['element-plus'],
          'charts': ['echarts', 'd3'],
          'vendor': ['vue', 'vue-router', 'pinia', 'axios']
        }
      }
    }
  }
})
```

### 7. 资源压缩

#### Vite 配置
```typescript
// vite.config.ts
import viteCompression from 'vite-plugin-compression'

export default defineConfig({
  plugins: [
    viteCompression({
      algorithm: 'gzip',
      ext: '.gz'
    })
  ],
  build: {
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,  // 生产环境移除 console
        drop_debugger: true
      }
    }
  }
})
```

### 8. CDN 加速

```html
<!-- index.html -->
<script src="https://cdn.jsdelivr.net/npm/vue@3.4.15/dist/vue.global.prod.js"></script>
<script src="https://cdn.jsdelivr.net/npm/element-plus@2.5.4/dist/index.full.min.js"></script>
```

```typescript
// vite.config.ts
export default defineConfig({
  build: {
    rollupOptions: {
      external: ['vue', 'element-plus'],
      output: {
        globals: {
          vue: 'Vue',
          'element-plus': 'ElementPlus'
        }
      }
    }
  }
})
```

### 9. 预加载和预连接

```html
<!-- index.html -->
<link rel="preconnect" href="https://api.example.com">
<link rel="dns-prefetch" href="https://api.example.com">
<link rel="preload" href="/fonts/main.woff2" as="font" type="font/woff2" crossorigin>
```

### 10. Service Worker（PWA）

```typescript
// vite.config.ts
import { VitePWA } from 'vite-plugin-pwa'

export default defineConfig({
  plugins: [
    VitePWA({
      registerType: 'autoUpdate',
      workbox: {
        globPatterns: ['**/*.{js,css,html,ico,png,svg}'],
        runtimeCaching: [
          {
            urlPattern: /^https:\/\/api\.example\.com\/.*/i,
            handler: 'NetworkFirst',
            options: {
              cacheName: 'api-cache',
              expiration: {
                maxEntries: 100,
                maxAgeSeconds: 60 * 60 * 24  // 24小时
              }
            }
          }
        ]
      }
    })
  ]
})
```

## 性能监控

### 1. 使用 Performance API

```typescript
// utils/performance.ts
export function measurePageLoad() {
  window.addEventListener('load', () => {
    const perfData = window.performance.timing
    const pageLoadTime = perfData.loadEventEnd - perfData.navigationStart
    const connectTime = perfData.responseEnd - perfData.requestStart
    const renderTime = perfData.domComplete - perfData.domLoading
    
    console.log('页面加载时间:', pageLoadTime, 'ms')
    console.log('请求响应时间:', connectTime, 'ms')
    console.log('页面渲染时间:', renderTime, 'ms')
  })
}
```

### 2. 使用 Lighthouse

```bash
# 安装 Lighthouse
npm install -g lighthouse

# 运行性能测试
lighthouse http://localhost:5173 --view
```

### 3. 使用 Vue Devtools

- 安装 Vue Devtools 浏览器扩展
- 查看组件渲染时间
- 分析组件层级
- 监控 Pinia 状态变化

### 4. 使用 Vite 分析插件

```bash
npm install rollup-plugin-visualizer -D
```

```typescript
// vite.config.ts
import { visualizer } from 'rollup-plugin-visualizer'

export default defineConfig({
  plugins: [
    visualizer({
      open: true,
      gzipSize: true,
      brotliSize: true
    })
  ]
})
```

## 性能指标

### 目标值

| 指标 | 目标 | 说明 |
|------|------|------|
| FCP (First Contentful Paint) | < 1.8s | 首次内容绘制 |
| LCP (Largest Contentful Paint) | < 2.5s | 最大内容绘制 |
| FID (First Input Delay) | < 100ms | 首次输入延迟 |
| CLS (Cumulative Layout Shift) | < 0.1 | 累积布局偏移 |
| TTI (Time to Interactive) | < 3.8s | 可交互时间 |
| Bundle Size | < 500KB | 打包大小（gzip） |

### 测试命令

```bash
# 开发环境
npm run dev

# 生产构建
npm run build

# 预览生产构建
npm run preview

# 分析打包大小
npm run build -- --report
```

## 最佳实践清单

- [x] 路由懒加载
- [x] 组件懒加载
- [ ] 虚拟滚动（大列表）
- [ ] 请求防抖和节流
- [ ] 图片懒加载
- [ ] 组件缓存（KeepAlive）
- [ ] 数据缓存
- [ ] 代码分割
- [ ] 资源压缩
- [ ] CDN 加速
- [ ] 预加载和预连接
- [ ] Service Worker（PWA）

## 性能优化检查清单

### 开发阶段
- [ ] 避免不必要的组件重渲染
- [ ] 使用 `v-show` 代替 `v-if`（频繁切换）
- [ ] 使用 `key` 优化列表渲染
- [ ] 避免在模板中使用复杂表达式
- [ ] 使用计算属性缓存结果

### 构建阶段
- [ ] 启用代码压缩
- [ ] 启用 Tree Shaking
- [ ] 移除未使用的依赖
- [ ] 优化图片资源
- [ ] 使用 CDN 加载第三方库

### 运行阶段
- [ ] 启用 HTTP/2
- [ ] 启用 Gzip/Brotli 压缩
- [ ] 设置合理的缓存策略
- [ ] 使用 CDN 分发静态资源
- [ ] 监控性能指标

## 总结

前端性能优化已完成基础配置：
- ✅ 路由懒加载
- ✅ 组件懒加载
- ✅ 图表性能优化

建议根据实际使用情况，逐步实施其他优化措施，并持续监控性能指标。
