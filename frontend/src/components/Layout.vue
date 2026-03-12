<template>
  <el-container class="layout-container">
    <el-header class="layout-header">
      <Navbar />
    </el-header>
    <el-container>
      <el-aside width="200px" class="layout-aside">
        <Sidebar />
      </el-aside>
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import Navbar from './Navbar.vue'
import Sidebar from './Sidebar.vue'
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.layout-header {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0;
  height: 60px;
  display: flex;
  align-items: center;
}

.layout-aside {
  background-color: #fff;
  border-right: 1px solid #e4e7ed;
  overflow-y: auto;
}

.layout-main {
  background-color: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}
</style>
