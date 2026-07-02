import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'CarList',
    component: () => import('../views/CarList.vue'),
    meta: { title: '在售车辆' }
  },
  {
    path: '/cars/:id',
    name: 'CarDetail',
    component: () => import('../views/CarDetail.vue'),
    meta: { title: '车辆详情' }
  },
  {
    path: '/test-drive/new',
    name: 'TestDriveNew',
    component: () => import('../views/TestDriveNew.vue'),
    meta: { title: '提交试驾预约' }
  },
  {
    path: '/my/test-drives',
    name: 'MyTestDrives',
    component: () => import('../views/MyTestDrives.vue'),
    meta: { title: '我的预约' }
  },
  {
    path: '/orders/new',
    name: 'OrderNew',
    component: () => import('../views/OrderNew.vue'),
    meta: { title: '提交订单' }
  },
  {
    path: '/my/orders',
    name: 'MyOrders',
    component: () => import('../views/MyOrders.vue'),
    meta: { title: '我的订单' }
  },
  {
    path: '/my/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: { title: '个人信息' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
