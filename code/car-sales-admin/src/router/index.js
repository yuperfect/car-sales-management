import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/admin'
  },
  {
    path: '/admin',
    name: 'Dashboard',
    component: () => import('../views/AdminDashboard.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/admin/cars',
    name: 'CarManage',
    component: () => import('../views/CarManage.vue'),
    meta: { title: '车辆管理' }
  },
  {
    path: '/admin/cars/new',
    name: 'CarNew',
    component: () => import('../views/CarForm.vue'),
    meta: { title: '新增车辆', isNew: true }
  },
  {
    path: '/admin/cars/:id/edit',
    name: 'CarEdit',
    component: () => import('../views/CarForm.vue'),
    meta: { title: '编辑车辆', isNew: false }
  },
  {
    path: '/admin/cars/import',
    name: 'CarImport',
    component: () => import('../views/CarImport.vue'),
    meta: { title: 'Excel导入车辆' }
  },
  {
    path: '/admin/users',
    name: 'CustomerManage',
    component: () => import('../views/CustomerManage.vue'),
    meta: { title: '客户管理' }
  },
  {
    path: '/admin/appointments',
    name: 'AppointmentManage',
    component: () => import('../views/AppointmentManage.vue'),
    meta: { title: '预约管理' }
  },
  {
    path: '/admin/orders',
    name: 'OrderManage',
    component: () => import('../views/OrderManage.vue'),
    meta: { title: '订单管理' }
  },
  {
    path: '/admin/queries',
    name: 'QueryView',
    component: () => import('../views/QueryView.vue'),
    meta: { title: '综合查询' }
  },
  {
    path: '/admin/statistics',
    name: 'StatisticsView',
    component: () => import('../views/StatisticsView.vue'),
    meta: { title: '统计分析' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
