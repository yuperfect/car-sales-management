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
    path: '/appointment/new',
    name: 'AppointmentNew',
    component: () => import('../views/AppointmentNew.vue'),
    meta: { title: '提交预约' }
  },
  {
    path: '/my/appointments',
    name: 'MyAppointments',
    component: () => import('../views/MyAppointments.vue'),
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
    path: '/query',
    name: 'QueryLookup',
    component: () => import('../views/QueryLookup.vue'),
    meta: { title: '查询中心' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
