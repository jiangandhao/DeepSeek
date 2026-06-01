import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/common/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/common/Register.vue')
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/blood-sugar',
    name: 'BloodSugar',
    component: () => import('../views/bloodSugar/Dashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/blood-sugar/diet',
    name: 'DietPlan',
    component: () => import('../views/bloodSugar/DietPlan.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/blood-sugar/exercise',
    name: 'ExercisePlan',
    component: () => import('../views/bloodSugar/ExercisePlan.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/blood-sugar/trend',
    name: 'TrendAnalysis',
    component: () => import('../views/bloodSugar/TrendAnalysis.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/health-manager',
    name: 'HealthManager',
    component: () => import('../views/healthManager/RiskAssessment.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/health-manager/diet',
    name: 'DietPrescription',
    component: () => import('../views/healthManager/DietPrescription.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/health-manager/exercise',
    name: 'ExerciseRecommend',
    component: () => import('../views/healthManager/ExerciseRecommend.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/checkup',
    name: 'Checkup',
    component: () => import('../views/checkup/Appointment.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/checkup/report',
    name: 'ReportView',
    component: () => import('../views/checkup/ReportView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/checkup/image',
    name: 'ImageAnalysis',
    component: () => import('../views/checkup/ImageAnalysis.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/risk-warning',
    name: 'RiskWarning',
    component: () => import('../views/riskWarning/RiskLevel.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/risk-warning/early',
    name: 'EarlyWarning',
    component: () => import('../views/riskWarning/EarlyWarning.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/risk-warning/prevention',
    name: 'PreventionPlan',
    component: () => import('../views/riskWarning/PreventionPlan.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/common/UserProfile.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
