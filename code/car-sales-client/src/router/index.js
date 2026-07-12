import { createRouter, createWebHistory } from 'vue-router'
import { isIdentified } from '../utils/user.js'

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
    path: '/my/appointments',
    name: 'MyAppointments',
    component: () => import('../views/MyAppointments.vue'),
    meta: { title: '我的预约' }
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
    component: () => import('../views/CustomerProfile.vue'),
    meta: { title: '个人信息' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局导航守卫：未设置用户名时，只允许访问个人信息页
router.beforeEach((to, from, next) => {
  if (to.path === '/my/profile') {
    // 个人信息页始终允许访问
    next()
    return
  }
  if (!isIdentified()) {
    // 未设置用户名，重定向到个人信息页
    next('/my/profile')
    return
  }
  next()
})

export default router
