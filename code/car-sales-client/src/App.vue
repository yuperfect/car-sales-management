<template>
  <div class="app-layout">
    <!-- 侧边栏导航 -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <h1>汽车销售管理<br/>— 客户端</h1>
      </div>
      <nav class="sidebar-nav">
        <router-link to="/" :class="{ active: $route.path === '/' }">
          <span class="nav-icon">🚗</span> 在售车辆
        </router-link>
        <router-link to="/test-drive/new" :class="{ active: $route.path === '/test-drive/new' }">
          <span class="nav-icon">📅</span> 提交试驾
        </router-link>
        <router-link to="/my/test-drives" :class="{ active: $route.path === '/my/test-drives' }">
          <span class="nav-icon">📋</span> 我的预约
        </router-link>
        <router-link to="/orders/new" :class="{ active: $route.path === '/orders/new' }">
          <span class="nav-icon">🛒</span> 提交订单
        </router-link>
        <router-link to="/my/orders" :class="{ active: $route.path === '/my/orders' }">
          <span class="nav-icon">📦</span> 我的订单
        </router-link>
        <router-link to="/my/profile" :class="{ active: $route.path === '/my/profile' }">
          <span class="nav-icon">👤</span> 个人信息
        </router-link>
      </nav>
    </aside>

    <!-- 主内容区 -->
    <main class="main-area">
      <div class="page-header">
        <h2>{{ currentTitle }}</h2>
      </div>
      <div class="page-content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const currentTitle = computed(() => route.meta?.title || '汽车销售管理系统')
</script>
