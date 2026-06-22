import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('../views/Login.vue') },
  { path: '/register', component: () => import('../views/Register.vue') },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('../views/Dashboard.vue') },
      { path: 'monitoring', component: () => import('../views/Monitoring.vue') },
      { path: 'plan', component: () => import('../views/Plan.vue') },
      { path: 'assistant', component: () => import('../views/Assistant.vue') },
      { path: 'health-manager', component: () => import('../views/HealthManager.vue') },
      { path: 'exam', component: () => import('../views/ExamBooking.vue') },
      { path: 'imaging', component: () => import('../views/Imaging.vue') },
      { path: 'account', component: () => import('../views/Account.vue') },
      { path: 'profile', component: () => import('../views/Profile.vue') },
      { path: 'glucose', component: () => import('../views/GlucoseRecords.vue') },
      { path: 'diet', component: () => import('../views/DietRecords.vue') },
      { path: 'exercise', component: () => import('../views/ExerciseRecords.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 登录守卫
router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  const isPublic = to.path === '/login' || to.path === '/register'
  if (!token && !isPublic) {
    return '/login'
  }
  if (token && isPublic) {
    return '/dashboard'
  }
  return true
})

export default router
