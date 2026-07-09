<template>
  <div class="layout">
    <!-- Sidebar -->
    <aside class="sidebar">
      <div class="sidebar-logo">🚗 汽车销售管理</div>
      <nav class="sidebar-nav">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
        >
          <span class="nav-icon">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>
    </aside>

    <!-- Main Area -->
    <div class="main-area">
      <header class="topbar">
        <h1 class="topbar-title">汽车销售管理系统 — 管理员端</h1>
      </header>
      <main class="content-area">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()

const navItems = [
  { path: '/admin', label: '首页', icon: '📊' },
  { path: '/admin/cars', label: '车辆管理', icon: '🚙' },
  { path: '/admin/users', label: '客户管理', icon: '👤' },
  { path: '/admin/appointments', label: '预约管理', icon: '📅' },
  { path: '/admin/orders', label: '订单管理', icon: '📋' },
  { path: '/admin/queries', label: '综合查询', icon: '🔍' },
  { path: '/admin/statistics', label: '统计分析', icon: '📈' }
]

function isActive(path) {
  return route.path === path || route.path.startsWith(path + '/')
}
</script>
