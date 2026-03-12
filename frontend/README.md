# Frontend - 数据仓库元数据管理系统

Vue.js 3 + TypeScript 前端应用

## 技术栈

- Vue.js 3 (Composition API)
- TypeScript
- Element Plus (UI 组件库)
- Pinia (状态管理)
- Vue Router 4 (路由)
- Vite (构建工具)
- Axios (HTTP 客户端)
- ECharts (图表)
- D3.js (血缘关系图)

## 项目结构

```
frontend/
├── src/
│   ├── api/              # API 客户端
│   ├── assets/           # 静态资源
│   ├── components/       # 可复用组件
│   ├── views/            # 页面视图
│   ├── stores/           # Pinia 状态管理
│   ├── router/           # 路由配置
│   ├── types/            # TypeScript 类型定义
│   ├── utils/            # 工具函数
│   ├── App.vue           # 根组件
│   └── main.ts           # 应用入口
├── public/               # 公共资源
├── tests/                # 测试
├── package.json          # 依赖配置
├── vite.config.ts        # Vite 配置
├── tsconfig.json         # TypeScript 配置
└── Dockerfile            # Docker 镜像
```

## 开发环境设置

### 1. 安装依赖

```bash
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

访问: http://localhost:5173

### 3. 构建生产版本

```bash
npm run build
```

### 4. 预览生产构建

```bash
npm run preview
```

## 开发指南

### 组件开发

使用 Vue 3 Composition API 和 TypeScript：

```vue
<template>
  <div class="my-component">
    <h1>{{ title }}</h1>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const title = ref<string>('Hello World')
</script>

<style scoped>
.my-component {
  padding: 20px;
}
</style>
```

### 状态管理

使用 Pinia 管理全局状态：

```typescript
// stores/user.ts
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null,
    token: null,
  }),
  actions: {
    setUser(user: any) {
      this.user = user
    },
  },
})
```

### API 调用

```typescript
// api/tables.ts
import axios from './client'

export const getTables = async (params: any) => {
  const response = await axios.get('/api/tables', { params })
  return response.data
}
```

## 测试

### 单元测试

```bash
npm run test:unit
```

### E2E 测试

```bash
npm run test:e2e
```

## 代码质量

### Lint 检查

```bash
npm run lint
```

### 格式化代码

```bash
npm run format
```

## 构建优化

### 代码分割

Vite 自动进行代码分割，主要分为：

- `vue-vendor`: Vue 核心库
- `element-plus`: UI 组件库
- `charts`: 图表库

### 性能优化

- 组件懒加载
- 路由懒加载
- 图片懒加载
- 虚拟滚动（大列表）

## 部署

### 使用 Docker

```bash
docker build -t kiro-web-frontend .
docker run -p 80:80 kiro-web-frontend
```

### 使用 Nginx

构建后将 `dist/` 目录部署到 Nginx：

```nginx
server {
    listen 80;
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

## 环境变量

在 `.env` 文件中配置：

```
VITE_API_BASE_URL=http://localhost:8000
```

在代码中使用：

```typescript
const apiUrl = import.meta.env.VITE_API_BASE_URL
```

## 浏览器支持

- Chrome >= 87
- Firefox >= 78
- Safari >= 14
- Edge >= 88

## 开发工具推荐

- VS Code
- Vue Language Features (Volar)
- TypeScript Vue Plugin (Volar)
- ESLint
- Prettier
