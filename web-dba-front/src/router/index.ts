import { createRouter, createWebHistory } from 'vue-router'
import DataSourceView from "@/views/DataSourceView.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'datasource',
      component: DataSourceView
    },
    {
      path: '/import',
      name: 'import',
      component:  () => import('@/views/ImportView.vue')
    },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue')
    }
  ]
})

export default router
