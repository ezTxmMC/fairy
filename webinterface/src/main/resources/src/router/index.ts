import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'dashboard',
      component: () => import('@/pages/DashboardView.vue'),
      meta: { title: 'Dashboard' },
    },
    {
      path: '/groups',
      name: 'groups',
      component: () => import('@/pages/GroupsView.vue'),
      meta: { title: 'Gruppen' },
    },
    {
      path: '/templates',
      name: 'templates',
      component: () => import('@/pages/TemplatesView.vue'),
      meta: { title: 'Templates' },
    },
    {
      path: '/services',
      name: 'services',
      component: () => import('@/pages/ServicesView.vue'),
      meta: { title: 'Services' },
    },
    {
      path: '/services/:id',
      name: 'service-details',
      component: () => import('@/pages/ServiceDetailsView.vue'),
      meta: { title: 'Service Details' },
      props: true,
    },
  ],
})

export default router
