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
      path: '/data/sync',
      name: 'data-sync',
      component: () => import('../views/DataSyncView.vue')
    },
    {
      path: '/data/sync/add',
      name: 'data-sync-add',
      component: () => import('../views/DataSyncAddView.vue')
    }
  ]
})

export default router
